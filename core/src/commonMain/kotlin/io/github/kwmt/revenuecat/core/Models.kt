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
    /**
     * 値段の数（通貨の100万分の1の単位。¥500 なら 500_000_000）。取れなければ null。
     *
     * 表示用の [localizedPriceString] では計算できない（年額の割引率などを出すときに使う）。
     */
    val priceAmountMicros: Long? = null,
    /**
     * 1か月あたりの値段の表示（ストアの通貨の書き方のまま）。月額より長い期間の商品にだけ入る。取れなければ null。
     */
    val pricePerMonthString: String? = null,
)

/**
 * Google Play で、有効な定期購入から別の商品へ乗り換えるときの切り替え方。RevenueCat SDK の `GoogleReplacementMode` と同じ名前。
 *
 * App Store は同じサブスクリプショングループの中なら Apple が乗り換えにするので、この指定は Google Play だけで効く。
 */
enum class PlanChangeMode {
    /** すぐ切り替え、残りの期間の差額を請求する（上位のプランへの乗り換えだけに使える）。 */
    CHARGE_PRORATED_PRICE,

    /** すぐ切り替え、新しい商品の満額を請求する（残りの期間は新しい商品の期間に足す）。 */
    CHARGE_FULL_PRICE,

    /** すぐ切り替え、残りの期間を新しい商品の期間に換算する（請求は次の更新日から）。 */
    WITH_TIME_PRORATION,

    /** すぐ切り替え、次の更新日から新しい値段で請求する。 */
    WITHOUT_PRORATION,

    /** 今の期間が終わってから切り替える。 */
    DEFERRED,
}

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
