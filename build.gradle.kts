// project-level build.gradle.kts
plugins {
    id("com.android.application") version libs.versions.agp apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("com.google.devtools.ksp") version libs.versions.ksp apply false
    // FIX: Use the hyphenated alias here too
    alias(libs.plugins.compose.compiler) apply false
}