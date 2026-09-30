package io.github.damson.hardwareinsets

import android.view.Surface
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import io.github.damson.hardwareinsets.domain.ScreenRotation
import io.github.damson.hardwareinsets.platform.screenRotation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

/**
 * The mapping off `Surface.ROTATION_*`, against the real constants.
 *
 * Everything above [screenRotation] takes a [ScreenRotation], so a wrong
 * mapping here is invisible: a side-anchored control lands on a different edge
 * and nothing throws. Asserting against a copy of the constants would prove
 * only that the copy matches itself.
 */
@RunWith(RobolectricTestRunner::class)
class ScreenRotationTest {

    @Test
    fun `Should name every rotation the platform reports`() {
        val expected = mapOf(
            Surface.ROTATION_0 to ScreenRotation.NONE,
            Surface.ROTATION_90 to ScreenRotation.QUARTER,
            Surface.ROTATION_180 to ScreenRotation.HALF,
            Surface.ROTATION_270 to ScreenRotation.THREE_QUARTERS,
        )
        assertThat(expected.keys).hasSameSizeAs(ScreenRotation.entries)

        expected.forEach { (constant, named) ->
            assertThat(rotationWhenDisplayReports(constant))
                .describedAs("Surface rotation %s", constant)
                .isEqualTo(named)
        }
    }

    @Test
    fun `Should read a rotation it does not recognise as upright`() {
        // A value outside the four is a device or an emulator answering
        // something undocumented. Upright is the answer that leaves a viewer
        // usable; throwing here would crash on a reading nobody asked for.
        assertThat(rotationWhenDisplayReports(42)).isEqualTo(ScreenRotation.NONE)
    }

    private fun rotationWhenDisplayReports(rotation: Int): ScreenRotation {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val display = ContextCompat.getDisplayOrDefault(activity)
        shadowOf(display).setRotation(rotation)
        return display.screenRotation
    }
}
