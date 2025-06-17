// settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()             // PENTING: Pastikan ini ada!
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()             // PENTING: Pastikan ini ada!
        mavenCentral()
    }
}

rootProject.name = "DearDiary"
include(":app")
