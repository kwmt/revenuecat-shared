# iOS Example App

English | [日本語](README.ja.md)

An iOS sample app (SwiftUI) that shows how to use the `core` and `paywall-logic` modules.

## Screenshots

| Home | Paywall | Settings |
|:----:|:-------:|:--------:|
| <img src="docs/screenshots/home.png" width="250"> | <img src="docs/screenshots/paywall.png" width="250"> | <img src="docs/screenshots/settings.png" width="250"> |

## Setup

### 1. Configure the RevenueCat dashboard

The sample app needs some configuration on the RevenueCat side.

#### 1-1. Create a project

1. Log in to the [RevenueCat Dashboard](https://app.revenuecat.com)
2. Create a project with "Create new project" (it can be shared with Android)
3. Add an "iOS app" → Bundle ID: `io.github.kwmt.revenuecat.example-ios`

#### 1-2. Entitlements

1. Project Settings > Entitlements > "New"
2. Create one with the identifier `premium` (it must match `entitlementId` in the code)

#### 1-3. Products

1. Project Settings > Products > "New"
2. Register the product ID of the subscription you created in App Store Connect
3. Attach it to the `premium` entitlement

#### 1-4. Offerings

1. Project Settings > Offerings > edit the Default Offering
2. Add packages (Monthly / Annual, etc.) and attach the products

#### 1-5. Get the API key

1. Project Settings > API Keys
2. Copy the **Public iOS API key** (`appl_xxx...`)

> **About App Store Connect settings**
> The App Store Connect shared secret is not needed for sandbox testing. For production, upload an App Store Connect API key to RevenueCat.

### 2. Configure the app

1. Build the KMP framework:

```bash
./gradlew :paywall-logic:linkDebugFrameworkIosSimulatorArm64
```

2. Set the RevenueCat API key in `ExampleApp/Configuration.swift`:

```swift
static let revenueCatAPIKey = "appl_xxxxxxxxxxxxx"
```

3. Open `ExampleApp.xcodeproj` in Xcode, then build and run (CocoaPods is not used)

## Sandbox testing (no real charges)

You can test the purchase flow in Apple's sandbox. **No real charges are made.**

### Option 1: StoreKit Testing in Xcode (local, recommended)

You can test locally right away, with no product setup in App Store Connect.

1. In Xcode, create a StoreKit Configuration File via File > New > File
2. Add subscription products (product IDs must match the products in RevenueCat)
3. In Scheme > Edit Scheme > Run > Options > StoreKit Configuration, select the file
4. Run on a simulator or device → the purchase dialog appears

### Option 2: Sandbox tester account

Testing with the App Store Connect sandbox.

#### Create a tester

1. Open [App Store Connect](https://appstoreconnect.apple.com)
2. Users and Access > Sandbox > Testers
3. Create a tester account with "+" (a non-existent email address is fine)

#### Steps

1. On a device, sign in with the tester account in Settings > App Store > Sandbox Account
2. Install and launch the app
3. Pick a package on the Paywall screen → tap "Subscribe"
4. The sandbox purchase dialog appears → enter the password (no charge)
5. After the purchase, check that the Home screen shows "Premium: Active"

### Notes

- **Simulator**: only StoreKit Testing (option 1) works. Sandbox accounts require a real device
- Use the "Sandbox data" toggle in the RevenueCat dashboard to see sandbox transactions
- RevenueCat detects the sandbox environment automatically; no extra configuration is needed

### Subscription periods in testing

In the sandbox, subscription renewal periods are shortened:

| Production | Test |
|----------|-----------|
| 1 week | 3 minutes |
| 1 month | 5 minutes |
| 2 months | 10 minutes |
| 3 months | 15 minutes |
| 6 months | 30 minutes |
| 1 year | 1 hour |

## Screens

| Screen | Features | API used |
|------|------|---------|
| Home | Shows the entitlement status, Check Entitlement | `RevenueCatClient.entitlementStatus` |
| Paywall | Lists, selects, purchases, and restores packages | `PaywallViewModel` |
| Settings | User ID input, Login/Logout, Restore | `RevenueCatClient.login/logout/restore` |

## Technical notes

- KMP StateFlows are observed with a callback pattern via `FlowHelper` (no SKIE needed)
- The RevenueCat iOS SDK is bundled into the Kotlin framework by `purchases-kmp` 3.x (since 0.0.8; adding `PurchasesHybridCommon` links the SDK twice)
- The `paywall-logic` framework also exposes the core API via `export(project(":core"))`
