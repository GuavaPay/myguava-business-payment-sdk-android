import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  `maven-publish`
}

val versionName = "0.0.1"

android {
  namespace = "com.guavapay.paymentsdk"
  compileSdk = 36

  defaultConfig {
    minSdk = 21

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")

    base.archivesName.set("myguava-business-payment-sdk-android-v$versionName")
  }

  sourceSets.named("main") {
    manifest.srcFile("src/main/AndroidManifest.xml")
    res.srcDirs("src/main/res")
  }

  lint {
    disable += "EnsureInitializerMetadata"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

    debug {
      isMinifyEnabled = false
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  buildFeatures {
    compose = true
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
  }
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    if (name.contains("release", ignoreCase = true)) {
      freeCompilerArgs.addAll(
        listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-progressive",
          "-Xlambdas=indy",
          "-Xno-call-assertions",
          "-Xno-param-assertions",
          "-Xno-receiver-assertions",
          "-Xno-source-debug-extension",
          "-Xenable-incremental-compilation",
          "-Xstring-concat=indy-with-constants",
          "-Xvalue-classes",
          "-Xnew-inference",
          "-Xcontext-parameters",
          "-Xsuppress-warning=UNCHECKED_CAST,CONTEXT_RECEIVERS_DEPRECATED,NOTHING_TO_INLINE"
        )
      )
    } else {
      freeCompilerArgs.addAll(
        listOf(
          "-opt-in=kotlin.RequiresOptIn",
          "-progressive",
          "-Xlambdas=indy",
          "-Xjvm-default=all",
          "-Xenable-incremental-compilation",
          "-Xstring-concat=indy-with-constants",
          "-Xvalue-classes",
          "-Xnew-inference",
          "-Xcontext-parameters",
          "-Xsuppress-warning=UNCHECKED_CAST,CONTEXT_RECEIVERS_DEPRECATED,NOTHING_TO_INLINE"
        )
      )
    }
  }
}

composeCompiler {
  if (gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }) {
    includeSourceInformation.set(false)
    includeTraceMarkers.set(false)
  } else {
    includeSourceInformation.set(true)
    includeTraceMarkers.set(true)
  }
}

dependencies {
  api(libs.myguava.xds2.android) { isChanging = true }

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

  debugImplementation(libs.compose.ui.tooling)
  debugImplementation(libs.compose.ui.test)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.compose.bom))
}

publishing {
  publications {
    create<MavenPublication>("release") {
      afterEvaluate {
        from(components["release"])
      }

      groupId = "com.guavapay.myguava.business"
      artifactId = "payment-sdk-android"
      version = android.defaultConfig.versionName ?: "0.0.1"

      artifact(tasks.register("androidSourcesJar", Jar::class) {
        archiveClassifier.set("sources")
        from(android.sourceSets.getByName("main").java.srcDirs)
      })
    }
  }

  repositories {
    maven {
      url = uri("https://nexus.guavapay.com/repository/maven-releases/")
      name = "Nexus"
      credentials {
        val localProperties = Properties()
        val localPropertiesFile = File(rootDir, "local.properties")
        if (localPropertiesFile.exists()) {
          localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        username = localProperties.getProperty("nexus.maven.username") ?: ""
        password = localProperties.getProperty("nexus.maven.password") ?: ""
      }

      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
}
