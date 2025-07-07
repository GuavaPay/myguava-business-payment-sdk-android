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
      url = uri("https://gitlab.guavapay.com/api/v4/projects/1110/packages/maven")
      name = "GitLab"
      credentials {
        val localProperties = java.util.Properties()
        val localPropertiesFile = File(rootDir, "local.properties")
        if (localPropertiesFile.exists()) {
          localPropertiesFile.inputStream().use { localProperties.load(it) }
        }
        username = localProperties.getProperty("gitlab.maven.username") ?: ""
        password = localProperties.getProperty("gitlab.maven.password") ?: ""
      }

      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
}

rootProject.name = "paymentsdk"
include(":application")
include(":foundation")
