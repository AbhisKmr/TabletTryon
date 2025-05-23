pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://sdk.developer.deepar.ai/maven-android-repository/releases/")
    }
}

rootProject.name = "TabletTryon"
include(":app")
