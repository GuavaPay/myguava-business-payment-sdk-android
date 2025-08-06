<p align="center">
  <img src="media/header.png" alt="./media/header"/>
</p>

<p align="center">
  <img alt="GitHub Tag" src="https://img.shields.io/github/v/tag/GuavaPay/myguava-business-payment-sdk-android?include_prereleases&style=for-the-badge&logoColor=BAF400&label=Version&color=BAF400">
</p>

# MyGuava Payment SDK

MyGuava Payment SDK is a comprehensive library for Android that simplifies the integration of various payment methods into your application. It provides a customizable UI and handles the complexities of payment processing, allowing you to focus on your core business logic.

## 📖 Table of Contents

- [MyGuava Payment SDK](#myguava-payment-sdk)
  - [📖 Table of Contents](#-table-of-contents)
  - [✨ Features](#-features)
  - [📸 Screenshots](#-screenshots)
  - [⚙️ Requirements](#️-requirements)
  - [📦 Dependencies \& Size](#-dependencies--size)
  - [🚀 Installation](#-installation)
    - [1. Add the repository](#1-add-the-repository)
    - [2. Add the dependency](#2-add-the-dependency)
  - [▶️ Usage](#️-usage)
    - [1. Creating Configuration](#1-creating-configuration)
    - [2. Launching the SDK](#2-launching-the-sdk)
    - [3. Environment Selection](#3-environment-selection)
    - [4. UI Customization](#4-ui-customization)
  - [💡 Recommendations](#-recommendations)

## ✨ Features

- **Simple Integration:** Get started with just a few lines of code.
- **Customizable UI:** Easily adapt the look and feel to match your app's branding using Jetpack Compose or Views.
- **Multiple Payment Methods:** Supports payments via new cards, saved cards, and Google Pay.

## 📸 Screenshots

| Main Payment Screen                           | Personal Data Entry                              | Saved Card Payment                             |
|-----------------------------------------------|--------------------------------------------------|------------------------------------------------|
| <img src="./media/srcn_main.png" width="250"> | <img src="./media/srcn_contact.png" width="250"> | <img src="./media/srcn_saved.png" width="250"> |

## ⚙️ Requirements

- **Android API Level:** `21` (Android 5.0 Lollipop) or higher
- **Compile SDK:** `36` or higher
- **Java Version:** `17` or higher
- **Kotlin Version:** `2.1.21` or higher

## 📦 Dependencies & Size

The SDK relies on modern Android libraries to provide a robust and seamless experience.

| Dependency                      | Version          |
|---------------------------------|------------------|
| **Jetpack Compose**             | `BOM 2025.05.01` |
| **OkHttp**                      | `4.12.0`         |
| **Retrofit**                    | `3.0.0`          |
| **Kotlinx Serialization**       | `1.8.1`          |
| **Google Play Services Wallet** | `19.4.0`         |
| **AndroidX Core**               | `1.16.0`         |
| **AndroidX Lifecycle**          | `2.9.0`          |

**Approximate SDK Size:** The core library adds approximately **~1.2 MB** to your app's download size.

## 🚀 Installation

### 1. Add the repository

First, add the GuavaPay Maven repository to your project's `settings.gradle.kts` file:

```kotlin
dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    maven {
      url = uri("https://maven.guavapay.com/public")
      name = "GuavaPay Maven Repository"

      content {
        includeGroupAndSubgroups("com.guavapay")
      }
    }
  }
}
```

### 2. Add the dependency

Next, add the Payment SDK dependency to your module's `build.gradle.kts` file:

```kotlin
dependencies {
  implementation("com.guavapay.myguava.business:payment-sdk-android:0.5.0")
}
```

## ▶️ Usage

Using MyGuava Payment SDK is very simple, all you need is to call one method and create one model. UI configuration and more detailed customization will require more code, but in general, everything is very simple.

### 1. Creating Configuration

First, create a `PaymentGatewayPayload` with the necessary data (`orderId` and `sessionToken`) that you receive from your backend after creating an order.

> **⚠️ Warning:**
> Always register orders on your backend. Never expose your API key on the client-side. Only the `sessionToken` and `orderId` should be passed to the mobile client.

A minimal configuration is very simple:

```kotlin
val payload = PaymentGatewayPayload(order.id, order.sessionToken)
```

Next, create the gateway instance.

- **For View-based projects:**

```kotlin
val gateway = PaymentGateway(activity, payload)
```

- **For Jetpack Compose projects:**

```kotlin
val gateway = rememberPaymentGateway(payload)
```

### 2. Launching the SDK

Launching the SDK is straightforward. Use the provided `PaymentGatewayCoroutineScope` to handle the lifecycle and receive the result.

```kotlin
PaymentGatewayCoroutineScope().launch {
  when (val result = gateway.start()) {
    is PaymentResult.Success -> { /* Handle success */ }
    is PaymentResult.Unsuccess -> { /* Handle unsuccess */ }
    is PaymentResult.Error -> { /* Handle error */ }
    is PaymentResult.Cancel -> { /* Handle cancellation */ }
  }
}
```

✅ All done! This way the SDK will be launched, and the user will be able to make a payment.

### 3. Environment Selection

You can select the environment in two ways:

1.  **Via Manifest (Recommended for production builds):**

    ```xml
    <!-- AndroidManifest.xml -->
    <meta-data
      android:name="com.guavapay.paymentsdk.environment"
      android:value="production" /> <!-- or "sandbox" -->
    ```

2.  **Via `PaymentGatewayPayload` (Overrides manifest):**

    ```kotlin
    val payload = PaymentGatewayPayload(
        orderId = order.id,
        sessionToken = order.sessionToken,
        circuit = PaymentCircuit.Sandbox
    )
    ```

### 4. UI Customization

The SDK's UI is built with Jetpack Compose, making it highly customizable. To apply your own theme, pass a `looknfeel` lambda to the `PaymentGatewayPayload`.

```kotlin
val payload = PaymentGatewayPayload(
  // ... other params
  looknfeel = PaymentGatewayPayload.PaymentGatewayLooknfeel { content ->
    YourAppTheme {
      content()
    }
  }
)
```

For more granular control over component colors and sizes, you can extend `PaymentSdkTokens` and `PaymentSdkSizes` and provide them through a `CompositionLocalProvider`.

```kotlin
val customTheme = PaymentGatewayPayload.PaymentGatewayLooknfeel { content ->
  // You can override entire theming by extending theme from PaymentSdkTokens or PaymentSdkSizes:

  val customTokens = object : PaymentSdkTokens() { /* ... override colors ... */ }
  val customSizes = object : PaymentSdkSizes() { /* ... override sizes ... */ }

  // Or override with defaults:

  val customSizes = PaymentSdkSizes().button().copy(height = 54.dp /* Overrides button height */)
  val customTokens = PaymentSdkTokens().button().copy(containerColor = Color(0xFFFF00FF) /* Overrides button background color */)

  CompositionLocalProvider(
    LocalTokensProvider provides customTokens,
    LocalSizesProvider provides customSizes
  ) {
    YourAppTheme { content() }
  }
}

val payload = PaymentGatewayPayload(
  // ... other params
  looknfeel = customTheme
)
```

## 💡 Recommendations

1.  When creating `PaymentGatewayPayload` in Jetpack Compose, wrap it in `remember(key1, key2) { ... }` to avoid unnecessary recompositions.
2.  Always use the provided `PaymentGatewayCoroutineScope()` to launch the payment flow.
3.  Ensure you pass a `ComponentActivity` instance to `PaymentGateway`, not a generic `Context`.
4.  Thoroughly test your integration in the **sandbox** environment before switching to **production**.
