# 使い方

[English](../usage.md) | 日本語

## 初期化（各アプリの起動時）

```kotlin
// Android: Application.onCreate() 等
val client = RevenueCatClientFactory.create()
client.configure(
    RevenueCatConfig(
        apiKey = "goog_XXXXX",  // or "appl_XXXXX"
        entitlementId = "premium",
        debugLogsEnabled = BuildConfig.DEBUG,
    )
)
```

## Entitlement確認

```kotlin
// suspend で確認（ネットワークあり）
val status = client.checkEntitlement()
if (status.isActive) { /* プレミアム機能を開放 */ }

// キャッシュで即時確認
if (client.isPremium) { /* ... */ }

// StateFlow で監視
client.entitlementStatus.collect { status ->
    // UI更新
}

// 確認に失敗したら null（checkEntitlement は失敗も isActive = false に丸める）。
// 購読状態を端末に保存するアプリは、確認できなかった回に上書きしないためにこちらを使う
client.checkEntitlementOrNull()?.let { status -> saveMember(status.isActive) }
```

## 無料体験の表示

```kotlin
val packages = client.fetchCurrentOfferingPackages()
val annual = packages.first { it.packageType == PackageKind.ANNUAL }

// 商品に設定された体験の長さ（App Store Connect の「1週間」は TrialPeriod(1, WEEK)）
val trial = annual.freeTrial

// その人が体験を使えるか。ELIGIBLE のときだけ「無料」と見せる
// （Android は Play が返す特典で決まる: 無料体験の特典が届いていれば ELIGIBLE・無ければ INELIGIBLE。
//   UNKNOWN は通常の価格として見せるのが RevenueCat の推奨）
val eligibility = client.checkTrialEligibility(packages)
val showTrial = trial != null && eligibility[annual.productIdentifier] == TrialEligibility.ELIGIBLE
```

## 段階のあるプラン（Entitlement が2つ以上）

```kotlin
// 設定した entitlementId 以外の Entitlement を確認する（確認に失敗したら null）
val explain = client.checkEntitlementOrNull("explain")?.isActive

// 値段の数（割引率の計算用）と、年額の「月あたり」の表示
val annual = packages.first { it.identifier == "standard_annual" }
val micros = annual.priceAmountMicros          // ¥4,000 → 4_000_000_000
val perMonth = annual.pricePerMonthString      // 例: "¥333"

// 上位のプランへ変える購入。Google Play で有効な購読が別の商品にあれば乗り換えとして買う
// （明示しないと2つの購読が並んで両方に請求される）。App Store・購読なしならふつうの購入と同じ
val result = client.purchaseChangingPlan(annual, PlanChangeMode.CHARGE_PRORATED_PRICE)
```

## Paywall（カスタムUI）

`paywall-logic` の `PaywallViewModel` を使い、UIだけ自前で実装:

```kotlin
@Composable
fun MyCustomPaywall(activity: Activity) {
    val viewModel = remember { PaywallViewModel(client) }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOfferings() }

    // state.packages, state.selectedPackage, state.isPurchasing 等を使って
    // 自由にUIを構築

    Button(onClick = { viewModel.purchase(activity) }) {
        Text("Subscribe ${state.selectedPackage?.localizedPriceString}")
    }
}
```

## Paywall（デフォルトUI）

`paywall-compose` の `DefaultPaywall` をそのまま使う:

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
        onDismiss = { /* 閉じる */ },
        onPurchaseSuccess = { /* 成功時の処理 */ },
    )
}
```

## SwiftUI

`PaywallViewModel.state`（Kotlin の `StateFlow`）を SwiftUI で監視するには `ObservableObject` で包みます。
下の `PaywallStateObserver` は iOS サンプルアプリの [`FlowObserver.swift`](../../example-ios/ExampleApp/FlowObserver.swift) にあります。

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

コード全体はサンプルアプリを見てください: [Android](../../example/README.ja.md) / [iOS](../../example-ios/README.ja.md)
