# 導入

[English](../installation.md) | 日本語

## 1. 参照先

ライブラリは **Maven Central** で配布しています（0.0.10 以降）。参照先や認証情報を足す必要はありません。
`mavenCentral()` が参照先に入っていることだけ確認してください（新しいプロジェクトなら最初から入っています）:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

> **0.0.9 以前から上げるとき:** 0.0.9 までは GitHub Packages だけで配布していました。
> 0.0.10 以降に上げたら、`maven.pkg.github.com/kwmt/revenuecat-shared` の参照先と、`local.properties` の
> `gpr.user` / `gpr.token` は消して構いません。

## 2. 依存を追加

```toml
# 各アプリの libs.versions.toml
[versions]
revenuecat-shared = "0.0.10"

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

### どのモジュールを使うか

| アプリ | 利用モジュール |
|---|---|
| KMP でネイティブ UI（SwiftUI + Compose） | core + paywall-logic + 自前UI |
| Compose Multiplatform（全Compose） | core + paywall-logic + 自前UI、または paywall-compose |

## 3. iOS ネイティブ依存（0.0.8 以降は不要）

0.0.8 から `purchases-kmp` 3.x を使っています。3.x は RevenueCat の iOS SDK（purchases-ios）を**ライブラリの中に同梱**しているので、
消費側アプリで iOS のネイティブ依存を足す必要はありません。

- ★**`PurchasesHybridCommon` を CocoaPods / SPM で足さないでください。** 足すと RevenueCat の SDK が二重に入ります。
  0.0.7 から上げるときは、Podfile・`cocoapods {}` の `pod("PurchasesHybridCommon", …)`・Xcode の Package Dependencies から外してください
- 消費側の Kotlin は **2.3.21 以上**にしてください（0.0.8 は Kotlin 2.3.21 でビルドしています。klib はそれより古い Kotlin では読めません）
- iOS Deployment Target は **17.0 以上**に設定してください
- 0.0.7 までの `PurchasesHybridCommon` 17.33.1 が引く purchases-ios 5.57.2 は、**Xcode 27（Swift 6.4）でコンパイルできません**
  （`PaywallColor.swift` の `invalid redeclaration of synthesized memberwise 'init(stringRepresentation:)'`）。
  0.0.8 は purchases-ios 5.89.0 を同梱しており、Xcode 26 と Xcode 27 の両方でビルドできます

次へ: [使い方](usage.md)
