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

    /** ネットワーク経由で Entitlement を確認する。確認に失敗したときも `isActive = false` を返す。 */
    suspend fun checkEntitlement(): EntitlementStatus

    /**
     * ネットワーク経由で Entitlement を確認する。確認に失敗したら null を返す。
     *
     * [checkEntitlement] は失敗も `isActive = false` に丸めるので、「未購入」と「確認できなかった」を
     * 区別できない。購読状態を端末に保存するアプリは、確認できなかった回に保存済みの状態を
     * 上書きしないよう、こちらを使う（通信できないだけで購入済みの人を未購入に戻さないため）。
     *
     * 成功したときは [entitlementStatus] も更新する。
     */
    suspend fun checkEntitlementOrNull(): EntitlementStatus? = checkEntitlement()

    /** 全 Offering を取得する。 */
    suspend fun fetchOfferings(): List<OfferingInfo>

    /** Current Offering のパッケージ一覧を取得する。 */
    suspend fun fetchCurrentOfferingPackages(): List<PackageInfo>

    /**
     * 無料体験・導入価格を使えるかを、パッケージごとに確認する。キーは [PackageInfo.productIdentifier]。
     *
     * - iOS: StoreKit に問い合わせる。同じサブスクグループで体験を使ったことがあれば [TrialEligibility.INELIGIBLE]
     * - Android: RevenueCat SDK が対応しておらず、常に [TrialEligibility.UNKNOWN] を返す
     *
     * 渡したパッケージはすべてキーに含まれる。確認できなかったものは [TrialEligibility.UNKNOWN]。
     */
    suspend fun checkTrialEligibility(packages: List<PackageInfo>): Map<String, TrialEligibility> =
        packages.associate { it.productIdentifier to TrialEligibility.UNKNOWN }

    /** 購入を実行する。 */
    suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult

    /** リストアを実行する。 */
    suspend fun restore(): PurchaseResult

    /** ユーザー ID でログインする。 */
    suspend fun login(appUserId: String)

    /** ログアウトする。 */
    suspend fun logout()
}
