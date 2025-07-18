plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.guavapay.paymentsdk.demo"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.guavapay.paymentsdk"

    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("debug")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  kotlinOptions {
    jvmTarget = "21"
    // todo: remove skip prerelease check flag, but need for development from EAP idea *okay.jpg*
    freeCompilerArgs = freeCompilerArgs + "-Xskip-prerelease-check"
  }

  buildFeatures {
    compose = true
  }
}

dependencies {
//  implementation(project(":foundation"))
  implementation("com.guavapay.myguava.business:payment-sdk-android:0.1.0")

  implementation(libs.androidx.core)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.lifecycle.viewmodel)
  implementation(libs.androidx.activity.compose)

  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui.core)
  implementation(libs.compose.ui.graphics)
  implementation(libs.androidx.activity.ktx)

  implementation(libs.material)
  implementation(libs.androidx.material3.android)

  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp.core)
  implementation(libs.okhttp.logging)

  implementation(libs.kotlinx.serialization)
}
