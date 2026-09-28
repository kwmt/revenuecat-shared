# Why not just use purchases-kmp?

English | [日本語](ja/why.md)

RevenueCat ships an official Kotlin Multiplatform SDK, [`purchases-kmp`](https://github.com/RevenueCat/purchases-kmp).
This library **does not replace it; it is a thin layer on top of it**, and calls `purchases-kmp` internally.

The official SDK is a general-purpose API that exposes every RevenueCat feature as is. This library takes over
the code every subscription app ends up writing, and the store differences that are easy to get wrong,
so that the API your app sees stays small.

## Motivation

When you use RevenueCat in several apps, every app ends up with code like this:

- Passing the entitlement ID each time and digging the state out of `CustomerInfo`
- Telling a cancelled purchase apart from an error by inspecting exceptions
- Reading the free-trial length and eligibility from different places on the App Store and Google Play
- Specifying the old product only on Google Play when upgrading to a higher plan
- Using the same paywall logic from both Compose and SwiftUI

Writing it once for one app is fine, but rewriting it per app means hitting the same pitfalls again and again.
This library puts that code in one place so every app uses the same API.

## Benefits

### 1. A small, app-oriented API

Set the entitlement ID once in `RevenueCatConfig`, then read the state with `checkEntitlement()`, `isPremium`,
and `entitlementStatus` (a StateFlow). No need to walk `CustomerInfo` and `EntitlementInfo` each time.

Purchase and restore results come back as `PurchaseResult` (`Success` / `Cancelled` / `Error`), a sealed interface.
The official SDK throws exceptions and signals cancellation with `PurchasesTransactionException.userCancelled`;
here those cases are separate types, so a `when` covers all of them.

### 2. "Could not check" is distinct from "not subscribed"

`checkEntitlementOrNull()` returns `null` when the store could not be reached. Apps that persist membership on the device
won't overwrite a paying user's state with "not subscribed" every time the network fails.

### 3. Trial eligibility works on Android too

The official SDK's `checkTrialOrIntroPriceEligibility` **always returns `UNKNOWN` on Android**.
This library's `checkTrialEligibility()` decides `ELIGIBLE` / `INELIGIBLE` from the offers Google Play returns
(Play only returns offers the user can use), so the same code decides whether to show a free trial on iOS and Android.

### 4. Store differences are handled for you

- **Free-trial length**: the App Store puts it in `introductoryDiscount`, Google Play in `subscriptionOptions.freeTrial`.
  `PackageInfo.freeTrial` returns the same `TrialPeriod` for both (App Store pay-as-you-go/pay-up-front intro prices are not counted as free trials).
- **Upgrading to a higher plan**: unless Google Play is given the old product (`product:basePlan`), the user ends up with two subscriptions and is charged for both.
  `purchaseChangingPlan()` looks at the current subscription and buys as a plan change only when needed. On the App Store, or with no subscription, it is a normal purchase.
- **Prices**: `PackageInfo` includes `priceAmountMicros` for computing discounts and `pricePerMonthString` for the per-month price of annual plans.

### 5. Easy to test

The official SDK is called through the `Purchases.sharedInstance` singleton.
This library goes through the `RevenueCatClient` interface, so ViewModel and repository tests can inject a fake
(the tests for `PaywallViewModel` in `paywall-logic` do exactly that).

### 6. Share paywall logic between Compose and SwiftUI

- `PaywallViewModel` / `PaywallState` in `paywall-logic` work from both Compose and SwiftUI
- `FlowHelper` lets Swift observe StateFlows, so you don't need SKIE or KMP-NativeCoroutines
- If you don't need a custom UI, use `DefaultPaywall` from `paywall-compose` as is

The official `purchases-kmp-ui` shows paywalls designed in the RevenueCat dashboard.
This library is for when **you build the paywall UI in your app**, so the two serve different purposes.

## When to use the official SDK directly

- You need features this library does not wrap (customer attributes, promotional offers, RevenueCat Paywalls, etc.)
- You have only one app and nothing to share
- You want to adopt new `purchases-kmp` versions right away (this library follows them with some delay)

You can use this library and the official SDK side by side in the same app. `purchases-kmp` is an `implementation` dependency, though, so to call it directly add `purchases-kmp-core` (the same version) to your app's dependencies.
