package io.github.kwmt.revenuecat.core

import kotlinx.coroutines.flow.StateFlow

/**
 * RevenueCat操作の共通インターフェース。
 *
 * DI でモック/フェイク注入が可能。
 * インスタンスの生成には [RevenueCatClientFactory.create] を使用する。
 *
 * 使い方:
 * ```
 * val client = RevenueCatClientFactory.create()
 * client.configure(RevenueCatConfig(apiKey = "appl_XXXXX"))
 * val isPremium = client.checkEntitlement()
 * ```
 */
interface RevenueCatClient {

    /** Entitlement ステータスの StateFlow。UI で collect して監視できる。 */
    val entitlementStatus: StateFlow<EntitlementStatus>

    /** キャッシュベースで即座に Entitlement を確認する */
    val isPremium: Boolean

    /** SDK を初期化する。アプリ起動時に1回呼び出す。 */
    fun configure(config: RevenueCatConfig)

    /** ネットワーク経由で Entitlement を確認する。 */
    suspend fun checkEntitlement(): EntitlementStatus

    /** 全 Offering を取得する。 */
    suspend fun fetchOfferings(): List<OfferingInfo>

    /** Current Offering のパッケージ一覧を取得する。 */
    suspend fun fetchCurrentOfferingPackages(): List<PackageInfo>

    /** 購入を実行する。 */
    suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult

    /** リストアを実行する。 */
    suspend fun restore(): PurchaseResult

    /** ユーザー ID でログインする。 */
    suspend fun login(appUserId: String)

    /** ログアウトする。 */
    suspend fun logout()
}
