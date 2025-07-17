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

### 1. Add repository and dependency

```kotlin
repositories {
  maven {
    url = uri("https://maven.guavapay.com/public")
    name = "GuavaPay Maven Repository"

    content {
      includeGroupAndSubgroups("com.guavapay")
    }
  }
}
```

```kotlin
dependencies {
  implementation("com.guavapay.myguava.business:payment-sdk-android:0.1.0")
}
```

## Usage

Using MyGuava Payment SDK is very simple, all you need is to call one method and create one model. UI configuration and more detailed customization will require more code, but in general, everything is very simple.

### 1. Creating Configuration

First, you need to create a configuration for the payment SDK, where you describe which payment methods, card categories, payment systems, theme you would like to see, and also pass the necessary data for the SDK to work (`orderId` and `sessionToken`) that you get after creating an order.

> **WARNING:**  
> Register orders using your backed.
>
> Do not pass the API key to the mobile client. Only the session token and order ID created on order registration must be passed to the mobile client.


The minimal configuration is very simple:

```kotlin
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
    PaymentResult.Success -> x
    PaymentResult.Unsuccess -> x
    PaymentResult.Error -> x
    PaymentResult.Cancel -> x
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
where:
* `android:value="sandbox"` - sandbox environment
* `android:value="production"` - production environment


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

Additionally, you can customize the appearance of the 3DS SDK that we use (the window with OTP request for transaction approval) in the same simple way by passing one parameter `threedsLooknfeel`.

```kotlin
val payload = PaymentGatewayPayload(..., threedsLooknfeel = GUiCustomization(...), looknfeel = theme)
```

If `threedsLooknfeel` is not explicitly configured, the default MyGuava color theme will be used.

### 4. Recommendations

1. If you create `PaymentGatewayPayload` in Jetpack Compose, then do it in `remember(x, y) {}`.
2. Always use the custom `PaymentGatewayCoroutineScope`
3. In `PaymentGateway`, a reference to `ComponentActivity` should be passed, not any `Context`.
4. Perform all checks on sandbox. Only if everything works well on it, then use the production circuit.
