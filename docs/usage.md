# Usage

English | [日本語](ja/usage.md)

## Setup (at app startup)

```kotlin
// Android: e.g. in Application.onCreate()
val client = RevenueCatClientFactory.create()
client.configure(
    RevenueCatConfig(
        apiKey = "goog_XXXXX",  // or "appl_XXXXX"
        entitlementId = "premium",
        debugLogsEnabled = BuildConfig.DEBUG,
    )
)
```

## Checking entitlements

```kotlin
// Check with a suspend call (hits the network)
val status = client.checkEntitlement()
if (status.isActive) { /* unlock premium features */ }

// Check the cached value instantly
if (client.isPremium) { /* ... */ }

// Observe as a StateFlow
client.entitlementStatus.collect { status ->
    // update the UI
}

// Returns null when the check fails (checkEntitlement rounds a failure to isActive = false).
// Apps that persist membership on the device should use this so a failed check never overwrites the saved state.
client.checkEntitlementOrNull()?.let { status -> saveMember(status.isActive) }
```

## Showing a free trial

```kotlin
val packages = client.fetchCurrentOfferingPackages()
val annual = packages.first { it.packageType == PackageKind.ANNUAL }

// The trial length configured on the product ("1 week" in App Store Connect is TrialPeriod(1, WEEK))
val trial = annual.freeTrial

// Whether this user can use the trial. Show "free" only when it is ELIGIBLE.
// (On Android it is decided by the offers Google Play returns: ELIGIBLE if a free-trial offer is present, INELIGIBLE otherwise.
//  RevenueCat recommends showing the regular price for UNKNOWN.)
val eligibility = client.checkTrialEligibility(packages)
val showTrial = trial != null && eligibility[annual.productIdentifier] == TrialEligibility.ELIGIBLE
```

## Tiered plans (two or more entitlements)

```kotlin
// Check an entitlement other than the configured entitlementId (null when the check fails)
val explain = client.checkEntitlementOrNull("explain")?.isActive

// The price as a number (for computing discounts) and the per-month price of an annual plan
val annual = packages.first { it.identifier == "standard_annual" }
val micros = annual.priceAmountMicros          // ¥4,000 → 4_000_000_000
val perMonth = annual.pricePerMonthString      // e.g. "¥333"

// Purchase that moves to a higher plan. On Google Play, if an active subscription exists for another product,
// it is bought as a plan change (otherwise the user ends up with two subscriptions and is charged for both).
// On the App Store, or with no active subscription, this is the same as a normal purchase.
val result = client.purchaseChangingPlan(annual, PlanChangeMode.CHARGE_PRORATED_PRICE)
```

## Paywall (custom UI)

Use `PaywallViewModel` from `paywall-logic` and build only the UI yourself:

```kotlin
@Composable
fun MyCustomPaywall(activity: Activity) {
    val viewModel = remember { PaywallViewModel(client) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOfferings() }

    // Build any UI with state.packages, state.selectedPackage, state.isPurchasing, etc.

    Button(onClick = { viewModel.purchase(activity) }) {
        Text("Subscribe ${state.selectedPackage?.localizedPriceString}")
    }
}
```

## Paywall (default UI)

Use `DefaultPaywall` from `paywall-compose` as is:

```kotlin
@Composable
fun PaywallScreen(activity: Activity) {
    DefaultPaywall(
        client = client,
        theme = PaywallTheme(
            title = "Go Premium",
            subtitle = "Unlock all features",
            features = listOf("Feature A", "Feature B", "No Ads"),
        ),
        purchaseParams = activity,
        onDismiss = { /* close */ },
        onPurchaseSuccess = { /* handle success */ },
    )
}
```

## SwiftUI

To observe `PaywallViewModel.state` (a Kotlin `StateFlow`) from SwiftUI, wrap it in an `ObservableObject`.
`PaywallStateObserver` below is defined in the iOS example app ([`FlowObserver.swift`](../example-ios/ExampleApp/FlowObserver.swift)).

```swift
struct PaywallView: View {
    @StateObject private var observer: PaywallStateObserver

    init(client: RevenueCatClient) {
        _observer = StateObject(wrappedValue: PaywallStateObserver(client: client))
    }

    var body: some View {
        VStack {
            if observer.state.isLoading {
                ProgressView()
            } else {
                ForEach(observer.state.packages, id: \.identifier) { pkg in
                    Button(pkg.localizedPriceString) {
                        observer.viewModel.selectPackage(packageInfo: pkg)
                        observer.viewModel.purchase(purchaseParams: NSObject())
                    }
                }
            }
        }
        .onAppear { observer.viewModel.loadOfferings() }
    }
}
```

See the example apps for complete code: [Android](../example/README.md) / [iOS](../example-ios/README.md)
