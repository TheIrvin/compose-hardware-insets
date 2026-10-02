# Changelog

All notable changes to this project are documented here, in the format of
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). This project follows
[semantic versioning](https://semver.org/spec/v2.0.0.html) from 0.1.0, which means the public API
may change in any 0.x release.

## [Unreleased]

### Added

- `ScreenEdge.toAlignment()`, mapping a horizontal screen edge to a Compose `Alignment`.

### Changed

- **Consumers now need Kotlin 2.3.0 or later.** This is built with Kotlin 2.4.20, and a 2.2
  compiler cannot read 2.4 metadata: it stops with "the binary version of its metadata is 2.4.0,
  expected version is 2.2.0". The fix is the Kotlin plugin version in the consuming build, not
  anything in its code. `0.1.0` was built with 2.2.21.
- **Nothing else about consuming this moved.** `compileSdk 36` and AGP 8.9.1 remain the floor, both
  set by `androidx.activity` and `androidx.core` rather than by this library, and both already true
  of `0.1.0` though the README did not say so. `minSdk` is unchanged at 23.
- The build itself moves to AGP 9.4.1, Gradle 9.8.0 and `compileSdk 37`.
- The Compose BOM and `core-ktx` are deliberately held at `2026.06.01` and `1.18.0`. Taking the
  newer ones would move the consumer floor to `compileSdk 37` and AGP 9.1, which nothing in this
  library needs. [#39](https://github.com/damson/compose-hardware-insets/issues/39) holds the
  condition for raising them.

### Fixed

- **The published aar no longer demands the `compileSdk` this repository happens to build with.**
  AGP 9 defaults a library's `minCompileSdk` to its own `compileSdk`, so moving to `compileSdk 37`
  would have required 37 of every consumer as a side effect of a build change. It is now declared
  explicitly as 30, the highest platform API the code touches.

## [0.1.0] - 2026-10-01

First release. Extracted from a production app and generalised, so every decision that was that
app's rather than the platform's is now a parameter.

### Added

- `CutoutShape` and `View.cutoutShape()`, publishing the display cutout's rectangles and per-edge
  insets as Compose state refreshed on every inset dispatch.
- `View.stopReportingCutoutShape()`, to remove that listener.
- `hardwareInsets()` and `Modifier.clearOfTheHardware()`, taking a `HardwarePolicy` for which insets
  count and an `isFarEdgeIgnored` flag for hosts that must not grow on the far edge.
- `cornerClearance()` and the pure `cornerClearanceFor()`, which move a corner control clear of only
  the cutout rectangles it actually overlaps.
- `ScreenEdge` and `ScreenEdge.onScreenAt()`, mapping a chosen device edge onto the screen edge it
  has become under a given rotation.
- `drawBehindTheHardware()` taking a `CutoutMode` and both `SystemBarStyle`s, and
  `hideTheSystemBars()` taking a type mask and a behaviour.

### Known gaps

- `cornerClearance` returns a zero offset for `ScreenEdge.LEFT` and `ScreenEdge.RIGHT`, documented
  rather than implemented.
- `HardwarePolicy` has no rounded-corner term; the platform reports corners as a radius, not an
  inset.

[Unreleased]: https://github.com/damson/compose-hardware-insets/compare/0.1.0...HEAD
[0.1.0]: https://github.com/damson/compose-hardware-insets/releases/tag/0.1.0
