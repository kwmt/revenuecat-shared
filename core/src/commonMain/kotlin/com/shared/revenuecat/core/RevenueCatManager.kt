package com.shared.revenuecat.core

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * RevenueCat操作の共通マネージャー。
 *
 * 使い方:
 * ```
 * RevenueCatManager.configure(RevenueCatConfig(apiKey = "appl_XXXXX"))
 * val isPremium = RevenueCatManager.checkEntitlement()
 * ```
 */
object RevenueCatManager {

    private var config: RevenueCatConfig? = null

    private val _entitlementStatus = MutableStateFlow(EntitlementStatus(isActive = false))
    val entitlementStatus: StateFlow<EntitlementStatus> = _entitlementStatus.asStateFlow()

    /** キャッシュベースで即座にEntitlementを確認する */
    val isPremium: Boolean
        get() = _entitlementStatus.value.isActive

    // -------------------------------------------------------
    // 初期化
    // -------------------------------------------------------

    fun configure(config: RevenueCatConfig) {
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

    suspend fun checkEntitlement(): EntitlementStatus {
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
        } catch (e: Exception) {
            EntitlementStatus(isActive = false)
        }
    }

    // -------------------------------------------------------
    // Offerings
    // -------------------------------------------------------

    suspend fun fetchOfferings(): List<OfferingInfo> {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        return offerings.all.values.map { offering ->
            OfferingInfo(
                identifier = offering.identifier,
                availablePackages = offering.availablePackages.map { pkg ->
                    PackageInfo(
                        identifier = pkg.identifier,
                        localizedPriceString = pkg.storeProduct.price.formatted,
                        productIdentifier = pkg.storeProduct.id,
                        rcPackage = pkg,
                    )
                }
            )
        }
    }

    suspend fun fetchCurrentOfferingPackages(): List<PackageInfo> {
        val offerings = Purchases.sharedInstance.awaitOfferings()
        val current = offerings.current ?: return emptyList()
        return current.availablePackages.map { pkg ->
            PackageInfo(
                identifier = pkg.identifier,
                localizedPriceString = pkg.storeProduct.price.formatted,
                productIdentifier = pkg.storeProduct.id,
                rcPackage = pkg,
            )
        }
    }

    // -------------------------------------------------------
    // 購入
    // -------------------------------------------------------

    suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult {
        val pkg = packageInfo.rcPackage as com.revenuecat.purchases.kmp.models.Package
        return try {
            val result = Purchases.sharedInstance.awaitPurchase(pkg)
            val isActive = result.customerInfo
                .entitlements[requireConfig().entitlementId]
                ?.isActive == true
            _entitlementStatus.value = EntitlementStatus(isActive = isActive)
            PurchaseResult.Success(isActive = isActive)
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            if (message.contains("userCancelled", ignoreCase = true) ||
                message.contains("PurchaseCancelled", ignoreCase = true)
            ) {
                PurchaseResult.Cancelled
            } else {
                PurchaseResult.Error(message = message)
            }
        }
    }

    // -------------------------------------------------------
    // リストア
    // -------------------------------------------------------

    suspend fun restore(): PurchaseResult {
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

    suspend fun login(appUserId: String) {
        Purchases.sharedInstance.awaitLogIn(appUserId)
        checkEntitlement()
    }

    suspend fun logout() {
        Purchases.sharedInstance.awaitLogOut()
        _entitlementStatus.value = EntitlementStatus(isActive = false)
    }

    // -------------------------------------------------------
    // Private
    // -------------------------------------------------------

    private fun requireConfig(): RevenueCatConfig {
        return config ?: error(
            "RevenueCatManager is not configured. Call RevenueCatManager.configure() first."
        )
    }
}
