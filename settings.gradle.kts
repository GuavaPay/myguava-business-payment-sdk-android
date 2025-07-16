pluginManagement {
  repositories {
    google { content { listOf("com\\.android.*", "com\\.google.*", "androidx.*").forEach(::includeGroupByRegex) } }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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

rootProject.name = "paymentsdk"
include(":application")
include(":foundation")
