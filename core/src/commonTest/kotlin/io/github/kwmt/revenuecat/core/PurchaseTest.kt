package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesErrorCode
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * purchase 処理における例外ハンドリングのテスト。
 *
 * RevenueCatClientImpl.purchase() 内で発生する例外が
 * 正しく PurchaseResult にマッピングされることを検証する。
 */
class PurchaseTest {

    // --- キャンセル検出 ---

    @Test
    fun purchase_whenUserCancelled_returnsCancelled() {
        val exception = PurchasesTransactionException(
            purchasesError = PurchasesError(code = PurchasesErrorCode.PurchaseCancelledError),
            userCancelled = true,
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Cancelled>(result)
    }

    @Test
    fun purchase_whenTransactionError_notCancelled_returnsErrorWithCode() {
        val exception = PurchasesTransactionException(
            purchasesError = PurchasesError(
                code = PurchasesErrorCode.StoreProblemError,
                underlyingErrorMessage = "Store unavailable",
            ),
            userCancelled = false,
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals(PurchasesErrorCode.StoreProblemError.code, result.code)
    }

    // --- SDK エラー ---

    @Test
    fun purchase_whenNetworkError_returnsErrorWithCode() {
        val exception = PurchasesException(
            PurchasesError(
                code = PurchasesErrorCode.NetworkError,
                underlyingErrorMessage = "Connection timeout",
            )
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals(PurchasesErrorCode.NetworkError.code, result.code)
    }

    @Test
    fun purchase_whenSdkError_preservesMessage() {
        val exception = PurchasesException(
            PurchasesError(
                code = PurchasesErrorCode.ProductAlreadyPurchasedError,
                underlyingErrorMessage = "Already purchased",
            )
        )

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals(exception.message, result.message)
    }

    // --- 予期しない例外 ---

    @Test
    fun purchase_whenUnexpectedException_returnsErrorWithMessage() {
        val exception = RuntimeException("unexpected error")

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals("unexpected error", result.message)
        assertNull(result.code)
    }

    @Test
    fun purchase_whenExceptionWithNullMessage_returnsUnknownError() {
        val exception = RuntimeException(null as String?)

        val result = mapPurchaseException(exception)

        assertIs<PurchaseResult.Error>(result)
        assertEquals("Unknown error", result.message)
        assertNull(result.code)
    }
}
