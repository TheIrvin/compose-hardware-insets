<!--
Work into `develop`, which is almost all of it. A promotion to `main` or a hotfix
has its own template, and CONTRIBUTING.md says how to reach them.

Every example below is from this repository's own history. Delete any heading that
would be empty rather than writing "n/a" under it.
-->

## Summary

What was wrong, and what is true now. Two or three sentences, in plain words, no
identifiers: the detail goes under *What changed*.

<!--
Example, from the pull request that removed this library's copy of the insets code
from the app it was extracted from:

  The hardware inset code lived in two places: here, and in the library extracted
  from it. This repo now consumes the published release and keeps no copy, so the
  behaviour has one home and one test suite. Nothing on screen changes.

What makes it work: the first sentence is the problem, the second is the state now,
and the third pre-empts the question a reader will have. No file names, no task
names.
-->

## What changed

The shape of the change, not a file list, which the diff already gives. Worth a line
each: a decision and the alternative it rejected, anything a reader would expect to
be here and will not find, and anything that moved beyond the obvious blast radius.

If a fact you changed is written in more than one place, say where the other copies
are. The same command lives in `CONTRIBUTING.md`, `AGENTS.md` and this template, and
a change that updates two of the three leaves a contributor following the stale one.

<!--
Example of a decision with its alternative:

  `asSurfaceRotation()` is a private extension here rather than something the library
  offers. The library deliberately does not hand out `Surface` constants, and this
  host is the only thing that needs one. The alternative is to give the canvas the
  named type and delete the Int entirely, which is tidier and widens the change into
  two more tests. Worth doing, not worth doing in a fix.

Example of naming what is absent:

  `cornerClearance` still answers zero for a vertical edge. Supporting it means
  measuring a control's height against a side, which is a different calculation, so
  it is documented rather than implemented.
-->

## Test plan

- [ ] `./gradlew check apiCheck koverVerify :hardware-insets:koverVerify` green, on JDK 21
- [ ] Any new test seen failing once, against the mistake it exists to catch, and the
      test that died was the one meant to
- [ ] `./gradlew apiDump` run and `api/*.api` committed, if the public API moved

For a public declaration: KDoc that says what the signature cannot, which is units,
coordinate space, what a caller has to do first, and what the name misleads about.

For anything in `domain`: it is gated at 100% of lines and branches, so a new branch
there needs a test that reaches it. A test written to move the counter rather than to
pin behaviour is worse than the uncovered branch.

<!--
Example of a defeat worth writing down, and of why the second clause matters:

  Seen red two ways. Against a mapping sending a quarter turn to `ROTATION_270`, and
  with the `turnTheDisplay` line removed. 26 tests ran and 1 failed, the same one
  both times. The second rules out the assertion passing for a reason unrelated to
  rotation, which the first does not.

Example of a test that pins behaviour rather than a counter:

  The deepest cutout is taken whatever order the platform lists rectangles in. Every
  fixture here happened to put the deeper one last, so taking the maximum only when
  it arrives last would have passed all of them.
-->

## Evidence

Numbers and claims in this description, and where each came from. A figure read off a
different scope than the one being discussed, or a guard whose failure was never
seen, is the thing this section exists to catch: say which command produced it.

<!--
Example:

  Coverage figures are CI's, which runs the same task on both sides: line 91.7% to
  91.5%, branch 78.4% to 77.3%. An earlier version of this description claimed line
  coverage went up, from a local run that measured the baseline with
  `jacocoTestReport` alone while measuring this branch with the full gate. Roborazzi
  is inert under the former, so the two runs executed different amounts of code and
  the comparison was between two different things.

That is the shape: the number, the command, and where a previous number came from if
you corrected one.
-->

## Review

Anything you are unsure about, or a call a reader might make differently. Naming the
weakest part is worth more than defending all of it.

<!--
Example:

  The overall gate is 90 where the figure is 97.3, which is a wide margin on purpose:
  the uncovered lines need a real window, and a tighter bound would fail the next time
  someone adds one of those rather than the next time coverage slips. Worth
  disagreeing with: 90 could be 95.
-->
