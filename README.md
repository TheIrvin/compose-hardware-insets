# compose-hardware-insets

Compose tells you how deep the display cutout goes into each edge. It does not tell you **where**
the camera is, so there is no supported way to ask whether the hardware is actually in the way of
one particular control.

This answers that. It publishes the cutout's rectangles as Compose state, turns them into an offset
for a control tucked into a corner, and keeps the pure geometry public so you can test it against
hardware you do not own.

```kotlin
dependencies {
    implementation("io.github.damson:hardware-insets:0.1.0")
}
```

## Pad content clear of the hardware

```kotlin
Box(
    Modifier
        .background(MyColours.panel)      // the background keeps the full width
        .clearOfTheHardware(ScreenEdge.TOP)  // only what is inside it backs off
) {
    ActionRow()
}
```

By default that means the display cutout and the waterfall curve, and all four edges. Both are
choices:

```kotlin
// Include the system bars too, for an app that shows them
Modifier.clearOfTheHardware(policy = HardwarePolicy(areSystemBarsIncluded = true))

// Leave the far edge at zero, for a wrap_content ComposeView that would
// otherwise grow and swallow touches inside the growth
Modifier.clearOfTheHardware(ScreenEdge.TOP, isFarEdgeIgnored = true)
```

## Move one control clear of the camera

The full-width inset is the wrong question. A centred punch-hole reports an inset across the whole
width, which would drop a corner button a centimetre to avoid a camera it is nowhere near. So read
the rectangles and count only the ones the control overlaps:

```kotlin
val cutout by someSiblingView.cutoutShape()          // live state, not a reading
val clearance = cornerClearance(cutout.bounds, ScreenEdge.TOP, isAtTheEnd = true)

Box(Modifier.offset { clearance(buttonWidthPx) }) { CloseButton() }
```

**Do not register that on the `ComposeView` itself.** A View holds exactly one
`OnApplyWindowInsetsListener` and `setContent` claims it, silently: the dispatch still arrives, the
listener is just no longer yours, and the value stays at its default. Use a sibling, and call
`stopReportingCutoutShape()` when you are done with it.

## Lay the window out behind the hardware

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawBehindTheHardware(cutoutMode = CutoutMode.SHORT_EDGES)
        // ALWAYS suits a surface that fills the window. Most apps want SHORT_EDGES.
    }
}
```

`hideTheSystemBars()` is a separate call, because hiding the bars and drawing behind them are
separate decisions. It takes a type mask, so you can keep the clock.

## Why the tests are the interesting part

There is no resource qualifier for a display cutout, and none at all for a curved edge. A test that
waited for a device with the hardware would only ever run on someone's desk. Every test here builds
a `WindowInsetsCompat` and dispatches it through a real layout instead, which is why the geometry is
covered on hardware nobody in this project owns.

`cornerClearanceFor` is public for the same reason it is pure: so you can ask it about a punch-hole,
a chin, a notch and three overlapping rectangles without owning any of them.

## Supported

- `minSdk 23`, which is Compose's floor rather than this library's. The cutout API arrives at API
  28 and the waterfall at API 30; below each, the platform reports nothing and this reports zero, so
  there is nothing to branch on in your code.

## Status

`0.1.0` generalises code that has been in production in one app. The API is expected to move before
`1.0`; see [CHANGELOG.md](CHANGELOG.md) and the roadmap below.

## Roadmap

- `rememberCutoutShape()` and a `LocalCutoutShape`, so a Compose-only app never touches a `View`.
- Rounded-corner insets. The platform reports corners as a radius from API 31 rather than as an
  inset, and Compose does not expose them either.
- A safest-edge chooser: given the shape, which edge has the least hardware in it.
- `Modifier.avoidHardware()`, offsetting a composable clear of any rectangle it overlaps.
- Fold and hinge support through `androidx.window`. The same problem: hardware in the way.
- A debug overlay drawing the cutout rectangles, the waterfall and the safe areas over your UI.
- A `-testing` artifact shipping the fake-cutout fixtures.
- `cornerClearance` on a vertical edge. It answers zero there today and says so; supporting it means
  measuring a control's height against a side, which is a different calculation.

## Licence

Apache 2.0. See [LICENSE](LICENSE).
