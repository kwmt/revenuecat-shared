# Android Example App

English | [日本語](README.ja.md)

An Android sample app (Jetpack Compose) that shows how to use the `core` and `paywall-logic` modules.

## Screenshots

| Home | Paywall | Settings |
|:----:|:-------:|:--------:|
| <img src="docs/screenshots/home.png" width="250"> | <img src="docs/screenshots/paywall.png" width="250"> | <img src="docs/screenshots/settings.png" width="250"> |

## Setup

### 1. Configure the RevenueCat dashboard

The sample app needs some configuration on the RevenueCat side.

#### 1-1. Create a project

1. Log in to the [RevenueCat Dashboard](https://app.revenuecat.com)
2. Create a project with "Create new project"
3. Add an "Android app" → package name: `io.github.kwmt.revenuecat.example`

#### 1-2. Entitlements

1. Project Settings > Entitlements > "New"
2. Create one with the identifier `premium` (it must match `entitlementId` in the code)

#### 1-3. Products

1. Project Settings > Products > "New"
2. Register the product ID of the subscription you created in the Google Play Console
3. Attach it to the `premium` entitlement

#### 1-4. Offerings

1. Project Settings > Offerings > edit the Default Offering
2. Add packages (Monthly / Annual, etc.) and attach the products

#### 1-5. Get the API key

1. Project Settings > API Keys
2. Copy the **Public Android API key** (`goog_xxx...`)

> **About Google Play service credentials**
> They are not needed for sandbox testing. For production, create a service account in the Google Cloud Console and upload it to RevenueCat.

### 2. Configure the app

1. Set the RevenueCat API key in `local.properties`:

```properties
revenuecat.apiKey=goog_xxxxxxxxxxxxx
```

2. Build:

```bash
./gradlew :example:assembleDebug
```

## Sandbox testing (no real charges)

You can test the purchase flow in Google Play's sandbox. **No real charges are made.**

### Add testers

1. Open the [Google Play Console](https://play.google.com/console)
2. Settings > License Testing
3. Add the Gmail addresses you test with
4. Set License type to `RESPOND_NORMALLY`

### Steps

1. Sign in on the device/emulator with a Google account added as a tester
2. Install and launch the app
3. Pick a package on the Paywall screen → tap "Subscribe"
4. Google Play's purchase dialog appears → pay with a **test card** (no charge)
5. After the purchase, check that the Home screen shows "Premium: Active"

### Notes

- On an emulator, use a **Google Play Store image** (Google APIs + Play Store)
- Accounts not added to License Testing are charged for real
- Use the "Sandbox data" toggle in the RevenueCat dashboard to see sandbox transactions

### Subscription periods in testing

In the sandbox, subscription renewal periods are shortened:

| Production | Test |
|----------|-----------|
| 1 week | 5 minutes |
| 1 month | 5 minutes |
| 3 months | 10 minutes |
| 6 months | 15 minutes |
| 1 year | 30 minutes |

## Screens

| Screen | Features | API used |
|------|------|---------|
| Home | Shows the entitlement status, opens the Paywall | `RevenueCatClient.entitlementStatus` |
| Paywall | Lists, selects, purchases, and restores packages | `PaywallViewModel` |
| Settings | User ID input, Login/Logout, Restore | `RevenueCatClient.login/logout/restore` |
