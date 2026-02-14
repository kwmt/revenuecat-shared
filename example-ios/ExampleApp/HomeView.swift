import SwiftUI
import RevenueCatSharedPaywallLogic

struct HomeView: View {
    @StateObject private var observer = EntitlementStatusObserver()

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
                    RevenueCatManager.shared.checkEntitlement { _, _ in }
                }
                .buttonStyle(.borderedProminent)

                Spacer()
            }
            .padding()
            .navigationTitle("Home")
        }
    }
}
