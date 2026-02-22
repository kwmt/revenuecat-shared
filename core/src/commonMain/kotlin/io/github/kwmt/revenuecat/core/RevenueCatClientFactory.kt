package io.github.kwmt.revenuecat.core

/**
 * [RevenueCatClient] のファクトリ。
 *
 * - Kotlin: `RevenueCatClientFactory.create()`
 * - Swift:  `RevenueCatClientFactory.shared.create()`
 */
object RevenueCatClientFactory {
    fun create(): RevenueCatClient = RevenueCatClientImpl()
}
