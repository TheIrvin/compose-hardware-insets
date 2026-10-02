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
                includes {
                    classes("com.devddagnet.hardwareinsets.lib.domain.*")
                }
            }

            verify {
                rule("The pure geometry is covered completely") {
                    // Reached, not aspired to. Nothing in `domain` touches a
                    // framework object it cannot be handed, so these are exactly
                    // the functions the README argues you can test against hardware
                    // you do not own.
                    //
                    // The cost of the branch half, which is real: an inline stdlib
                    // call brings its own branches into this package's count, so
                    // two of the three this gate first closed were inside
                    // `maxOfOrNull` rather than in code written here. The next
                    // `sumOf` or `associate` in `domain` can add a branch
                    // reachable only by input shaped to the standard library. When
                    // that happens the honest fix is a test of the behaviour that
                    // needs the shape, or a narrower bound, not a test written to
                    // the counter.
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
