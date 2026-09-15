package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.models.DiscountPaymentMode
import com.revenuecat.purchases.kmp.models.IntroEligibilityStatus
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.freePhase

/** RevenueCat SDK の [Package] をアプリに渡す [PackageInfo] にする。 */
internal fun Package.toPackageInfo(): PackageInfo = PackageInfo(
    identifier = identifier,
    localizedPriceString = storeProduct.price.formatted,
    productIdentifier = storeProduct.id,
    rcPackage = this,
    packageType = packageType.toPackageKind(),
    freeTrial = storeProduct.freeTrial(),
)

internal fun PackageType.toPackageKind(): PackageKind = when (this) {
    PackageType.UNKNOWN -> PackageKind.UNKNOWN
    PackageType.CUSTOM -> PackageKind.CUSTOM
    PackageType.LIFETIME -> PackageKind.LIFETIME
    PackageType.ANNUAL -> PackageKind.ANNUAL
    PackageType.SIX_MONTH -> PackageKind.SIX_MONTH
    PackageType.THREE_MONTH -> PackageKind.THREE_MONTH
    PackageType.TWO_MONTH -> PackageKind.TWO_MONTH
    PackageType.MONTHLY -> PackageKind.MONTHLY
    PackageType.WEEKLY -> PackageKind.WEEKLY
}

private fun StoreProduct.freeTrial(): TrialPeriod? = freeTrialOf(
    introductoryPaymentMode = introductoryDiscount?.paymentMode,
    introductoryPeriod = introductoryDiscount?.subscriptionPeriod,
    freePhasePeriod = subscriptionOptions?.freeTrial?.freePhase?.billingPeriod,
)

/**
 * 無料体験の長さを取り出す。
 *
 * 置き場所がストアで違う:
 * - App Store: `introductoryDiscount`。導入価格は無料体験のほかに「割引価格で支払う」形もあるので、
 *   支払い方が [DiscountPaymentMode.FREE_TRIAL] のときだけ無料体験として扱う
 * - Google Play: `subscriptionOptions.freeTrial` の無料の期間（`introductoryDiscount` は常に null）
 */
internal fun freeTrialOf(
    introductoryPaymentMode: DiscountPaymentMode?,
    introductoryPeriod: Period?,
    freePhasePeriod: Period?,
): TrialPeriod? {
    val period = if (introductoryPaymentMode == DiscountPaymentMode.FREE_TRIAL) introductoryPeriod else freePhasePeriod
    return period?.toTrialPeriod()
}

/** 長さが読めない期間（単位が不明・0 以下）は、体験が無いものとして扱う（「0日間無料」と見せない）。 */
internal fun Period.toTrialPeriod(): TrialPeriod? {
    if (value <= 0) return null
    val trialUnit = when (unit) {
        PeriodUnit.DAY -> TrialPeriodUnit.DAY
        PeriodUnit.WEEK -> TrialPeriodUnit.WEEK
        PeriodUnit.MONTH -> TrialPeriodUnit.MONTH
        PeriodUnit.YEAR -> TrialPeriodUnit.YEAR
        PeriodUnit.UNKNOWN -> return null
    }
    return TrialPeriod(value = value, unit = trialUnit)
}

/** 体験が存在しない（[IntroEligibilityStatus.NO_INTRO_OFFER_EXISTS]）ものは「使えない」に含める。 */
internal fun IntroEligibilityStatus.toTrialEligibility(): TrialEligibility = when (this) {
    IntroEligibilityStatus.ELIGIBLE -> TrialEligibility.ELIGIBLE
    IntroEligibilityStatus.INELIGIBLE,
    IntroEligibilityStatus.NO_INTRO_OFFER_EXISTS,
    -> TrialEligibility.INELIGIBLE
    IntroEligibilityStatus.UNKNOWN -> TrialEligibility.UNKNOWN
}

/**
 * 無料体験を使えるかを決める。
 *
 * - Google Play の定期購入（[googlePlayFreeTrialOffered] が null でない）: **SDK の答えを使わない**。RevenueCat SDK は
 *   Android で常に UNKNOWN を返す。Play Billing が返す特典（`ProductDetails.subscriptionOfferDetails`）は
 *   **その人が使えるものだけ**なので、無料体験の特典が届いていれば [TrialEligibility.ELIGIBLE]、
 *   届いていなければ [TrialEligibility.INELIGIBLE]（体験を使い終えた人には特典が届かない）
 * - それ以外（App Store）: SDK の答えのまま。答えが無ければ [TrialEligibility.UNKNOWN]
 */
internal fun trialEligibilityOf(sdkAnswer: TrialEligibility?, googlePlayFreeTrialOffered: Boolean?): TrialEligibility =
    when (googlePlayFreeTrialOffered) {
        true -> TrialEligibility.ELIGIBLE
        false -> TrialEligibility.INELIGIBLE
        null -> sdkAnswer ?: TrialEligibility.UNKNOWN
    }

/**
 * Google Play の定期購入なら、その人に無料体験の特典が届いているか。Google Play の定期購入でなければ null。
 *
 * `subscriptionOptions` は Play Store の定期購入にしか入らない（App Store・Amazon・買い切りでは null）ので、
 * これでストアを見分ける。
 */
internal fun StoreProduct.googlePlayFreeTrialOffered(): Boolean? = subscriptionOptions?.let { it.freeTrial != null }
