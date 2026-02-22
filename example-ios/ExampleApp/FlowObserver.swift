import Foundation
import RevenueCatSharedPaywallLogic

/// RevenueCatClient.entitlementStatus (KMP StateFlow) を SwiftUI で監視するための ObservableObject。
/// FlowHelper 経由でコールバックを受け取り、@Published プロパティを更新する。
class EntitlementStatusObserver: ObservableObject {
    @Published var status: EntitlementStatus

    private var closeable: (any Closeable)?

    init() {
        self.status = EntitlementStatus(isActive: false, willRenew: false, expirationDateMillis: nil)
        self.closeable = FlowHelper.shared.observeEntitlementStatus(client: ExampleAppApp.revenueCatClient) { [weak self] status in
            DispatchQueue.main.async {
                self?.status = status
            }
        }
    }

    deinit {
        closeable?.close()
    }
}

/// PaywallViewModel.state (KMP StateFlow) を SwiftUI で監視するための ObservableObject。
class PaywallStateObserver: ObservableObject {
    @Published var state: PaywallState
    let viewModel: PaywallViewModel

    private var closeable: (any Closeable)?

    init() {
        let vm = PaywallViewModel.companion.create(client: ExampleAppApp.revenueCatClient)
        self.viewModel = vm
        self.state = (vm.state.value as? PaywallState) ?? PaywallState(
            isLoading: true, isPurchasing: false, isPremium: false,
            packages: [], selectedPackage: nil, errorMessage: nil, purchaseSuccess: false
        )
        self.closeable = FlowHelper.shared.observeStateFlow(flow: vm.state) { [weak self] state in
            guard let state = state as? PaywallState else { return }
            DispatchQueue.main.async {
                self?.state = state
            }
        }
    }

    deinit {
        closeable?.close()
        viewModel.clear()
    }
}
