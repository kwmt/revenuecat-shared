package io.github.kwmt.revenuecat.example

import android.app.Application
import android.util.Log
import io.github.kwmt.revenuecat.core.RevenueCatClient
import io.github.kwmt.revenuecat.core.RevenueCatClientFactory
import io.github.kwmt.revenuecat.core.RevenueCatConfig

class ExampleApp : Application() {
    companion object {
        lateinit var revenueCatClient: RevenueCatClient
    }

    override fun onCreate() {
        super.onCreate()
        revenueCatClient = RevenueCatClientFactory.create()
        val apiKey = BuildConfig.REVENUECAT_API_KEY
        if (apiKey.isNotBlank()) {
            revenueCatClient.configure(
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
