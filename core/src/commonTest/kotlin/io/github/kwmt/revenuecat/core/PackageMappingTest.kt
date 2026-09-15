package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.models.DiscountPaymentMode
import com.revenuecat.purchases.kmp.models.IntroEligibilityStatus
import com.revenuecat.purchases.kmp.models.PackageType
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PackageMappingTest {

    @Test
    fun appStoreFreeTrialIsReadFromIntroductoryDiscount() {
        val trial = freeTrialOf(
            introductoryPaymentMode = DiscountPaymentMode.FREE_TRIAL,
            introductoryPeriod = Period(value = 1, unit = PeriodUnit.WEEK),
            freePhasePeriod = null,
        )

        assertEquals(TrialPeriod(value = 1, unit = TrialPeriodUnit.WEEK), trial)
    }

    @Test
    fun paidIntroductoryPriceIsNotAFreeTrial() {
        val trial = freeTrialOf(
            introductoryPaymentMode = DiscountPaymentMode.PAY_AS_YOU_GO,
            introductoryPeriod = Period(value = 3, unit = PeriodUnit.MONTH),
            freePhasePeriod = null,
        )

        assertNull(trial)
    }

    @Test
    fun googlePlayFreeTrialIsReadFromFreePhase() {
        val trial = freeTrialOf(
            introductoryPaymentMode = null,
            introductoryPeriod = null,
            freePhasePeriod = Period(value = 7, unit = PeriodUnit.DAY),
        )

        assertEquals(TrialPeriod(value = 7, unit = TrialPeriodUnit.DAY), trial)
    }

    @Test
    fun productWithoutTrialHasNoFreeTrial() {
        assertNull(freeTrialOf(introductoryPaymentMode = null, introductoryPeriod = null, freePhasePeriod = null))
    }

    @Test
    fun unreadablePeriodIsNotShownAsATrial() {
        assertNull(Period(value = 1, unit = PeriodUnit.UNKNOWN).toTrialPeriod())
        assertNull(Period(value = 0, unit = PeriodUnit.DAY).toTrialPeriod())
    }

    @Test
    fun standardPackageTypesKeepTheirKind() {
        assertEquals(PackageKind.MONTHLY, PackageType.MONTHLY.toPackageKind())
        assertEquals(PackageKind.ANNUAL, PackageType.ANNUAL.toPackageKind())
        assertEquals(PackageKind.CUSTOM, PackageType.CUSTOM.toPackageKind())
    }

    /** Google Play: 無料体験の特典が届いていれば使える（SDK は Android で常に UNKNOWN を返すが、それに引きずられない）。 */
    @Test
    fun googlePlayFreeTrialOfferMeansTheTrialCanBeUsed() {
        assertEquals(TrialEligibility.ELIGIBLE, trialEligibilityOf(TrialEligibility.UNKNOWN, googlePlayFreeTrialOffered = true))
        assertEquals(TrialEligibility.ELIGIBLE, trialEligibilityOf(null, googlePlayFreeTrialOffered = true))
    }

    /** Google Play: 特典が届いていなければ使えない（体験を使い終えた人には Play が特典を返さない）。 */
    @Test
    fun googlePlayWithoutFreeTrialOfferMeansTheTrialCannotBeUsed() {
        assertEquals(TrialEligibility.INELIGIBLE, trialEligibilityOf(TrialEligibility.UNKNOWN, googlePlayFreeTrialOffered = false))
        assertEquals(TrialEligibility.INELIGIBLE, trialEligibilityOf(null, googlePlayFreeTrialOffered = false))
    }

    /** App Store: SDK の答えのまま（答えが無ければ UNKNOWN）。 */
    @Test
    fun appStoreKeepsTheSdkAnswer() {
        assertEquals(TrialEligibility.ELIGIBLE, trialEligibilityOf(TrialEligibility.ELIGIBLE, googlePlayFreeTrialOffered = null))
        assertEquals(TrialEligibility.INELIGIBLE, trialEligibilityOf(TrialEligibility.INELIGIBLE, googlePlayFreeTrialOffered = null))
        assertEquals(TrialEligibility.UNKNOWN, trialEligibilityOf(null, googlePlayFreeTrialOffered = null))
    }

    @Test
    fun onlyEligibleStatusMeansTheTrialCanBeUsed() {
        assertEquals(TrialEligibility.ELIGIBLE, IntroEligibilityStatus.ELIGIBLE.toTrialEligibility())
        assertEquals(TrialEligibility.INELIGIBLE, IntroEligibilityStatus.INELIGIBLE.toTrialEligibility())
        assertEquals(TrialEligibility.INELIGIBLE, IntroEligibilityStatus.NO_INTRO_OFFER_EXISTS.toTrialEligibility())
        assertEquals(TrialEligibility.UNKNOWN, IntroEligibilityStatus.UNKNOWN.toTrialEligibility())
    }
}
