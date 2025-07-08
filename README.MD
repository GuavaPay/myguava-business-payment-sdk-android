# MyGuava Payment SDK

## A payment SDK from MyGuava.

## Requirements

- **Android API Level:** `21 (Android 5.0 Lollipop)`
- **Compile SDK:** `36 (Android 14)`
- **Target SDK:** `36 (Android 14)`
- **Java Version:** `21`
- **Kotlin Version:** `2.1.21`

## Dependencies
- **Jetpack Compose:** `BOM 2025.05.01`
- **OkHttp:** `4.12.0`
- **Retrofit:** `3.0.0`
- **Kotlinx Serialization:** `1.8.1`
- **Google Play Services Wallet:** `19.4.0`
- **AndroidX Core:** `1.16.0`
- **AndroidX Lifecycle:** `2.9.0`

## Installing

### 1. Add dependency

```kotlin
dependencies {
  implementation("com.guavapay:paymentsdk:1.0.0")
}
```

### 2. Manual dependency adding

- Create libs directory
- Add the foundation-xxx.aar where xxx is release or debug, to the libs directory.
- Modify your build.gradle.kts and add to implementations this library.
```kotlin
implementation(files("libs/foundation-xxx.aar"))
```

>Important!

Need to add manually dependencies for foundation. **(See point 5)**

## Usage

Using MyGuava Payment SDK is very simple, all you need is to call one method and create one model. UI configuration and more detailed customization will require more code, but in general, everything is very simple.

### 1. Creating Configuration

First, you need to create a configuration for the payment SDK, where you describe which payment methods, card categories, payment systems, theme you would like to see, and also pass the necessary data for the SDK to work (`orderId` and `sessionToken`) that you get after creating an order.

The minimal configuration is very simple:

```kotlin
val order = create()
val payload = PaymentGatewayPayload(order.id, order.sessionToken)
```

Next, you need to create the gateway itself, into which we pass the payload, this is required to launch the SDK.

- If you use View in your project, not Jetpack Compose, then create the gateway like this:

```kotlin
val gateway = PaymentGateway(activity, payload)
```

- If you use Jetpack Compose in your project, then create the gateway like this:

```kotlin
val gateway = rememberPaymentGateway(payload)
```

### 2. Launching SDK

As mentioned above, using the SDK is extremely simple, so launching is not more complex than creating a payload.

```kotlin
PaymentGatewayCoroutineScope().launch {
  when (gateway.start()) {
    PaymentResult.Completed -> x
    PaymentResult.Declined -> x
    PaymentResult.Canceled -> x
    PaymentResult.Failed -> x
  }
}
```

All done! This way the SDK will be launched, and the user will be able to make a payment.

### 3. Environment Selection

To select the environment, you can do it in two ways:

1. Specifying the environment in the manifest:
```xml
<meta-data
  android:name="com.guavapay.paymentsdk.environment"
  android:value="sandbox"/>
```
2. Passing to PaymentGatewayPayload (takes priority over manifest):
```kotlin
val payload = PaymentGatewayPayload(order.id, order.sessionToken, circuit = PaymentCircuit.Sandbox)
```

### 3. UI Customization

UI customization is very simple, since the entire SDK UI is written in Jetpack Compose, it is customized very easily. Especially if you already have Jetpack Compose in your project.

For this, you need to pass the looknfeel parameter to `PaymentGatewayPayload`:

```kotlin
val payload = PaymentGatewayPayload(..., looknfeel = { delegable ->
  MaterialTheme(...) { delegable() }
})
```

For customizing button sizes or individual colors, you can configure this in more detail like this:

By inheriting `PaymentSdkTokens` and `PaymentSdkSizes`, you can change their properties.

```kotlin
val theme = PaymentGatewayLooknfeel { delegable ->
  CompositionLocalProvider(
    LocalTokensProvider provides PaymentSdkTokens(),
    LocalSizesProvider provides PaymentSdkSizes()
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = CustomTypography,
      shapes = CustomShapes,
      content = delegable
    )
  }
}

val payload = PaymentGatewayPayload(..., looknfeel = theme)
```

### 4. Recommendations

1. If you create `PaymentGatewayPayload` in Jetpack Compose, then do it in `remember(x, y) {}`.
2. Always use the custom `PaymentGatewayCoroutineScope`
3. In `PaymentGateway`, a reference to `ComponentActivity` should be passed, not any `Context`.
4. Perform all checks on sandbox. Only if everything works well on it, then use the production circuit.
