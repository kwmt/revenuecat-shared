# Installation

English | [日本語](ja/installation.md)

## 1. Add GitHub Packages as a repository

The library is published to GitHub Packages. In each app's `settings.gradle.kts`:

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

Add your credentials to `local.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=YOUR_GITHUB_TOKEN  # needs the read:packages scope
```

## 2. Add the dependencies

```toml
# libs.versions.toml
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
