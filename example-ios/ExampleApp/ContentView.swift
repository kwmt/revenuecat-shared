import SwiftUI
import RevenueCatSharedPaywallLogic

struct ContentView: View {
    let client: RevenueCatClient

    var body: some View {
        TabView {
            HomeView(client: client)
                .tabItem {
                    Label("Home", systemImage: "house")
                }

            PaywallView(client: client)
                .tabItem {
                    Label("Paywall", systemImage: "cart")
                }

            SettingsView(client: client)
                .tabItem {
                    Label("Settings", systemImage: "gear")
                }
        }
    }
}
