<!--
A fix that cannot wait for the next release, cut from `main` and going back to it.

Open it with ?template=hotfix.md on the compare URL. If the fix can wait, it is
ordinary work into `develop` and uses the default template.
-->

## Summary

What is broken for someone using a released version, and what is true after this.
Say which released version is affected.

## Why this is not ordinary work into `develop`

A hotfix skips `develop`, so it needs a reason: what a consumer cannot do until this
ships, and why waiting for the next release is not an answer.

<!--
The shape, using the one real near-miss this repository has had:

  Anyone following the README cannot resolve the dependency at all: the install line
  names a coordinate that returns 404, so the first thing a visitor copies fails.
  Waiting means every visitor between now and the next release hits it.

A weak version of the same thing, which is ordinary work rather than a hotfix:

  The install line is formatted inconsistently with the rest of the README.
-->

## What changed

Keep it to the fix. A hotfix is the worst branch to carry a tidy-up on, because it is
the one reviewed in a hurry and released without soaking.

## Test plan

- [ ] `./gradlew check apiCheck koverVerify :hardware-insets:koverVerify` green, on JDK 21
- [ ] A test that fails without the fix, seen failing
- [ ] The released version reproduced the fault first, so this is known to fix the
      thing that was reported rather than something adjacent

## It has to reach `develop` too

`main` is not merged back automatically, and a fix that lands only on `main` is
reverted by the next promotion without anything failing.

<!--
Example:

  Follow-up #41 carries this onto `develop`. Without it the next promotion of
  `develop` to `main` reverts the fix, and nothing fails: the tree simply goes back to
  what `develop` has, which is the broken version.
-->

- [ ] A follow-up is open, or named here, that gets this onto `develop`
- [ ] `CHANGELOG.md` records it under the patch version being cut

## Review

Say what was not done because this was urgent, so the follow-up is explicit rather
than remembered.
