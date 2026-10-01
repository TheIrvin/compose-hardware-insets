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

## Cutting a release

**Bump the version before the tag, in both build files.** `hardware-insets/build.gradle.kts` and
`sample/build.gradle.kts` each declare it, and a workflow checks all three agree. It checks after the
release is published, because that is the first moment it can run, so a disagreement is reported and
not prevented: the release sits there with no assets until the versions are corrected and the tag
re-cut.

**The tag carries no `v`.** JitPack's coordinate is the tag itself, so `v0.2.0` would be served as
version `v0.2.0` while every file and the README say `0.2.0`.

**Promote to `main` before tagging there.** A workflow run uses the file present at the ref the event
carries, and for a release that ref is the tag, with no fall back to the default branch. So a tag whose
commit predates `.github/workflows/release.yml` publishes a release that attaches nothing, and no check
goes red to say so.

Then publish a release for the tag. Attaching the artifacts is automatic from there, and two of them
come with caveats worth knowing before anyone relies on them.

- **The aar is for reading and archiving. JitPack is how you depend on this.** The aar's own
  coordinates are `com.devddagnet:hardware-insets`, which resolves nowhere, and JitPack serves
  `com.github.damson:compose-hardware-insets`. The pom and the Gradle metadata are attached beside it,
  so a consumer who does drop it into `libs/` can at least see the Compose and activity dependencies
  it needs.
- **The sample apk is debug-signed, and every release's is signed by a different key.** The runner
  generates one when none exists, so consecutive samples cannot upgrade over each other: uninstall the
  previous one first. Its `versionCode` is 1 and stays there, which would block an upgrade on its own
  even if the keys matched. It is also debuggable and unminified, which is fine for reading and is not a
  release build of anything.
