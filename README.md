# revenuecat-shared

複数アプリで共通利用するRevenueCat KMPラッパーライブラリ。

## モジュール構成

| モジュール | 内容 | 依存が必要なケース |
|---|---|---|   
| `:core` | SDK初期化・購入・リストア・Entitlement確認 | 全アプリ必須 |
| `:paywall-logic` | PaywallViewModel / PaywallState | Paywall画面を作るアプリ |
| `:paywall-compose` | デフォルトPaywall Composable | UIをカスタマイズしないアプリ |

## セットアップ

### 1. GitHub Packages を参照先に追加

各アプリの `settings.gradle.kts`:

```kotlin
import java.util.Properties

val localProps = Properties().apply {
    val file = rootProject.projectDir.resolve("local.properties")
    if (file.exists()) load(file.inputStream())
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/kwmt/revenuecat-shared")
            credentials {
                username = localProps.getProperty("gpr.user") ?: ""
                password = localProps.getProperty("gpr.token") ?: ""
            }
        }
    }
}
```

`local.properties` に認証情報を追加:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_TOKEN  # read:packages 権限
```

### 2. 依存を追加

```kotlin
// 各アプリの libs.versions.toml
[versions]
revenuecat-shared = "0.1.0"

[libraries]
revenuecat-shared-core = { module = "io.github.kwmt.revenuecat:core", version.ref = "revenuecat-shared" }
revenuecat-shared-paywall-logic = { module = "io.github.kwmt.revenuecat:paywall-logic", version.ref = "revenuecat-shared" }
revenuecat-shared-paywall-compose = { module = "io.github.kwmt.revenuecat:paywall-compose", version.ref = "revenuecat-shared" }
```

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation(libs.revenuecat.shared.core)
    implementation(libs.revenuecat.shared.paywall.logic)
    // デフォルトUIを使う場合のみ:
    // implementation(libs.revenuecat.shared.paywall.compose)
}
```

### 3. iOS ネイティブ依存（0.0.8 以降は不要）

0.0.8 から `purchases-kmp` 3.x を使っています。3.x は RevenueCat の iOS SDK（purchases-ios）を**ライブラリの中に同梱**しているので、
消費側アプリで iOS のネイティブ依存を足す必要はありません。

- ★**`PurchasesHybridCommon` を CocoaPods / SPM で足さないでください。** 足すと RevenueCat の SDK が二重に入ります。
  0.0.7 から上げるときは、Podfile・`cocoapods {}` の `pod("PurchasesHybridCommon", …)`・Xcode の Package Dependencies から外してください
- 消費側の Kotlin は **2.3.21 以上**にしてください（0.0.8 は Kotlin 2.3.21 でビルドしています。klib はそれより古い Kotlin では読めません）
- iOS Deployment Target は **17.0 以上**に設定してください
- 0.0.7 までの `PurchasesHybridCommon` 17.33.1 が引く purchases-ios 5.57.2 は、**Xcode 27（Swift 6.4）でコンパイルできません**
  （`PaywallColor.swift` の `invalid redeclaration of synthesized memberwise 'init(stringRepresentation:)'`）。
  0.0.8 は purchases-ios 5.89.0 を同梱しており、Xcode 26 と Xcode 27 の両方でビルドできます

## 使い方

### 初期化（各アプリの起動時）

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

### Entitlement確認

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

### 無料体験の表示

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

### 段階のあるプラン（Entitlement が2つ以上）

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

### Paywall（カスタムUI）

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

### Paywall（デフォルトUI）

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

### SwiftUI (App A の iOS側)

```swift
struct PaywallView: View {
    @StateObject private var observer = PaywallViewModelObserver()

    var body: some View {
        VStack {
            if observer.state.isLoading {
                ProgressView()
            } else {
                ForEach(observer.state.packages, id: \.identifier) { pkg in
                    Button(pkg.localizedPriceString) {
                        observer.viewModel.purchase(purchaseParams: ())
                    }
                }
            }
        }
        .onAppear { observer.viewModel.loadOfferings() }
    }
}
```

## リリース

```bash
git tag v0.1.0
git push origin v0.1.0
# → GitHub Actions が自動で GitHub Packages に publish
```

## アプリ構成

| アプリ | プラットフォーム | 利用モジュール |
|---|---|---|
| App A | KMP (SwiftUI + Compose) | core + paywall-logic + 自前UI |
| App B | CMP (全Compose) | core + paywall-logic + 自前UI or paywall-compose |
| 今後のアプリ | 任意 | 必要なモジュールを選択 |
