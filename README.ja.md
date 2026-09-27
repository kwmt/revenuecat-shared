# revenuecat-shared

[English](README.md) | 日本語

RevenueCat SDK を包む小さな Kotlin Multiplatform ライブラリです。共通の `commonMain` のコード
（ViewModel・Repository）から、プラットフォームの SDK を import せずに **Android と iOS** で
Entitlement の確認と購入ができます。

- `checkEntitlement()` / `entitlementStatus`（StateFlow）/ `isPremium`
- `checkEntitlementOrNull()` — ストアに問い合わせられなかったときは `null` を返す。購読状態を端末に保存するアプリが、
  ネットワークのエラーで課金中の人の状態を「未購読」で上書きしないため
- `checkEntitlementOrNull(entitlementId)` — Entitlement が複数ある（段階のある）アプリ向け
- `fetchCurrentOfferingPackages()` — 値段の数（`priceAmountMicros`・`pricePerMonthString`）つき
- `checkTrialEligibility()` — 両プラットフォーム対応（Android は Google Play が返す特典から決める）
- `purchase()` / `purchaseChangingPlan()`（上位プランへ変えるときの Google Play の乗り換え）/ `restore()` / `login()` / `logout()`
- 必要に応じて `paywall-logic`（ViewModel と状態）と `paywall-compose`（Compose Multiplatform の既定の Paywall）

[Machilingual](https://machilingual.com)（iOS・Android の Kotlin Multiplatform / Compose Multiplatform アプリ）で本番運用しています。

## モジュール構成

| モジュール | 内容 | 依存が必要なケース |
|---|---|---|
| `:core` | SDK初期化・購入・リストア・Entitlement確認 | 全アプリ必須 |
| `:paywall-logic` | `PaywallViewModel` / `PaywallState` | Paywall画面を作るアプリ |
| `:paywall-compose` | デフォルトPaywall Composable | UIをカスタマイズしないアプリ |

## クイックスタート

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("io.github.kwmt.revenuecat:core:0.0.9")
}
```

```kotlin
val client = RevenueCatClientFactory.create()
client.configure(
    RevenueCatConfig(
        apiKey = "goog_XXXXX",  // or "appl_XXXXX"
        entitlementId = "premium",
    )
)

if (client.checkEntitlement().isActive) { /* プレミアム機能を開放 */ }
```

GitHub Packages で配布しているので、先に参照先と認証情報の設定が要ります。[導入](docs/ja/installation.md)を見てください。

## ドキュメント

- [導入](docs/ja/installation.md) — GitHub Packages・依存の追加・iOS の要件
- [使い方](docs/ja/usage.md) — 初期化・Entitlement 確認・無料体験・段階のあるプラン・Paywall
- [リリース](docs/ja/release.md) — 新しいバージョンの出し方
- サンプルアプリ: [Android](example/README.ja.md) / [iOS](example-ios/README.ja.md)

## ライセンス

[Apache License 2.0](LICENSE)
