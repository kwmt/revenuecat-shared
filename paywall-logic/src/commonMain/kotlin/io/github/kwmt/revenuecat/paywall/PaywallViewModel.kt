package io.github.kwmt.revenuecat.paywall

import io.github.kwmt.revenuecat.core.PackageInfo
import io.github.kwmt.revenuecat.core.PurchaseResult
import io.github.kwmt.revenuecat.core.RevenueCatManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

/**
 * Paywall画面用の共通ViewModel。
 *
 * - Compose: collectAsState で購読
 * - SwiftUI: SKIE 等で StateFlow → Publisher 変換して購読
 */
class PaywallViewModel(
    private val manager: RevenueCatManager = RevenueCatManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    /** iOS (Swift) 向けファクトリ。Kotlin/Native はデフォルト引数をエクスポートしないため。 */
    companion object {
        fun create(): PaywallViewModel = PaywallViewModel()
    }

    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    fun loadOfferings() {
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val entitlement = manager.checkEntitlement()
                if (entitlement.isActive) {
                    _state.update { it.copy(isLoading = false, isPremium = true) }
                    return@launch
                }
                val packages = manager.fetchCurrentOfferingPackages()
                _state.update {
                    it.copy(
                        isLoading = false,
                        packages = packages,
                        selectedPackage = packages.firstOrNull(),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Failed to load")
                }
            }
        }
    }

    fun selectPackage(packageInfo: PackageInfo) {
        _state.update { it.copy(selectedPackage = packageInfo) }
    }

    fun purchase(purchaseParams: Any) {
        val selected = _state.value.selectedPackage ?: return
        scope.launch {
            _state.update { it.copy(isPurchasing = true, errorMessage = null) }
            when (val result = manager.purchase(purchaseParams, selected)) {
                is PurchaseResult.Success -> _state.update {
                    it.copy(isPurchasing = false, isPremium = result.isActive, purchaseSuccess = result.isActive)
                }
                is PurchaseResult.Cancelled -> _state.update {
                    it.copy(isPurchasing = false)
                }
                is PurchaseResult.Error -> _state.update {
                    it.copy(isPurchasing = false, errorMessage = result.message)
                }
            }
        }
    }

    fun restore() {
        scope.launch {
            _state.update { it.copy(isPurchasing = true, errorMessage = null) }
            when (val result = manager.restore()) {
                is PurchaseResult.Success -> _state.update {
                    it.copy(isPurchasing = false, isPremium = result.isActive, purchaseSuccess = result.isActive)
                }
                is PurchaseResult.Error -> _state.update {
                    it.copy(isPurchasing = false, errorMessage = result.message)
                }
                else -> _state.update { it.copy(isPurchasing = false) }
            }
        }
    }

    fun clearError() { _state.update { it.copy(errorMessage = null) } }
    fun clearPurchaseSuccess() { _state.update { it.copy(purchaseSuccess = false) } }

    fun clear() {
        scope.coroutineContext.cancelChildren()
    }
}
