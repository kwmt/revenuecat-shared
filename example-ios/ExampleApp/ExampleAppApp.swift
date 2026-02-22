import SwiftUI
import RevenueCatSharedPaywallLogic

@main
struct ExampleAppApp: App {
    static var revenueCatClient: RevenueCatClient!

    init() {
        let apiKey = Configuration.revenueCatAPIKey
        guard apiKey != "YOUR_API_KEY_HERE" && !apiKey.isEmpty else {
            print("⚠️ RevenueCat API key is not set. Update Configuration.swift.")
            return
        }
        let client = RevenueCatClientFactory.shared.create()
        client.configure(
            config: RevenueCatConfig(
                apiKey: apiKey,
                entitlementId: "premium",
                debugLogsEnabled: true
            )
        )
        ExampleAppApp.revenueCatClient = client
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
