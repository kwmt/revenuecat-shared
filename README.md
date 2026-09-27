# revenuecat-shared

English | [日本語](README.ja.md)

A small Kotlin Multiplatform wrapper around the RevenueCat SDK, so that shared `commonMain` code
(ViewModels, repositories) can check entitlements and make purchases on **Android and iOS** without
importing a platform SDK.

- `checkEntitlement()` / `entitlementStatus` (StateFlow) / `isPremium`
- `checkEntitlementOrNull()` — returns `null` when the store could not be reached, so an app that
  persists membership never overwrites a paying user's state with "not subscribed" because of a network error
- `checkEntitlementOrNull(entitlementId)` for apps with several entitlements (tiers)
- `fetchCurrentOfferingPackages()` with prices (`priceAmountMicros`, `pricePerMonthString`)
- `checkTrialEligibility()` on both platforms (on Android it is derived from the offers Google Play returns)
- `purchase()` / `purchaseChangingPlan()` (Google Play plan-change modes for tier upgrades) / `restore()` / `login()` / `logout()`
- Optional `paywall-logic` (ViewModel + state) and `paywall-compose` (a default Compose Multiplatform paywall)

Used in production by [Machilingual](https://machilingual.com), a Kotlin Multiplatform / Compose Multiplatform app on iOS and Android.

## Modules

| Module | Contents | When you need it |
|---|---|---|
| `:core` | SDK setup, purchase, restore, entitlement checks | Every app |
| `:paywall-logic` | `PaywallViewModel` / `PaywallState` | Apps that build a paywall screen |
| `:paywall-compose` | A default paywall Composable | Apps that don't customize the paywall UI |

## Quick start

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

if (client.checkEntitlement().isActive) { /* unlock premium features */ }
```

Packages are published to GitHub Packages, so you need to add the repository and credentials first.
See [Installation](docs/installation.md).

## Documentation

- [Why not just use purchases-kmp?](docs/why.md) — what this adds on top of the official SDK
- [Installation](docs/installation.md) — GitHub Packages, dependencies, iOS requirements
- [Usage](docs/usage.md) — setup, entitlements, free trials, tiered plans, paywalls
- [Release](docs/release.md) — how a new version is published
- Example apps: [Android](example/README.md) / [iOS](example-ios/README.md)

## License

[Apache License 2.0](LICENSE)
