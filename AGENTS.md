# Working on this library

One Gradle module, `hardware-insets`. Four source files, and the tests are larger than the sources
on purpose.

## The one command

```
JAVA_HOME=<a JDK 21> ./gradlew check apiCheck
```

**JDK 21 is required.** Robolectric loads the Android jar for the emulated SDK and the API 36 jar
refuses to load under anything earlier. Under JDK 17 every test fails in setup with `Failed to
create a Robolectric sandbox`, reported as `classMethod FAILED`, which looks like broken tests
rather than a wrong JDK.

## What each file answers

| File | Question it answers |
|---|---|
| `ScreenEdge.kt` | Which edge, and which screen edge a device edge has become under a rotation |
| `CutoutShape.kt` | Where the cameras are, as Compose state that follows the window |
| `HardwareInsets.kt` | How far off each edge content must sit, and how far one corner control must move |
| `EdgeToEdge.kt` | How the window meets the hardware, and whether the bars are on screen |

`cornerClearanceFor` is the pure core. Geometry goes there, where it needs no window.

## Rules that will cost you if you miss them

- **A comment earns its place only as a platform trap, the derivation behind a number, or a decision
  the code cannot show.** Delete it when the declaration already says it, when a test already
  enforces it, or when it narrates the change rather than the code. The KDoc bar is different and
  higher: a public declaration gets documentation wherever a caller reading only the signature would
  still be missing something, such as units, coordinate space, or which API level starts reporting
  the value. Neither bar is satisfied by restating the name.
- **A test that has never failed is not known to test anything.** Break what a new test guards,
  watch it go red, put it back. Every bug in this area is silent: a wrong inset looks like a layout
  choice and no screenshot moves.
- **`./gradlew apiDump` after any public API change**, and commit `hardware-insets/api/*.api`.
  `apiCheck` fails otherwise.
- **A test configured below API 30 cannot touch `android.view.WindowInsets.Type`.** Those methods
  arrived in API 30, so a companion object that reads one fails in its static initialiser before any
  test runs, and the error names the missing method rather than the reason. Use
  `WindowInsetsCompat.Type`.
- **Never assert on hardware.** There is no resource qualifier for a cutout and none at all for a
  curved edge. Build a `WindowInsetsCompat` and dispatch it through a real layout, as the existing
  tests do.

## Things deliberately not done

- `cornerClearance` answers a zero offset on a vertical edge. Documented, not implemented: a side
  needs a control's height measured against it, which is a different calculation.
- `HardwarePolicy` has no rounded-corner term. The platform reports corners as a radius from API 31,
  not as an inset, so converting one needs the caller's own corner shape.
- `explicitApi()` is not switched on. The API dump is what guards the surface.
