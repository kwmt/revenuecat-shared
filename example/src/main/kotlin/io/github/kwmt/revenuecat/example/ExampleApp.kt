package io.github.kwmt.revenuecat.example

import android.app.Application
import android.util.Log
import io.github.kwmt.revenuecat.core.RevenueCatConfig
import io.github.kwmt.revenuecat.core.RevenueCatManager

class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isNotBlank()) {
            RevenueCatManager.configure(
                RevenueCatConfig(
                    apiKey = apiKey,
                    debugLogsEnabled = BuildConfig.DEBUG,
                )
            )
        } else {
            Log.w("ExampleApp", "RevenueCat API key is not set. Add revenuecat.apiKey to local.properties.")
        }
    }
}
