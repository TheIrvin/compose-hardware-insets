---
name: Bug
about: Something behaves differently from what it says
labels: bug
---

**What you expected, and what happened instead.**

**The hardware.** Device or emulator, and what it has in the way: a punch hole, a notch, a waterfall
curve, a chin. A screenshot with the control in the wrong place says more than a description.

**API level**, because most of this library is gated on 28 and 30.

**A failing test, if you can.** The tests here build a `WindowInsetsCompat` and dispatch it, so a
cutout you do not own can usually be reproduced without the device. See `CutoutShapeTest`.
