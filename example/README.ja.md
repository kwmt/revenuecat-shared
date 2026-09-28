# Android Example App

[English](README.md) | 日本語

`core` + `paywall-logic` モジュールの使い方を示す Android サンプルアプリケーション（Jetpack Compose）。

## スクリーンショット

| Home | Paywall | Settings |
|:----:|:-------:|:--------:|
| <img src="docs/screenshots/home.png" width="250"> | <img src="docs/screenshots/paywall.png" width="250"> | <img src="docs/screenshots/settings.png" width="250"> |

## セットアップ

### 1. RevenueCat ダッシュボードの設定

サンプルアプリを動作させるには、RevenueCat 側の設定が必要です。

#### 1-1. プロジェクト作成

1. [RevenueCat Dashboard](https://app.revenuecat.com) にログイン
2. 「Create new project」でプロジェクトを作成
3. 「Android app」を追加 → パッケージ名: `io.github.kwmt.revenuecat.example`

#### 1-2. Entitlements 設定

1. Project Settings > Entitlements > 「New」
2. Identifier: `premium` で作成（コード内の `entitlementId` と一致させる）

#### 1-3. Products 登録

1. Project Settings > Products > 「New」
2. Google Play Console で作成した定期購入の商品IDを登録
3. 作成した Entitlement (`premium`) に紐付け

#### 1-4. Offerings 設定

1. Project Settings > Offerings > Default Offering を編集
2. パッケージ（Monthly / Annual 等）を追加し、Products を紐付け

#### 1-5. API キーの取得

1. Project Settings > API Keys
2. **Public Android API key** (`goog_xxx...`) をコピー

> **Google Play Service Credentials について**
> Sandbox テストだけなら不要です。本番運用時は Google Cloud Console で Service Account を作成し、RevenueCat にアップロードしてください。

### 2. アプリ側の設定

1. `local.properties` に RevenueCat API キーを設定:

```properties
revenuecat.apiKey=goog_xxxxxxxxxxxxx
```

2. ビルド:

```bash
./gradlew :example:assembleDebug
```

## Sandbox テスト（実際の課金なし）

購入フローのテストは Google Play の Sandbox 環境で行えます。**実際の課金は発生しません。**

### テスターの追加

1. [Google Play Console](https://play.google.com/console) を開く
2. Settings > License Testing
3. テストに使う Gmail アドレスを追加
4. License type: `RESPOND_NORMALLY` を選択

### テスト手順

1. テスターとして追加した Google アカウントでデバイス/エミュレータにログイン
2. アプリをインストールして起動
3. Paywall 画面でパッケージを選択 → 「Subscribe」をタップ
4. Google Play の購入ダイアログが表示される → **テストカード**で決済（課金なし）
5. 購入完了後、Home 画面で「Premium: Active」になることを確認

### 注意点

- エミュレータでテストする場合は **Google Play Store 搭載イメージ** (Google APIs + Play Store) を使用
- License Testing に追加していないアカウントでは実際の課金が発生するので注意
- RevenueCat ダッシュボードの「Sandbox data」トグルで Sandbox トランザクションを確認可能

### 定期購入のテスト期間

Sandbox 環境では定期購入の更新サイクルが短縮されます：

| 本番期間 | テスト期間 |
|----------|-----------|
| 1週間 | 5分 |
| 1ヶ月 | 5分 |
| 3ヶ月 | 10分 |
| 6ヶ月 | 15分 |
| 1年 | 30分 |

## 画面構成

| 画面 | 機能 | 使用API |
|------|------|---------|
| Home | Entitlement Status 表示、Paywall 遷移 | `RevenueCatClient.entitlementStatus` |
| Paywall | パッケージ一覧・選択・購入・リストア | `PaywallViewModel` |
| Settings | User ID 入力、Login/Logout、Restore | `RevenueCatClient.login/logout/restore` |
