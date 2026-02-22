package io.github.kwmt.revenuecat.core

import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException

/**
 * 購入処理の例外を [PurchaseResult] にマッピングする。
 *
 * - [PurchasesTransactionException] で `userCancelled` が true → [PurchaseResult.Cancelled]
 * - [PurchasesException] → [PurchaseResult.Error]（エラーコード付き）
 * - その他の例外 → [PurchaseResult.Error]
 */
internal fun mapPurchaseException(e: Exception): PurchaseResult = when {
    e is PurchasesTransactionException && e.userCancelled -> PurchaseResult.Cancelled
    e is PurchasesException -> PurchaseResult.Error(
        message = e.message ?: "Unknown error",
        code = e.code.code,
    )
    else -> PurchaseResult.Error(message = e.message ?: "Unknown error")
}
