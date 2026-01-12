buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // SafeArgs plugin pro navigaci
        val nav_version = "2.8.5"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}

plugins {

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}