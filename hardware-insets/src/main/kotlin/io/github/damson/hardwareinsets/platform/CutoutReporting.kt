package io.github.damson.hardwareinsets.platform

import android.graphics.Rect
import android.view.View
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.ViewCompat
import io.github.damson.hardwareinsets.domain.CutoutShape

/**
 * The window's [CutoutShape] as Compose state, refreshed on every inset
 * dispatch -- which is what lets the chosen edge follow a camera that rotation
 * or a fold has moved. Compose observes insets but not the `DisplayCutout` they
 * came from.
 *
 * **Never register this on the `ComposeView`.** A View holds exactly one
 * `OnApplyWindowInsetsListener` and `setContent` claims it, silently: the
 * dispatch still arrives, the listener is just no longer yours, and the value
 * stays at its default. Use a sibling.
 *
 * The insets are returned untouched -- consuming them would stop them reaching
 * the composition.
 *
 * **The listener outlives the state this returns, and there is only one slot.**
 * Calling this twice on the same view leaves the first state frozen at whatever
 * it last read, with nothing to say so, because the second call took the slot.
 * The listener is also never removed on its own: it holds this view for as long
 * as the view lives. Call [stopReportingCutoutShape] when the state is no longer
 * observed, and do not call this again on a view that already has it.
 *
 * @return live state, not a reading. It starts at an empty [CutoutShape] and
 *   changes on every later dispatch, so a caller has to observe it rather than
 *   take its value once.
 */
fun View.cutoutShape(): State<CutoutShape> {
    val shape = mutableStateOf(CutoutShape())

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val cutout = insets.displayCutout
        shape.value = CutoutShape(
            bounds = cutout?.boundingRects.orEmpty(),
            topInset = cutout?.safeInsetTop ?: 0,
            bottomInset = cutout?.safeInsetBottom ?: 0,
            leftInset = cutout?.safeInsetLeft ?: 0,
            rightInset = cutout?.safeInsetRight ?: 0,
        )
        insets
    }

    return shape
}

/**
 * Removes whatever [cutoutShape] registered, so the view stops publishing and
 * the listener stops holding it.
 *
 * It clears the view's single listener slot outright rather than only the one
 * [cutoutShape] set, because the platform offers no way to remove a particular
 * listener. Anything else registered on this view goes with it.
 */
fun View.stopReportingCutoutShape() {
    ViewCompat.setOnApplyWindowInsetsListener(this, null)
}
