package io.github.kwmt.revenuecat.paywall

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PaywallStateTest {

    @Test
    fun defaultState_hasCorrectDefaults() {
        val state = PaywallState()
        assertTrue(state.isLoading)
        assertFalse(state.isPurchasing)
        assertFalse(state.isPremium)
        assertTrue(state.packages.isEmpty())
        assertNull(state.selectedPackage)
        assertNull(state.errorMessage)
        assertFalse(state.purchaseSuccess)
    }

    @Test
    fun copy_preservesUnchangedFields() {
        val original = PaywallState(
            isLoading = false,
            isPurchasing = true,
            isPremium = true,
            errorMessage = "error",
            purchaseSuccess = true,
        )
        val copied = original.copy(errorMessage = null)
        assertFalse(copied.isLoading)
        assertTrue(copied.isPurchasing)
        assertTrue(copied.isPremium)
        assertNull(copied.errorMessage)
        assertTrue(copied.purchaseSuccess)
    }
}
