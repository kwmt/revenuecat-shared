package io.github.kwmt.revenuecat.core

sealed interface PurchaseResult {
    data class Success(val isActive: Boolean) : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Error(val message: String, val code: Int? = null) : PurchaseResult
}

data class EntitlementStatus(
    val isActive: Boolean,
    val willRenew: Boolean = false,
    val expirationDateMillis: Long? = null,
)

data class OfferingInfo(
    val identifier: String,
    val availablePackages: List<PackageInfo>,
)

data class PackageInfo(
    val identifier: String,
    val localizedPriceString: String,
    val productIdentifier: String,
    /** 内部で購入処理に使うための参照。アプリ側は触らない。 */
    val rcPackage: Any,
)
