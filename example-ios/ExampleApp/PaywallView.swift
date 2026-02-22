import SwiftUI
import RevenueCatSharedPaywallLogic

struct PaywallView: View {
    @StateObject private var observer: PaywallStateObserver
    @State private var showError = false

    init(client: RevenueCatClient) {
        _observer = StateObject(wrappedValue: PaywallStateObserver(client: client))
    }

    var body: some View {
        NavigationStack {
            Group {
                if observer.state.isLoading {
                    ProgressView("Loading...")
                } else if observer.state.isPremium {
                    VStack(spacing: 16) {
                        Spacer()
                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.green)
                        Text("You are Premium!")
                            .font(.title)
                            .foregroundColor(.green)
                        Spacer()
                    }
                } else {
                    packageListView
                }
            }
            .navigationTitle("Paywall")
            .onAppear {
                observer.viewModel.loadOfferings()
            }
            .alert("Error", isPresented: $showError, actions: {
                Button("OK") {
                    observer.viewModel.clearError()
                }
            }, message: {
                Text(observer.state.errorMessage ?? "Unknown error")
            })
            .onChange(of: observer.state.errorMessage) { newValue in
                showError = newValue != nil
            }
        }
    }

    private var packageListView: some View {
        VStack(spacing: 0) {
            List {
                Section("Choose a Plan") {
                    ForEach(observer.state.packages, id: \.identifier) { pkg in
                        packageRow(pkg)
                    }
                }
            }
            .listStyle(.insetGrouped)

            VStack(spacing: 12) {
                Button {
                    observer.viewModel.purchase(purchaseParams: NSObject())
                } label: {
                    if observer.state.isPurchasing {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                    } else {
                        Text("Subscribe")
                            .frame(maxWidth: .infinity)
                    }
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)
                .disabled(observer.state.selectedPackage == nil || observer.state.isPurchasing)

                Button("Restore Purchases") {
                    observer.viewModel.restore()
                }
                .disabled(observer.state.isPurchasing)
            }
            .padding()
        }
    }

    private func packageRow(_ pkg: PackageInfo) -> some View {
        let isSelected = observer.state.selectedPackage?.identifier == pkg.identifier
        return Button {
            observer.viewModel.selectPackage(packageInfo: pkg)
        } label: {
            HStack {
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(isSelected ? .accentColor : .secondary)
                VStack(alignment: .leading) {
                    Text(pkg.identifier)
                        .font(.headline)
                    Text(pkg.productIdentifier)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Spacer()
                Text(pkg.localizedPriceString)
                    .font(.headline)
                    .foregroundColor(.accentColor)
            }
        }
        .listRowBackground(isSelected ? Color.accentColor.opacity(0.1) : nil)
    }
}
