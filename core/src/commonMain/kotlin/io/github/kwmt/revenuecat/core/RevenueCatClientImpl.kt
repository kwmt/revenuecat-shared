package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.ktx.awaitTrialOrIntroPriceEligibility
import com.revenuecat.purchases.kmp.models.GoogleReplacementMode
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.Store
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.Volatile
import kotlinx.coroutines.flow.asStateFlow

/**
 * [RevenueCatClient] の実装クラス。
 *
 * インスタンスの生成には [RevenueCatClientFactory.create] を使用する。
 */
internal class RevenueCatClientImpl : RevenueCatClient {

    @Volatile
    private var config: RevenueCatConfig? = null

    private val _entitlementStatus = MutableStateFlow(EntitlementStatus(isActive = false))
    override val entitlementStatus: StateFlow<EntitlementStatus> = _entitlementStatus.asStateFlow()

    override val isPremium: Boolean
        get() = _entitlementStatus.value.isActive

    // -------------------------------------------------------
    // 初期化
    // -------------------------------------------------------

    override fun configure(config: RevenueCatConfig) {
        this.config = config

        Purchases.logLevel = if (config.debugLogsEnabled) {
            com.revenuecat.purchases.kmp.LogLevel.DEBUG
        } else {
            com.revenuecat.purchases.kmp.LogLevel.ERROR
        }

        Purchases.configure(
            PurchasesConfiguration(apiKey = config.apiKey) {}
        )
    }

    // -------------------------------------------------------
    // Entitlement 確認
    // -------------------------------------------------------

    override suspend fun checkEntitlement(): EntitlementStatus =
        checkEntitlementOrNull() ?: EntitlementStatus(isActive = false)

    override suspend fun checkEntitlementOrNull(): EntitlementStatus? {
        val entitlementId = requireConfig().entitlementId
        return try {
            val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
            val entitlement = customerInfo.entitlements[entitlementId]
            val status = EntitlementStatus(
                isActive = entitlement?.isActive == true,
                willRenew = entitlement?.willRenew == true,
                expirationDateMillis = entitlement?.expirationDateMillis,
            )
            _entitlementStatus.value = status
            status
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun checkEntitlementOrNull(entitlementId: String): EntitlementStatus? = try {
        val entitlement = Purchases.sharedInstance.awaitCustomerInfo().entitlements[entitlementId]
        EntitlementStatus(
            isActive = entitlement?.isActive == true,
            willRenew = entitlement?.willRenew == true,
            expirationDateMillis = entitlement?.expirationDateMillis,
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

    // -------------------------------------------------------
    // Offerings
    // -------------------------------------------------------

    override suspend fun fetchOfferings(): List<OfferingInfo> {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        return offerings.all.values.map { offering ->
            OfferingInfo(
                identifier = offering.identifier,
                availablePackages = offering.availablePackages.map { it.toPackageInfo() },
            )
        }
    }

    override suspend fun fetchCurrentOfferingPackages(): List<PackageInfo> {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val current = offerings.current ?: return emptyList()
        return current.availablePackages.map { it.toPackageInfo() }
    }

    override suspend fun checkTrialEligibility(packages: List<PackageInfo>): Map<String, TrialEligibility> {
        val products = packages.mapNotNull { (it.rcPackage as? Package)?.storeProduct }
        val sdkAnswers = try {
            if (products.isEmpty()) {
                emptyMap()
            } else {
                Purchases.sharedInstance.awaitTrialOrIntroPriceEligibility(products)
                    .entries
                    .associate { (product, status) -> product.id to status.toTrialEligibility() }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emptyMap()
        }
        return packages.associate { info ->
            val product = (info.rcPackage as? Package)?.storeProduct
            info.productIdentifier to trialEligibilityOf(sdkAnswers[info.productIdentifier], product?.googlePlayFreeTrialOffered())
        }
    }

    // -------------------------------------------------------
    // 購入
    // -------------------------------------------------------

    override suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult {
        val pkg = packageInfo.rcPackage as com.revenuecat.purchases.kmp.models.Package
        return try {
            val result = Purchases.sharedInstance.awaitPurchase(pkg)
            val isActive = result.customerInfo
                .entitlements[requireConfig().entitlementId]
                ?.isActive == true
            _entitlementStatus.value = EntitlementStatus(isActive = isActive)
            PurchaseResult.Success(isActive = isActive)
        } catch (e: Exception) {
            mapPurchaseException(e)
        }
    }

    override suspend fun purchaseChangingPlan(packageInfo: PackageInfo, mode: PlanChangeMode): PurchaseResult {
        val pkg = packageInfo.rcPackage as Package
        val entitlementId = requireConfig().entitlementId
        return try {
            val active = Purchases.sharedInstance.awaitCustomerInfo().entitlements[entitlementId]?.takeIf { it.isActive }
            val oldProductId = playPlanChangeOldProductIdOf(
                activeOnPlayStore = active?.store == Store.PLAY_STORE,
                activeProductIdentifier = active?.productIdentifier,
                activeProductPlanIdentifier = active?.productPlanIdentifier,
                targetProductIdentifier = pkg.storeProduct.id,
            ) ?: return purchase(Unit, packageInfo)
            val result = Purchases.sharedInstance.awaitPurchase(
                packageToPurchase = pkg,
                oldProductId = oldProductId,
                replacementMode = mode.toGoogleReplacementMode(),
            )
            val isActive = result.customerInfo.entitlements[entitlementId]?.isActive == true
            _entitlementStatus.value = EntitlementStatus(isActive = isActive)
            PurchaseResult.Success(isActive = isActive)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            mapPurchaseException(e)
        }
    }

    private fun PlanChangeMode.toGoogleReplacementMode(): GoogleReplacementMode = when (this) {
        PlanChangeMode.CHARGE_PRORATED_PRICE -> GoogleReplacementMode.CHARGE_PRORATED_PRICE
        PlanChangeMode.CHARGE_FULL_PRICE -> GoogleReplacementMode.CHARGE_FULL_PRICE
        PlanChangeMode.WITH_TIME_PRORATION -> GoogleReplacementMode.WITH_TIME_PRORATION
        PlanChangeMode.WITHOUT_PRORATION -> GoogleReplacementMode.WITHOUT_PRORATION
        PlanChangeMode.DEFERRED -> GoogleReplacementMode.DEFERRED
    }

    private fun mapPurchaseException(e: Exception): PurchaseResult = when {
        e is PurchasesTransactionException && e.userCancelled -> PurchaseResult.Cancelled
        e is PurchasesException -> PurchaseResult.Error(
            message = e.message,
            code = e.code.code,
        )
        else -> PurchaseResult.Error(message = e.message ?: "Unknown error")
    }

    // -------------------------------------------------------
    // リストア
    // -------------------------------------------------------

    override suspend fun restore(): PurchaseResult {
        return try {
            val customerInfo = Purchases.sharedInstance.awaitRestore()
            val isActive = customerInfo
                .entitlements[requireConfig().entitlementId]
                ?.isActive == true
            _entitlementStatus.value = EntitlementStatus(isActive = isActive)
            PurchaseResult.Success(isActive = isActive)
        } catch (e: Exception) {
            PurchaseResult.Error(message = e.message ?: "Restore failed")
        }
    }

    // -------------------------------------------------------
    // ユーザー管理
    // -------------------------------------------------------

    // ★configure の前に Purchases.sharedInstance を触ると SDK が例外を投げるので、先に null を返す
    override fun appUserIdOrNull(): String? =
        if (config == null) null else runCatching { Purchases.sharedInstance.appUserID }.getOrNull()

    override suspend fun login(appUserId: String) {
        Purchases.sharedInstance.awaitLogIn(appUserId)
        checkEntitlement()
    }

    override suspend fun logout() {
        Purchases.sharedInstance.awaitLogOut()
        _entitlementStatus.value = EntitlementStatus(isActive = false)
    }

    // -------------------------------------------------------
    // Private
    // -------------------------------------------------------

    private fun requireConfig(): RevenueCatConfig {
        return config ?: error(
            "RevenueCatClient is not configured. Call client.configure() first."
        )
    }
}
