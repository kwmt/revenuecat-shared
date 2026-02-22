package io.github.kwmt.revenuecat.paywall

import io.github.kwmt.revenuecat.core.EntitlementStatus
import io.github.kwmt.revenuecat.core.OfferingInfo
import io.github.kwmt.revenuecat.core.PackageInfo
import io.github.kwmt.revenuecat.core.PurchaseResult
import io.github.kwmt.revenuecat.core.RevenueCatClient
import io.github.kwmt.revenuecat.core.RevenueCatConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRevenueCatClient : RevenueCatClient {

    // --- Controllable return values ---
    var entitlementToReturn = EntitlementStatus(isActive = false)
    var packagesToReturn: List<PackageInfo> = emptyList()
    var offeringsToReturn: List<OfferingInfo> = emptyList()
    var purchaseResultToReturn: PurchaseResult = PurchaseResult.Success(isActive = true)
    var restoreResultToReturn: PurchaseResult = PurchaseResult.Success(isActive = true)

    // --- Error simulation ---
    var shouldThrowOnCheckEntitlement = false
    var shouldThrowOnFetchPackages = false
    var shouldThrowOnPurchase = false
    var shouldThrowOnRestore = false
    var checkEntitlementError: Exception = RuntimeException("checkEntitlement failed")
    var fetchPackagesError: Exception = RuntimeException("fetchPackages failed")
    var purchaseError: Exception = RuntimeException("purchase failed")
    var restoreError: Exception = RuntimeException("restore failed")

    // --- Call tracking ---
    var checkEntitlementCallCount = 0; private set
    var fetchPackagesCallCount = 0; private set
    var purchaseCallCount = 0; private set
    var restoreCallCount = 0; private set
    var lastPurchaseParams: Any? = null; private set
    var lastPurchasePackage: PackageInfo? = null; private set

    // --- StateFlow ---
    private val _entitlementStatus = MutableStateFlow(EntitlementStatus(isActive = false))
    override val entitlementStatus: StateFlow<EntitlementStatus> = _entitlementStatus.asStateFlow()
    override val isPremium: Boolean get() = _entitlementStatus.value.isActive

    override fun configure(config: RevenueCatConfig) { /* no-op */ }

    override suspend fun checkEntitlement(): EntitlementStatus {
        checkEntitlementCallCount++
        if (shouldThrowOnCheckEntitlement) throw checkEntitlementError
        return entitlementToReturn
    }

    override suspend fun fetchOfferings(): List<OfferingInfo> = offeringsToReturn

    override suspend fun fetchCurrentOfferingPackages(): List<PackageInfo> {
        fetchPackagesCallCount++
        if (shouldThrowOnFetchPackages) throw fetchPackagesError
        return packagesToReturn
    }

    override suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult {
        purchaseCallCount++
        lastPurchaseParams = purchaseParams
        lastPurchasePackage = packageInfo
        if (shouldThrowOnPurchase) throw purchaseError
        return purchaseResultToReturn
    }

    override suspend fun restore(): PurchaseResult {
        restoreCallCount++
        if (shouldThrowOnRestore) throw restoreError
        return restoreResultToReturn
    }

    override suspend fun login(appUserId: String) { /* no-op */ }
    override suspend fun logout() { /* no-op */ }
}
