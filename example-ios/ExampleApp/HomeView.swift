import SwiftUI
import RevenueCatSharedPaywallLogic

struct HomeView: View {
    let client: RevenueCatClient
    @StateObject private var observer: EntitlementStatusObserver

    init(client: RevenueCatClient) {
        self.client = client
        _observer = StateObject(wrappedValue: EntitlementStatusObserver(client: client))
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Spacer()

                Text("Entitlement Status")
                    .font(.title)

                Text(observer.status.isActive ? "Premium: Active" : "Premium: Inactive")
                    .font(.title2)
                    .foregroundColor(observer.status.isActive ? .green : .red)

                Text("Will Renew: \(observer.status.willRenew ? "Yes" : "No")")

                if let millis = observer.status.expirationDateMillis {
                    let date = Date(timeIntervalSince1970: Double(truncating: millis) / 1000.0)
                    Text("Expires: \(date.formatted(date: .abbreviated, time: .shortened))")
                }

                Button("Check Entitlement") {
                    client.checkEntitlement { _, _ in }
                }
                .buttonStyle(.borderedProminent)

                Spacer()
            }
            .padding()
            .navigationTitle("Home")
        }
    }
}
