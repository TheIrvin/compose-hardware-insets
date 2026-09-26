plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

android {
    namespace = "io.github.damson.hardwareinsets"
    compileSdk = 36

    defaultConfig {
        // 23 is Compose's own floor, not this library's: the cutout API arrives
        // at 28 and the waterfall at 30, and below each the platform reports
        // nothing and this reports zero. Lowering it fails the manifest merge
        // against foundation-layout rather than failing at runtime.
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            all {
                // Robolectric reaches jdk.internal.access to set up the
                // application state, which java.base exports to nobody. Without
                // this every Robolectric test dies before it runs, with
                // "Failed to interact with raw FileDescriptor internals;
                // perhaps JRE has changed?", which names neither the module nor
                // the flag.
                it.jvmArgs("--add-exports=java.base/jdk.internal.access=ALL-UNNAMED")
            }
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.activity)

    val composeBom = platform(libs.androidx.compose.bom)
    api(composeBom)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.ui)

    testImplementation(composeBom)
    // Only the tests paint a background to see what moved inside it; the
    // library itself needs layout and runtime, not all of foundation.
    testImplementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.compose.ui.test.manifest)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.github.damson"
            artifactId = "hardware-insets"
            version = "0.1.0"
            afterEvaluate { from(components["release"]) }
        }
    }
}
