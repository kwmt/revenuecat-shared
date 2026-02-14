import SwiftUI
import RevenueCatSharedPaywallLogic

@main
struct ExampleAppApp: App {
    init() {
        let apiKey = Configuration.revenueCatAPIKey
        guard apiKey != "YOUR_API_KEY_HERE" && !apiKey.isEmpty else {
            print("⚠️ RevenueCat API key is not set. Update Configuration.swift.")
            return
        }
        RevenueCatManager.shared.configure(
            config: RevenueCatConfig(
                apiKey: apiKey,
                entitlementId: "premium",
                debugLogsEnabled: true
            )
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
