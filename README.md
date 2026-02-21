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

### 3. iOS ネイティブ依存のリンク

KMP の `purchases-kmp-core` は RevenueCat iOS ネイティブ SDK をラップしていますが、ネイティブフレームワーク自体は消費側アプリで別途提供する必要があります（KMP + Maven 配布の制約）。

#### バージョン対応ルール

`purchases-kmp-core` のバージョン（例: `2.5.1+17.33.1`）の `+` 以降がネイティブ SDK のバージョンです。
`revenuecat-shared` が使用しているバージョンに合わせてください。

#### iOS 最小バージョン要件

iOS Deployment Target を **17.0 以上** に設定してください。

#### CocoaPods で追加（推奨）

KMP プロジェクトでは `kotlin-cocoapods` プラグインを使うと、Gradle から iOS ネイティブ依存を宣言的に管理できます。

**1.** iOS フレームワークをビルドしている shared モジュールの `build.gradle.kts` に `cocoapods` プラグインを追加:

```kotlin
plugins {
    // 既存のプラグイン
    kotlin("multiplatform")
    kotlin("native.cocoapods") // 追加
}
```

**2.** `kotlin {}` ブロック内に `cocoapods {}` を追加し、手動フレームワーク設定を置き換え:

```kotlin
kotlin {
    // 変更前:
    // listOf(iosArm64(), iosSimulatorArm64()).forEach {
    //     it.binaries.framework {
    //         baseName = "SharedApp"
    //         isStatic = true
    //     }
    // }

    // 変更後:
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Shared module"
        homepage = "https://github.com/example"
        version = "1.0"
        ios.deploymentTarget = "17.0"
        framework {
            baseName = "SharedApp"
            isStatic = true
        }
        pod("PurchasesHybridCommon", "17.33.1")
    }
}
```

**3.** iOS プロジェクトで `pod install` を実行:

```bash
cd iosApp
pod install
```

> **注意:** `cocoapods` プラグイン導入後は `.xcodeproj` ではなく `.xcworkspace` を開いてください。

#### SPM で追加（代替）

CocoaPods を使わない場合は、Xcode で手動追加できます:

1. Xcode で File > Add Package Dependencies を開く
2. URL: `https://github.com/RevenueCat/purchases-hybrid-common`
3. Dependency Rule: Exact Version `17.33.1`
4. `PurchasesHybridCommon` ライブラリを iOS ターゲットに追加

## 使い方

### 初期化（各アプリの起動時）

```kotlin
// Android: Application.onCreate() 等
RevenueCatManager.configure(
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
val status = RevenueCatManager.checkEntitlement()
if (status.isActive) { /* プレミアム機能を開放 */ }

// キャッシュで即時確認
if (RevenueCatManager.isPremium) { /* ... */ }

// StateFlow で監視
RevenueCatManager.entitlementStatus.collect { status ->
    // UI更新
}
```

### Paywall（カスタムUI）

`paywall-logic` の `PaywallViewModel` を使い、UIだけ自前で実装:

```kotlin
@Composable
fun MyCustomPaywall(activity: Activity) {
    val viewModel = remember { PaywallViewModel() }
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
