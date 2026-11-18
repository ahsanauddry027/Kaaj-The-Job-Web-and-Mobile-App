// Top-level build file.
//
// AGP 9 compiles Kotlin by itself ("built-in Kotlin") and ships with Kotlin 2.2.10. The classpath line
// below moves the whole build to Kotlin 2.4.21, which is what Android's AGP 9 notes recommend
// ("Upgrade to a higher KGP version"). The Compose and serialization compiler plugins use the same version.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
