package io.github.kwmt.revenuecat.core

/**
 * アプリごとに異なる設定値。
 * 各アプリは起動時にこれを生成して [RevenueCatManager.configure] に渡す。
 */
data class RevenueCatConfig(
    /** RevenueCat API Key (プラットフォームごとに異なる) */
    val apiKey: String,
    /** Entitlement ID。全アプリで統一する場合はデフォルトの "premium" のまま */
    val entitlementId: String = "premium",
    /** デバッグログを有効にするか */
    val debugLogsEnabled: Boolean = false,
)
