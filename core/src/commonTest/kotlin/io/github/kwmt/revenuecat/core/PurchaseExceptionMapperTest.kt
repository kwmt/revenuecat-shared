package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class PurchaseExceptionMapperTest {

    // --- PurchasesTransactionException (userCancelled = true) ---

    @Test
    fun whenUserCancelled_returnsCancelled() {
        val error = PurchasesError(
            code = PurchasesErrorCode.PurchaseCancelledError,
        )
        val exception = PurchasesTransactionException(
            purchasesError = error,
            userCancelled = true,
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Cancelled>(result)
    }

    // --- PurchasesTransactionException (userCancelled = false) ---

    @Test
    fun whenTransactionException_notCancelled_returnsErrorWithCode() {
        val error = PurchasesError(
            code = PurchasesErrorCode.StoreProblemError,
            underlyingErrorMessage = "Store unavailable",
        )
        val exception = PurchasesTransactionException(
            purchasesError = error,
            userCancelled = false,
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals(PurchasesErrorCode.StoreProblemError.code, result.code)
    }

    // --- PurchasesException ---

    @Test
    fun whenPurchasesException_returnsErrorWithCode() {
        val error = PurchasesError(
            code = PurchasesErrorCode.NetworkError,
            underlyingErrorMessage = "Connection timeout",
        )
        val exception = PurchasesException(error)

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals(PurchasesErrorCode.NetworkError.code, result.code)
    }

    @Test
    fun whenPurchasesException_messageIsPreserved() {
        val error = PurchasesError(
            code = PurchasesErrorCode.ProductAlreadyPurchasedError,
            underlyingErrorMessage = "Already purchased",
        )
        val exception = PurchasesException(error)

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        // PurchasesException.message comes from the error
        assertEquals(exception.message, result.message)
    }

    // --- Generic Exception ---

    @Test
    fun whenGenericException_returnsErrorWithMessage() {
        val exception = RuntimeException("unexpected error")

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals("unexpected error", result.message)
        assertNull(result.code)
    }

    @Test
    fun whenGenericException_nullMessage_returnsUnknownError() {
        val exception = RuntimeException(null as String?)

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals("Unknown error", result.message)
        assertNull(result.code)
    }
}
