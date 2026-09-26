package io.github.kwmt.revenuecat.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlanChangeTest {

    @Test
    fun playSubscriptionOnAnotherProductIsChangedFromWithItsBasePlan() {
        val old = playPlanChangeOldProductIdOf(
            activeOnPlayStore = true,
            activeProductIdentifier = "lite_monthly",
            activeProductPlanIdentifier = "monthly",
            targetProductIdentifier = "standard_monthly:monthly",
        )

        assertEquals("lite_monthly:monthly", old)
    }

    @Test
    fun productIdThatAlreadyHasBasePlanIsKeptAsIs() {
        val old = playPlanChangeOldProductIdOf(
            activeOnPlayStore = true,
            activeProductIdentifier = "lite_annual:annual",
            activeProductPlanIdentifier = "annual",
            targetProductIdentifier = "standard_annual:annual",
        )

        assertEquals("lite_annual:annual", old)
    }

    @Test
    fun sameProductWithAnotherBasePlanIsAnOrdinaryPurchase() {
        assertNull(
            playPlanChangeOldProductIdOf(
                activeOnPlayStore = true,
                activeProductIdentifier = "standard",
                activeProductPlanIdentifier = "monthly",
                targetProductIdentifier = "standard:annual",
            ),
        )
    }

    @Test
    fun noPlaySubscriptionIsAnOrdinaryPurchase() {
        // App Store の購読・購読が無いときは乗り換えを指定しない（App Store は同じグループなら Apple が乗り換えにする）
        assertNull(playPlanChangeOldProductIdOf(false, "lite_monthly", null, "standard_monthly"))
        assertNull(playPlanChangeOldProductIdOf(true, null, null, "standard_monthly:monthly"))
        assertNull(playPlanChangeOldProductIdOf(true, "", "monthly", "standard_monthly:monthly"))
    }
}
