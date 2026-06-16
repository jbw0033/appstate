pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("/usr/local/google/home/jbwoods/androidx-main/out/repository")
        maven {
            url = uri("https://androidx.dev/snapshots/builds/15657030/artifacts/repository")
        }
    }
}

rootProject.name = "WeatherAppState"
include(":app")
include(":navigation3-appstate")

 