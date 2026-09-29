plugins {
    // Both declared here, or a subproject asking for the other one is told the
    // plugin is already on the classpath with an unknown version.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.binary.compatibility)
}
