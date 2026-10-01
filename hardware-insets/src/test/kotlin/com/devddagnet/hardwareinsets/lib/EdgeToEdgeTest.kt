package com.devddagnet.hardwareinsets.lib

import android.graphics.Color.TRANSPARENT
import android.os.Build
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.core.view.WindowInsetsCompat.Type.navigationBars
import androidx.core.view.WindowInsetsCompat.Type.statusBars
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.devddagnet.hardwareinsets.lib.domain.CutoutMode
import com.devddagnet.hardwareinsets.lib.platform.drawBehindTheHardware
import com.devddagnet.hardwareinsets.lib.platform.hideTheSystemBars

/**
 * Two decisions that arrive together and are not the same one.
 *
 * The window is laid out behind the hardware, which is what lets content cover
 * the cutout, and is not optional from SDK 35. The bars are then hidden, which
 * is a separate choice belonging to the app rather than to the window.
 *
 * They run against a bare `ComponentActivity`, which is all either function
 * asks of a host. Being extension functions rather than base-class behaviour is
 * what makes that possible: nothing has to be inherited to be tested.
 */
@RunWith(RobolectricTestRunner::class)
class EdgeToEdgeTest {

    @Test
    fun `Should extend the window into the cutout on every edge`() {
        val activity = launchActivity()

        activity.drawBehindTheHardware()

        // ALWAYS rather than SHORT_EDGES: SHORT_EDGES stops extending into the
        // cutout once the cutout is on a long edge, which letterboxes a
        // full-window surface in landscape on exactly the devices that have one.
        assertThat(activity.window.attributes.layoutInDisplayCutoutMode)
            .isEqualTo(LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS)
    }

    @Test
    fun `Should stop at a short edge When that is the mode asked for`() {
        val activity = launchActivity()

        activity.drawBehindTheHardware(cutoutMode = CutoutMode.SHORT_EDGES)

        assertThat(activity.window.attributes.layoutInDisplayCutoutMode)
            .isEqualTo(LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    }

    @Test
    fun `Should keep the window out of the cutout When the mode says never`() {
        val activity = launchActivity()

        activity.drawBehindTheHardware(cutoutMode = CutoutMode.NEVER)

        assertThat(activity.window.attributes.layoutInDisplayCutoutMode)
            .isEqualTo(LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER)
    }

    @Test
    fun `Should hide only the bar it was handed`() {
        val activity = launchActivity()

        activity.hideTheSystemBars(types = navigationBars())

        // The status bar is what an app keeps when it wants the clock, so the
        // mask has to be honoured rather than widened back to both.
        assertThat(activity.requestedVisibleTypes() and navigationBars()).isZero()
        assertThat(activity.requestedVisibleTypes() and statusBars()).isEqualTo(statusBars())
    }

    @Test
    fun `Should hand both bar styles on When both were given`() {
        val activity = launchActivity()

        activity.drawBehindTheHardware(
            statusBarStyle = SystemBarStyle.light(TRANSPARENT, TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT),
        )

        // Deliberately opposite, because the branch that takes both could pass
        // one and drop the other and still satisfy a test that asked for the
        // same style twice.
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        assertThat(controller.isAppearanceLightStatusBars).isTrue()
        assertThat(controller.isAppearanceLightNavigationBars).isFalse()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `Should fall back to a short edge Below the API that has every edge`() {
        val activity = launchActivity()

        activity.drawBehindTheHardware(cutoutMode = CutoutMode.ALWAYS)

        // ALWAYS arrived in API 30. Asking for it below that has to land on the
        // most either version offers rather than on the platform's default,
        // which would stop the window short of the cutout entirely.
        assertThat(activity.window.attributes.layoutInDisplayCutoutMode)
            .isEqualTo(LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O_MR1])
    fun `Should not reach the cutout attribute at all Below the API that has it`() {
        val activity = launchActivity()

        // The field itself arrived in API 28, so touching it below that is a
        // link error rather than a wrong value. The early return is the only
        // thing between a caller and a crash on an API 27 device.
        assertThatCode { activity.drawBehindTheHardware() }.doesNotThrowAnyException()
    }

    @Test
    fun `Should take both system bars off screen`() {
        val activity = launchActivity()

        // Read first, so the assertion below is known to be looking at a field
        // that can hold the other answer. Without this the test would pass just
        // as happily against a mask that is always zero.
        assertThat(activity.requestedVisibleTypes() and SYSTEM_BARS).isEqualTo(SYSTEM_BARS)

        activity.hideTheSystemBars()

        assertThat(activity.requestedVisibleTypes() and SYSTEM_BARS).isZero()
    }

    @Test
    fun `Should bring a bar back only for as long as a swipe asks for it`() {
        val activity = launchActivity()

        activity.hideTheSystemBars()

        // The alternative leaves a revealed bar on screen until something else
        // hides it, which over full-window content means a bar sitting on it.
        val controller = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        assertThat(controller.systemBarsBehavior).isEqualTo(BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    }

    @Test
    fun `Should hide the bars again When asked a second time`() {
        val activity = launchActivity()

        activity.hideTheSystemBars()
        activity.hideTheSystemBars()

        // Called on every focus gain, because returning from another app or
        // dismissing a dialog can leave a bar behind.
        assertThat(activity.requestedVisibleTypes() and SYSTEM_BARS).isZero()
    }

    /**
     * Robolectric does not round-trip a `hide()` back to `rootWindowInsets` --
     * the window manager that would answer is not running -- so the only place
     * the request lands is the controller's own requested-visibility mask. That
     * is not public API, and it is reached by reflection here because the
     * alternative is a test that cannot see the one thing the function does. If
     * a later SDK moves it, this fails loudly rather than passing on nothing.
     */
    private fun ComponentActivity.requestedVisibleTypes(): Int {
        val controller = requireNotNull(window.insetsController)
        return controller.javaClass.getMethod("getRequestedVisibleTypes").invoke(controller) as Int
    }

    private companion object {
        // The compat spelling, not `android.view.WindowInsets.Type`, which
        // arrived in API 30: a companion that cannot initialise below that
        // fails every test configured for an older SDK before it runs, and the
        // error names the field rather than the reason.
        val SYSTEM_BARS = statusBars() or navigationBars()
    }

    // ComponentActivity, not an AppCompat one: everything here is declared on
    // ComponentActivity, and an AppCompat host would need a theme this library
    // does not ship and should not require of a consumer.
    private fun launchActivity(): ComponentActivity =
        Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
}
