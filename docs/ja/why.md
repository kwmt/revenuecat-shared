# 公式の purchases-kmp との違い

[English](../why.md) | 日本語

RevenueCat は公式の Kotlin Multiplatform SDK [`purchases-kmp`](https://github.com/RevenueCat/purchases-kmp) を出しています。
このライブラリはそれを**置き換えるものではなく、その上に載せる薄い層**です。中では `purchases-kmp` を呼んでいます。

公式 SDK は RevenueCat のすべての機能をそのまま出す汎用の API です。このライブラリは、
「サブスクを売るアプリが毎回書くことになるコード」と「ストアごとの違いでハマりやすい所」を引き受け、
アプリから見る API を小さく保つことを目的にしています。

## モチベーション

複数のアプリで RevenueCat を使うと、どのアプリでも次のようなコードを書くことになります。

- Entitlement の ID を毎回指定して `CustomerInfo` から状態を取り出す
- 購入のキャンセルとエラーを例外から見分ける
- 無料体験の長さや使えるかを、App Store と Google Play で違う場所から読む
- 上位プランへの変更で、Google Play だけ乗り換え元を指定する
- 同じ購入画面のロジックを Compose と SwiftUI の両方から使う

1つのアプリで書けば済みますが、アプリごとに書き直すと、同じハマり方を何度もします。
それを1か所にまとめて、どのアプリでも同じ API で使えるようにしたのがこのライブラリです。

## メリット

### 1. アプリ向けに絞った小さな API

`RevenueCatConfig` で Entitlement の ID を1度決めれば、あとは `checkEntitlement()`・`isPremium`・
`entitlementStatus`（StateFlow）で状態を読めます。`CustomerInfo` や `EntitlementInfo` を毎回たどる必要はありません。

購入・リストアの結果は `PurchaseResult`（`Success` / `Cancelled` / `Error`）という sealed interface で返します。
公式 SDK は失敗を例外で投げ、キャンセルは `PurchasesTransactionException.userCancelled` で見分けますが、
ここではそれを型で分けて返すので、`when` で漏れなく扱えます。

### 2. 「確認できなかった」と「未購入」を区別できる

`checkEntitlementOrNull()` は、ストアに問い合わせられなかったときに `null` を返します。
購読状態を端末に保存するアプリが、通信エラーのたびに課金中の人を「未購入」で上書きしてしまう事故を防げます。

### 3. Android でも無料体験を使えるか判定できる

公式 SDK の `checkTrialOrIntroPriceEligibility` は、**Android では常に `UNKNOWN`** を返します。
このライブラリの `checkTrialEligibility()` は、Google Play が返す特典（その人が使えるものだけが届く）から
`ELIGIBLE` / `INELIGIBLE` を決めるので、iOS と同じコードで「無料体験を見せるか」を決められます。

### 4. ストアごとの違いを吸収する

- **無料体験の長さ**: App Store は `introductoryDiscount`、Google Play は `subscriptionOptions.freeTrial` にあります。
  `PackageInfo.freeTrial` はどちらでも同じ `TrialPeriod` で返します（App Store の割引価格の導入価格は無料体験に数えません）
- **上位プランへの変更**: Google Play は乗り換え元（`商品:基本プラン`）を明示しないと、2つの購読が並んで両方に請求されます。
  `purchaseChangingPlan()` は今の購読を見て、必要なときだけ乗り換えとして買います。App Store や購読が無いときはふつうの購入です
- **値段**: 割引率の計算用の `priceAmountMicros` と、年額の「月あたり」の `pricePerMonthString` を `PackageInfo` に入れています

### 5. テストしやすい

公式 SDK は `Purchases.sharedInstance` というシングルトンを直接呼ぶ形です。
このライブラリは `RevenueCatClient` という interface を通すので、ViewModel や Repository のテストでは
Fake を差し込めます（`paywall-logic` の `PaywallViewModel` のテストもそうしています）。

### 6. 購入画面のロジックを Compose と SwiftUI で共有できる

- `paywall-logic` の `PaywallViewModel` / `PaywallState` を、Compose からも SwiftUI からも使えます
- Swift から StateFlow を監視する `FlowHelper` を用意しているので、SKIE や KMP-NativeCoroutines は要りません
- UI をカスタマイズしないなら、`paywall-compose` の `DefaultPaywall` をそのまま使えます

公式の `purchases-kmp-ui` は、RevenueCat のダッシュボードで作る Paywall を表示するものです。
こちらは**アプリ側で UI を作る**ときのためのものなので、用途が違います。

## 公式 SDK を直接使ったほうがよい場合

- このライブラリが包んでいない機能を使う（Customer Attributes・プロモーションオファー・RevenueCat Paywalls など）
- アプリが1つだけで、共通化する必要がない
- GitHub Packages の認証（`read:packages` のトークン）を用意したくない
- `purchases-kmp` の新しいバージョンにすぐ上げたい（このライブラリは追従にタイムラグがあります）

このライブラリと公式 SDK は同じアプリで併用できます。ただし `purchases-kmp` は `implementation` 依存なので、アプリから直接呼ぶときは同じバージョンの `purchases-kmp-core` をアプリの依存に足してください。
