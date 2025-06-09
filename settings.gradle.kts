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
    }
}

rootProject.name = "paymentsdk"
include(":application")
include(":foundation")
