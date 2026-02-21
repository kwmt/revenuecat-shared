package io.github.kwmt.revenuecat.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * iOS (Swift) 側から StateFlow を監視するためのヘルパー。
 *
 * SKIEやKMP-NativeCoroutinesを使わずに、コールバックベースで
 * StateFlowの値変化を受け取れるようにする。
 *
 * 使い方 (Swift):
 * ```swift
 * let closeable = FlowHelper.shared.observeEntitlementStatus { status in
 *     self.isActive = status.isActive
 * }
 * // 不要になったら
 * closeable.close()
 * ```
 */
object FlowHelper {

    /**
     * [RevenueCatManager.entitlementStatus] を監視し、値が変化するたびに [onEach] を呼び出す。
     * 返却された [Closeable] を close() すると監視が停止する。
     */
    fun observeEntitlementStatus(onEach: (EntitlementStatus) -> Unit): Closeable {
        return observeStateFlow(RevenueCatManager.entitlementStatus, onEach)
    }

    /**
     * 任意の [StateFlow] を監視する汎用ヘルパー。
     * 返却された [Closeable] を close() すると監視が停止する。
     */
    fun <T> observeStateFlow(flow: StateFlow<T>, onEach: (T) -> Unit): Closeable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        flow.onEach { value -> onEach(value) }.launchIn(scope)
        return object : Closeable {
            override fun close() {
                scope.cancel()
            }
        }
    }
}

/**
 * リソース解放用のインターフェース。
 * Kotlin/Native では `platform.Foundation.NSObject` 等に変換される。
 */
interface Closeable {
    fun close()
}
