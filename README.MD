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

### 5. Manual dependencies.

```kotlin
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)
  implementation(libs.okhttp.sse)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.serialization)

  implementation(libs.kotlinx.serialization)

  implementation(libs.play.services.wallet)
  implementation(libs.play.services.wallet.compose)

  implementation(libs.androidx.core)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.startup)

  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.core)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.material)
  implementation(libs.androidx.material3.android)
```

With these declarations in `libs.versions.toml`:

```toml
[versions]
androidx-core = "1.16.0"
androidx-lifecycle = "2.9.0"
androidx-activity = "1.10.1"
androidx-appcompat = "1.7.0"
androidx-startup = "1.2.0"
androidx-espesso-core = "3.6.1"
androidx-junit = "1.2.1"

nav3Core = "1.0.0-alpha01"
lifecycleViewmodelNav3 = "1.0.0-alpha01"

compose-bom = "2025.05.01"

okhttp = "4.12.0"
retrofit = "3.0.0"

kotlinx-serialization = "1.8.1"

play-services-wallet = "19.4.0"
play-services-wallet-compose = "1.1.0"

junit = "4.13.2"
material = "1.12.0"
material3-android = "1.3.2"
activity-ktx = "1.10.1"

[libraries]
androidx-core = { group = "androidx.core", name = "core-ktx", version.ref = "androidx-core" }
androidx-lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "androidx-lifecycle" }
androidx-lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle" }
androidx-lifecycle-viewmodel-navigation3 = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-navigation3", version.ref = "lifecycleViewmodelNav3" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "androidx-activity" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "androidx-appcompat" }
androidx-startup = { group = "androidx.startup", name = "startup-runtime", version.ref = "androidx-startup" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "androidx-espesso-core" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidx-junit" }

compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui-core = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }

androidx-navigation3-runtime = { group = "androidx.navigation3", name = "navigation3-runtime", version.ref = "nav3Core" }
androidx-navigation3-ui = { group = "androidx.navigation3", name = "navigation3-ui", version.ref = "nav3Core" }

okhttp-bom = { group = "com.squareup.okhttp3", name = "okhttp-bom", version.ref = "okhttp" }
okhttp-core = { group = "com.squareup.okhttp3", name = "okhttp" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor" }
okhttp-sse = { group = "com.squareup.okhttp3", name = "okhttp-sse" }
retrofit-core = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-serialization = { group = "com.squareup.retrofit2", name = "converter-kotlinx-serialization", version.ref = "retrofit" }

kotlinx-serialization = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
play-services-wallet = { group = "com.google.android.gms", name = "play-services-wallet", version.ref = "play-services-wallet" }
play-services-wallet-compose = { group = "com.google.pay.button", name = "compose-pay-button", version.ref = "play-services-wallet-compose" }

material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-material3-android = { group = "androidx.compose.material3", name = "material3-android", version.ref = "material3-android" }
androidx-activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activity-ktx" }
```

>Also don't forget about 3ds library, it need to be gathered from our team. And also added to dependencies as aar!