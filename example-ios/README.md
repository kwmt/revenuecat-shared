# iOS Example App

`core` + `paywall-logic` モジュールの使い方を示す iOS サンプルアプリケーション（SwiftUI）。

## スクリーンショット

| Home | Paywall | Settings |
|:----:|:-------:|:--------:|
| <img src="docs/screenshots/home.png" width="250"> | <img src="docs/screenshots/paywall.png" width="250"> | <img src="docs/screenshots/settings.png" width="250"> |

## セットアップ

1. KMP フレームワークをビルド:

```bash
./gradlew :paywall-logic:linkDebugFrameworkIosSimulatorArm64
```

2. CocoaPods 依存をインストール:

```bash
cd example-ios
pod install
```

3. `ExampleApp/Configuration.swift` に RevenueCat API キーを設定:

```swift
static let revenueCatAPIKey = "your_api_key_here"
```

4. `ExampleApp.xcworkspace` を Xcode で開いてビルド・実行

## 画面構成

| 画面 | 機能 | 使用API |
|------|------|---------|
| Home | Entitlement Status 表示、Check Entitlement | `RevenueCatManager.entitlementStatus` |
| Paywall | パッケージ一覧・選択・購入・リストア | `PaywallViewModel` |
| Settings | User ID 入力、Login/Logout、Restore | `RevenueCatManager.login/logout/restore` |

## 技術的な補足

- KMP StateFlow の監視には `FlowHelper` 経由のコールバックパターンを使用（SKIE 不要）
- RevenueCat iOS SDK (`PurchasesHybridCommon`) は CocoaPods で管理
- `paywall-logic` フレームワークが `export(project(":core"))` で core API も公開
