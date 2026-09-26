# Contributing

Pull requests are welcome. You do not need to ask first.

## The one command

```
JAVA_HOME=<a JDK 21> ./gradlew check apiCheck
```

That is everything CI runs. If it passes locally it passes there.

**JDK 21 is required**, not optional. Robolectric loads the Android jar for the emulated SDK, and
the API 36 jar refuses to load under anything earlier. On JDK 17 every test fails in setup with
`Failed to create a Robolectric sandbox`, which reads like broken tests rather than a wrong JDK.

## What a change needs

- **A test that has been seen failing.** Break what your new test guards, watch it go red, put it
  back. A check that has never failed is not known to check anything, and the bugs this library
  exists to catch are all silent: an inset on the wrong edge looks like a layout choice.
- **A public declaration documented where a caller could not work it out.** Units, coordinate space,
  which API level starts reporting the thing, what a name misleads about. Not a restatement of the
  signature.
- **`./gradlew apiDump` run and the result committed** if you changed the public API. `apiCheck`
  fails the build otherwise, which is the point: an accidental break should cost us a red build
  rather than costing a consumer a broken one.

## Testing hardware you do not have

There is no resource qualifier for a display cutout and none at all for a curved edge, so the tests
build a `WindowInsetsCompat` and dispatch it through a real layout. Add to that rather than reaching
for a device: a test that needs particular hardware only ever runs on one desk.

`cornerClearanceFor` is pure and public. Geometry belongs there, where it can be tested without a
window at all.

## Scope

This library is about hardware that is physically in the way of your UI: cutouts, waterfall curves,
and later folds and rounded corners. Insets that are software, such as the IME or the system bars,
belong to Compose and androidx, and are offered here only as a policy flag rather than reimplemented.
