package io.github.kwmt.revenuecat.paywall

import io.github.kwmt.revenuecat.core.PackageInfo

data class PaywallState(
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isPremium: Boolean = false,
    val packages: List<PackageInfo> = emptyList(),
    val selectedPackage: PackageInfo? = null,
    val errorMessage: String? = null,
    val purchaseSuccess: Boolean = false,
)
