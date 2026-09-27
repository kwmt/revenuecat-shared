# Installation

English | [日本語](ja/installation.md)

## 1. Repository

The library is published to **Maven Central** (0.0.10 and later), so no extra repository or credentials are needed.
Make sure `mavenCentral()` is in your repositories (it is in new projects by default):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

> **Upgrading from 0.0.9 or earlier:** those versions were published only to GitHub Packages.
> After moving to 0.0.10 or later, you can remove the `maven.pkg.github.com/kwmt/revenuecat-shared` repository
> and the `gpr.user` / `gpr.token` entries in `local.properties`.

## 2. Add the dependencies

```toml
# libs.versions.toml
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
    // Only if you use the default UI:
    // implementation(libs.revenuecat.shared.paywall.compose)
}
```

### Which modules to use

| App | Modules |
|---|---|
| KMP with native UI (SwiftUI + Compose) | `core` + `paywall-logic` + your own UI |
| Compose Multiplatform | `core` + `paywall-logic` + your own UI, or `paywall-compose` |

## 3. iOS native dependencies (not needed since 0.0.8)

Since 0.0.8 the library uses `purchases-kmp` 3.x, which **bundles** RevenueCat's iOS SDK (purchases-ios).
Consuming apps don't need to add any native iOS dependency.

- **Do not add `PurchasesHybridCommon` via CocoaPods or SPM.** Doing so links the RevenueCat SDK twice.
  When upgrading from 0.0.7, remove it from your Podfile, from `pod("PurchasesHybridCommon", …)` in `cocoapods {}`,
  and from Xcode's Package Dependencies.
- Use **Kotlin 2.3.21 or later** in the consuming app. 0.0.8 is built with Kotlin 2.3.21, and older Kotlin versions cannot read its klibs.
- Set the iOS Deployment Target to **17.0 or later**.
- purchases-ios 5.57.2, pulled in by `PurchasesHybridCommon` 17.33.1 in 0.0.7 and earlier, **does not compile with Xcode 27 (Swift 6.4)**
  (`invalid redeclaration of synthesized memberwise 'init(stringRepresentation:)'` in `PaywallColor.swift`).
  0.0.8 bundles purchases-ios 5.89.0 and builds with both Xcode 26 and Xcode 27.

Next: [Usage](usage.md)
