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
    /** パッケージの期間の種類。RevenueCat の標準パッケージ（`$rc_monthly` / `$rc_annual` など）から決まる。 */
    val packageType: PackageKind = PackageKind.UNKNOWN,
    /**
     * 商品に設定されている無料体験の長さ。無料体験が無ければ null。
     *
     * その人が体験を使えるかどうかは含まない。表示する前に [RevenueCatClient.checkTrialEligibility] で確認する
     * （体験を使い終えた人に「無料」と見せないため）。
     * App Store Connect の「1週間」は `TrialPeriod(1, WEEK)` で届く（`7, DAY` ではない）。
     */
    val freeTrial: TrialPeriod? = null,
)

/** パッケージの期間の種類。RevenueCat SDK の `PackageType` と同じ並び。 */
enum class PackageKind {
    UNKNOWN,
    CUSTOM,
    LIFETIME,
    ANNUAL,
    SIX_MONTH,
    THREE_MONTH,
    TWO_MONTH,
    MONTHLY,
    WEEKLY,
}

/** 無料体験の長さ。 */
data class TrialPeriod(
    val value: Int,
    val unit: TrialPeriodUnit,
)

enum class TrialPeriodUnit {
    DAY,
    WEEK,
    MONTH,
    YEAR,
}

/** 無料体験・導入価格を使えるか。 */
enum class TrialEligibility {
    /** 使える。 */
    ELIGIBLE,

    /** 使えない（同じサブスクグループで使ったことがある、または体験が無い）。 */
    INELIGIBLE,

    /** 確認できなかった。体験の無い通常の価格として見せる（RevenueCat の推奨）。 */
    UNKNOWN,
}
