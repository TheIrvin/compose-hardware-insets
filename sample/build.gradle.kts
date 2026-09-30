plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.github.damson.hardwareinsets.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.damson.hardwareinsets.sample"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            // Unsigned, and never released. The sample exists to be run and
            // read, not to ship.
            isMinifyEnabled = false
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // The published coordinates would work here too. A project dependency is
    // deliberate: an API change breaks the sample in the same build rather
    // than after a release.
    implementation(project(":hardware-insets"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    // Real icons rather than glyphs borrowed from the keyboard.
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(composeBom)
    testImplementation(libs.junit)
    testImplementation(libs.assertj)
}
