package io.github.kwmt.revenuecat.core

import kotlin.test.Test
import kotlin.test.assertNull

class AppUserIdTest {

    // ★configure の前に SDK へ触ると例外で落ちる。起動の途中で ID を読むアプリがあっても落とさない
    @Test
    fun appUserIdIsNullBeforeConfigure() {
        assertNull(RevenueCatClientImpl().appUserIdOrNull())
    }
}
