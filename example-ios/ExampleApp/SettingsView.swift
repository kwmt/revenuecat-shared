import SwiftUI
import RevenueCatSharedPaywallLogic

struct SettingsView: View {
    @State private var userId = ""
    @State private var statusMessage = ""

    var body: some View {
        NavigationStack {
            Form {
                Section("User Management") {
                    TextField("User ID", text: $userId)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)

                    Button("Login") {
                        guard !userId.isEmpty else { return }
                        ExampleAppApp.revenueCatClient.login(appUserId: userId) { error in
                            DispatchQueue.main.async {
                                if let error = error {
                                    statusMessage = "Login failed: \(error.localizedDescription)"
                                } else {
                                    statusMessage = "Logged in as: \(userId)"
                                }
                            }
                        }
                    }
                    .disabled(userId.isEmpty)

                    Button("Logout") {
                        ExampleAppApp.revenueCatClient.logout { error in
                            DispatchQueue.main.async {
                                if let error = error {
                                    statusMessage = "Logout failed: \(error.localizedDescription)"
                                } else {
                                    userId = ""
                                    statusMessage = "Logged out"
                                }
                            }
                        }
                    }
                }

                Section("Purchases") {
                    Button("Restore Purchases") {
                        statusMessage = "Restoring..."
                        ExampleAppApp.revenueCatClient.restore { result, error in
                            DispatchQueue.main.async {
                                if let error = error {
                                    statusMessage = "Restore failed: \(error.localizedDescription)"
                                } else if let success = result as? PurchaseResultSuccess {
                                    statusMessage = success.isActive
                                        ? "Restore successful: Premium active"
                                        : "Restore complete: No active entitlements"
                                } else {
                                    statusMessage = "Restore completed"
                                }
                            }
                        }
                    }
                }

                if !statusMessage.isEmpty {
                    Section("Status") {
                        Text(statusMessage)
                            .foregroundColor(.secondary)
                    }
                }
            }
            .navigationTitle("Settings")
        }
    }
}
