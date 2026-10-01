import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    // Both declared here, or a subproject asking for the other one is told the
    // plugin is already on the classpath with an unknown version.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.binary.compatibility)
    alias(libs.plugins.kover)
}

apiValidation {
    // The sample is not part of the published surface.
    ignoredProjects.add("sample")
}

// A second report, aggregating the library, filtered to the one package that has
// no excuse. Kover 0.9 has no per-rule filters, so a package-scoped gate and a
// whole-library gate cannot live in the same report: the module keeps the 90%
// line gate, and the 100% one lives here.
dependencies {
    kover(project(":hardware-insets"))
}

kover {
    reports {
        // Inside `total`, not beside it: filters set on `reports` itself are
        // defaults that the aggregated report does not pick up, and the gate then
        // measures the whole library and reads as a failure of the rule rather
        // than of the configuration.
        total {
            filters {
                excludes {
                    classes(
                        "com.devddagnet.hardwareinsets.lib.HardwareInsetsKt",
                        "com.devddagnet.hardwareinsets.lib.platform.*",
                    )
                }
            }

            verify {
                rule("The pure geometry is covered completely") {
                    // Reached, not aspired to. Nothing in `domain` touches a
                    // framework object it cannot be handed, so these are exactly
                    // the functions the README argues you can test against hardware
                    // you do not own. An uncovered branch here is the one kind this
                    // project has no excuse for.
                    bound {
                        minValue = 100
                        coverageUnits = CoverageUnit.LINE
                    }
                    bound {
                        minValue = 100
                        coverageUnits = CoverageUnit.BRANCH
                    }
                }
            }
        }
    }
}
