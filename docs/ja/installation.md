# 導入

[English](../installation.md) | 日本語

## 1. GitHub Packages を参照先に追加

ライブラリは GitHub Packages で配布しています。各アプリの `settings.gradle.kts`:

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

## 2. 依存を追加

```toml
# 各アプリの libs.versions.toml
[versions]
revenuecat-shared = "0.0.9"

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
