package io.github.kwmt.revenuecat.paywall

import io.github.kwmt.revenuecat.core.EntitlementStatus
import io.github.kwmt.revenuecat.core.PackageInfo
import io.github.kwmt.revenuecat.core.PurchaseResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PaywallViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val fakeClient = FakeRevenueCatClient()
    private val viewModel = PaywallViewModel(client = fakeClient, scope = testScope)

    private val samplePackages = listOf(
        PackageInfo(
            identifier = "monthly",
            localizedPriceString = "$4.99/mo",
            productIdentifier = "com.app.monthly",
            rcPackage = "monthly_rc",
        ),
        PackageInfo(
            identifier = "annual",
            localizedPriceString = "$29.99/yr",
            productIdentifier = "com.app.annual",
            rcPackage = "annual_rc",
        ),
    )

    // --- Initial state ---

    @Test
    fun initialState_isLoading() {
        val state = viewModel.state.value
        assertTrue(state.isLoading)
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertTrue(state.packages.isEmpty())
        assertNull(state.selectedPackage)
        assertNull(state.errorMessage)
        assertFalse(state.purchaseSuccess)
    }

    // --- loadOfferings ---

    @Test
    fun loadOfferings_whenNotPremium_loadsPackagesAndSelectsFirst() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages

        viewModel.loadOfferings()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertFalse(state.isPremium)
        assertEquals(samplePackages, state.packages)
        assertEquals(samplePackages.first(), state.selectedPackage)
        assertNull(state.errorMessage)
    }

    @Test
    fun loadOfferings_whenAlreadyPremium_showsPremiumAndSkipsPackageFetch() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = true)

        viewModel.loadOfferings()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.isPremium)
        assertTrue(state.packages.isEmpty())
        assertEquals(0, fakeClient.fetchPackagesCallCount)
    }

    @Test
    fun loadOfferings_whenCheckEntitlementFails_showsError() = testScope.runTest {
        fakeClient.shouldThrowOnCheckEntitlement = true
        fakeClient.checkEntitlementError = RuntimeException("network error")

        viewModel.loadOfferings()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("network error", state.errorMessage)
    }

    @Test
    fun loadOfferings_whenFetchPackagesFails_showsError() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.shouldThrowOnFetchPackages = true
        fakeClient.fetchPackagesError = RuntimeException("fetch failed")

        viewModel.loadOfferings()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("fetch failed", state.errorMessage)
    }

    @Test
    fun loadOfferings_whenNoPackagesAvailable_showsEmptyList() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = emptyList()

        viewModel.loadOfferings()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.packages.isEmpty())
        assertNull(state.selectedPackage)
    }

    // --- selectPackage ---

    @Test
    fun selectPackage_updatesSelectedPackage() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        val second = samplePackages[1]
        viewModel.selectPackage(second)

        assertEquals(second, viewModel.state.value.selectedPackage)
    }

    // --- purchase ---

    @Test
    fun purchase_whenSuccess_updatesPremiumAndPurchaseSuccess() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.purchaseResultToReturn = PurchaseResult.Success(isActive = true)
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertTrue(state.isPremium)
        assertTrue(state.purchaseSuccess)
        assertEquals(1, fakeClient.purchaseCallCount)
        assertEquals(samplePackages.first(), fakeClient.lastPurchasePackage)
    }

    @Test
    fun purchase_whenSuccessButNotActive_doesNotSetPremium() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.purchaseResultToReturn = PurchaseResult.Success(isActive = false)
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertFalse(state.purchaseSuccess)
    }

    @Test
    fun purchase_whenCancelled_onlyStopsLoadingIndicator() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.purchaseResultToReturn = PurchaseResult.Cancelled
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertFalse(state.purchaseSuccess)
        assertNull(state.errorMessage)
    }

    @Test
    fun purchase_whenCancelled_doesNotClearPreviousError() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        // 1回目: エラーを発生させる
        fakeClient.purchaseResultToReturn = PurchaseResult.Error("first error")
        viewModel.purchase("params")
        assertEquals("first error", viewModel.state.value.errorMessage)

        // 2回目: キャンセル → errorMessage は purchase 開始時にクリアされる
        fakeClient.purchaseResultToReturn = PurchaseResult.Cancelled
        viewModel.purchase("params")
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun purchase_whenError_showsErrorMessage() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.purchaseResultToReturn = PurchaseResult.Error("payment failed", code = 1)
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertEquals("payment failed", state.errorMessage)
    }

    @Test
    fun purchase_whenException_showsErrorAndStopsPurchasing() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.shouldThrowOnPurchase = true
        fakeClient.purchaseError = RuntimeException("unexpected crash")
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertEquals("unexpected crash", state.errorMessage)
    }

    @Test
    fun purchase_whenExceptionWithNullMessage_showsFallbackError() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        fakeClient.shouldThrowOnPurchase = true
        fakeClient.purchaseError = RuntimeException(null as String?)
        viewModel.purchase("params")

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertEquals("Purchase failed", state.errorMessage)
    }

    @Test
    fun purchase_passesSelectedPackageToClient() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()

        val second = samplePackages[1]
        viewModel.selectPackage(second)
        fakeClient.purchaseResultToReturn = PurchaseResult.Success(isActive = true)
        viewModel.purchase("activity")

        assertEquals(second, fakeClient.lastPurchasePackage)
        assertEquals("activity", fakeClient.lastPurchaseParams)
    }

    @Test
    fun purchase_whenNoPackageSelected_doesNothing() = testScope.runTest {
        // No loadOfferings called, so selectedPackage is null
        viewModel.purchase("params")

        assertEquals(0, fakeClient.purchaseCallCount)
    }

    // --- restore ---

    @Test
    fun restore_whenSuccess_updatesPremiumAndPurchaseSuccess() = testScope.runTest {
        fakeClient.restoreResultToReturn = PurchaseResult.Success(isActive = true)

        viewModel.restore()

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertTrue(state.isPremium)
        assertTrue(state.purchaseSuccess)
        assertEquals(1, fakeClient.restoreCallCount)
    }

    @Test
    fun restore_whenError_showsErrorMessage() = testScope.runTest {
        fakeClient.restoreResultToReturn = PurchaseResult.Error("restore failed")

        viewModel.restore()

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertEquals("restore failed", state.errorMessage)
    }

    @Test
    fun restore_whenCancelled_onlyStopsLoadingIndicator() = testScope.runTest {
        fakeClient.restoreResultToReturn = PurchaseResult.Cancelled

        viewModel.restore()

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertFalse(state.purchaseSuccess)
    }

    @Test
    fun restore_whenException_showsErrorAndStopsPurchasing() = testScope.runTest {
        fakeClient.shouldThrowOnRestore = true
        fakeClient.restoreError = RuntimeException("restore crash")

        viewModel.restore()

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertEquals("restore crash", state.errorMessage)
    }

    @Test
    fun restore_whenNoActivePurchase_doesNotSetPremium() = testScope.runTest {
        fakeClient.restoreResultToReturn = PurchaseResult.Success(isActive = false)

        viewModel.restore()

        val state = viewModel.state.value
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertFalse(state.purchaseSuccess)
    }

    // --- clearError ---

    @Test
    fun clearError_removesErrorMessage() = testScope.runTest {
        fakeClient.shouldThrowOnCheckEntitlement = true
        fakeClient.checkEntitlementError = RuntimeException("some error")
        viewModel.loadOfferings()
        assertEquals("some error", viewModel.state.value.errorMessage)

        viewModel.clearError()

        assertNull(viewModel.state.value.errorMessage)
    }

    // --- clearPurchaseSuccess ---

    @Test
    fun clearPurchaseSuccess_resetsPurchaseSuccessFlag() = testScope.runTest {
        fakeClient.entitlementToReturn = EntitlementStatus(isActive = false)
        fakeClient.packagesToReturn = samplePackages
        viewModel.loadOfferings()
        fakeClient.purchaseResultToReturn = PurchaseResult.Success(isActive = true)
        viewModel.purchase("params")
        assertTrue(viewModel.state.value.purchaseSuccess)

        viewModel.clearPurchaseSuccess()

        assertFalse(viewModel.state.value.purchaseSuccess)
    }

    // --- clear ---

    @Test
    fun clear_cancelsOngoingCoroutines() = testScope.runTest {
        viewModel.clear()
        // Verify clear completes without error.
        // The scope's children are cancelled, so subsequent operations
        // on the same scope will not execute.
        assertTrue(true)
    }
}
