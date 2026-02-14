package com.shared.revenuecat.paywall

import com.shared.revenuecat.core.PackageInfo

data class PaywallState(
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val isPremium: Boolean = false,
    val packages: List<PackageInfo> = emptyList(),
    val selectedPackage: PackageInfo? = null,
    val errorMessage: String? = null,
    val purchaseSuccess: Boolean = false,
)
