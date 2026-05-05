pluginManagement {
    repositories {
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
    }
}

rootProject.name = "CarFlow"
include(":app")
include(":shared:parser")
include(":shared:network")
include(":shared:network")
