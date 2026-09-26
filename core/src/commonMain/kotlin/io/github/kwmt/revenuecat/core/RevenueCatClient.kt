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
     *
     * **既定の実装は [checkEntitlement] に委ねるだけで、失敗を区別しない（null を返さない）。**
     * 既存の実装（テスト用の Fake など）を壊さないための既定値で、この約束は満たさない。
     * [RevenueCatClient] を実装・ラップするクラスは、失敗を区別できるなら必ず override すること
     * （override し忘れたラッパーを通すと、確認できなかった回に「未購入」が返り、保存済みの購読状態を消してしまう）。
     */
    suspend fun checkEntitlementOrNull(): EntitlementStatus? = checkEntitlement()

    /**
     * [RevenueCatConfig.entitlementId] 以外の Entitlement を、ネットワーク経由で確認する。確認に失敗したら null を返す。
     *
     * 段階のあるプラン（上位のプランだけに付ける Entitlement がある）を売るアプリが、2つ目以降の Entitlement を読むために使う。
     * [entitlementStatus] は更新しない（あちらは [RevenueCatConfig.entitlementId] だけを表す）。
     *
     * **既定の実装は null（確認できなかった）を返す。** 既存の実装（テスト用の Fake など）を壊さないための既定値。
     */
    suspend fun checkEntitlementOrNull(entitlementId: String): EntitlementStatus? = null

    /** 全 Offering を取得する。 */
    suspend fun fetchOfferings(): List<OfferingInfo>

    /** Current Offering のパッケージ一覧を取得する。 */
    suspend fun fetchCurrentOfferingPackages(): List<PackageInfo>

    /**
     * 無料体験・導入価格を使えるかを、パッケージごとに確認する。キーは [PackageInfo.productIdentifier]。
     *
     * - iOS: StoreKit に問い合わせる。同じサブスクグループで体験を使ったことがあれば [TrialEligibility.INELIGIBLE]
     * - Android: Google Play が返す特典は**その人が使えるものだけ**なので、無料体験の特典が届いていれば
     *   [TrialEligibility.ELIGIBLE]、届いていなければ [TrialEligibility.INELIGIBLE]
     *   （RevenueCat SDK の判定は Android で常に UNKNOWN なので使わない）
     *
     * 渡したパッケージはすべてキーに含まれる。確認できなかったものは [TrialEligibility.UNKNOWN]。
     */
    suspend fun checkTrialEligibility(packages: List<PackageInfo>): Map<String, TrialEligibility> =
        packages.associate { it.productIdentifier to TrialEligibility.UNKNOWN }

    /** 購入を実行する。 */
    suspend fun purchase(purchaseParams: Any, packageInfo: PackageInfo): PurchaseResult

    /**
     * 購入を実行する。Google Play で [RevenueCatConfig.entitlementId] の有効な定期購入が**別の商品**にあれば、
     * そこからの乗り換えとして買う（[mode] の切り替え方で）。
     *
     * Google Play は乗り換えを明示しないと、2つの定期購入が並んで両方に請求される。App Store は同じサブスクリプショングループの中なら
     * Apple が乗り換えにするので、ふつうの購入と同じ。乗り換えが要らない（有効な購読が無い・同じ商品・App Store）ときも、ふつうの購入をする。
     *
     * **既定の実装は [purchase] に委ねるだけ（乗り換えを指定しない）。** 既存の実装を壊さないための既定値。
     */
    suspend fun purchaseChangingPlan(
        packageInfo: PackageInfo,
        mode: PlanChangeMode = PlanChangeMode.CHARGE_PRORATED_PRICE,
    ): PurchaseResult = purchase(Unit, packageInfo)

    /** リストアを実行する。 */
    suspend fun restore(): PurchaseResult

    /** ユーザー ID でログインする。 */
    suspend fun login(appUserId: String)

    /** ログアウトする。 */
    suspend fun logout()
}
