# Changelog

All notable changes to this project are documented here, in the format of
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/). This project follows
[semantic versioning](https://semver.org/spec/v2.0.0.html) from 0.1.0, which means the public API
may change in any 0.x release.

## [Unreleased]

## [0.1.0]

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
