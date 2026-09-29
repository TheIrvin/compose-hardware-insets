package io.github.damson.hardwareinsets

import android.graphics.Rect
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import io.github.damson.hardwareinsets.domain.ScreenEdge
import io.github.damson.hardwareinsets.domain.HardwarePolicy

/**
 * The contents' half of edge-to-edge. A surface covers the hardware and so
 * does a panel's own background; it is what sits inside the background that
 * backs off, and by default only for hardware rather than for the bars.
 *
 * Both insets are driven from a built [WindowInsetsCompat] rather than a device
 * qualifier, because there is no resource qualifier for a cutout and none at all
 * for a curved edge -- a test that waited for a device with the hardware would
 * only ever run on someone's desk.
 */
@RunWith(RobolectricTestRunner::class)
class HardwareInsetsTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `Should hold the panel below the cutout`() {
        showPanel()

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), topInset())
                .build()
        )

        assertThat(panelBounds().top).isEqualTo(INSET_PX)
    }

    @Test
    fun `Should sit flush against the edge When there is no hardware in the way`() {
        showPanel()

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.statusBars(), topInset())
                .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, INSET_PX))
                .build()
        )

        // The default policy leaves the bars out, so they owe the panel nothing. Insetting by a
        // bar that is not on screen would push the handle off the edge it is
        // meant to sit on -- and on the great majority of phones that is the
        // only inset there would be.
        assertThat(panelBounds().top).isZero()
        assertThat(panelBounds().left).isZero()
    }

    @Test
    fun `Should hold the panel inside a waterfall edge`() {
        showPanel()

        // safeDrawing leaves the curve out -- it is drawable. It is not reliably
        // touchable, which is what a control on that edge cares about, so waterfall is
        // applied on top of it.
        applyInsets(
            WindowInsetsCompat.Builder()
                .setDisplayCutout(waterfallOf(Insets.of(INSET_PX, 0, INSET_PX, 0)))
                .build()
        )

        assertThat(panelBounds().left).isEqualTo(INSET_PX)
    }

    @Test
    fun `Should leave the background at the full width of the screen`() {
        showPanel()

        applyInsets(
            WindowInsetsCompat.Builder()
                .setDisplayCutout(waterfallOf(Insets.of(INSET_PX, 0, INSET_PX, 0)))
                .build()
        )

        // The point of the split: a panel inset off the curve would leave a strip
        // of bare surface down each side of itself. The background stays whole and
        // its contents move.
        assertThat(backgroundBounds().left).isZero()
        assertThat(backgroundBounds().right).isEqualTo(rootBounds().right)
    }

    @Test
    fun `Should ignore a cutout at the bottom`() {
        showPanel()
        val unpadded = panelBounds().top

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), Insets.of(0, 0, 0, INSET_PX))
                .build()
        )

        // A bottom inset would grow the ComposeView hosting the panel, and a
        // ComposeView swallows every touch inside its bounds -- so that growth
        // is surface nobody can reach. The panel is anchored to the top and
        // has nothing down there to avoid.
        assertThat(panelBounds().top).isEqualTo(unpadded)
    }

    @Test
    fun `Should report every edge When the far one is not ignored`() {
        lateinit var density: Density
        lateinit var insets: WindowInsets
        compose.setContent {
            density = LocalDensity.current
            insets = hardwareInsets(ScreenEdge.TOP)
        }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(
                    WindowInsetsCompat.Type.displayCutout(),
                    Insets.of(0, INSET_PX, 0, CHIN_PX),
                )
                .build()
        )

        // The default every caller gets. Zeroing the far edge is a workaround
        // for hosting a panel in a wrap_content ComposeView, and it has to be
        // asked for rather than assumed.
        assertThat(insets.getTop(density)).isEqualTo(INSET_PX)
        assertThat(insets.getBottom(density)).isEqualTo(CHIN_PX)
    }

    @Test
    fun `Should leave the system bars out Unless the policy asks for them`() {
        lateinit var density: Density
        lateinit var byDefault: WindowInsets
        lateinit var withBars: WindowInsets
        compose.setContent {
            density = LocalDensity.current
            byDefault = hardwareInsets()
            withBars = hardwareInsets(policy = HardwarePolicy(areSystemBarsIncluded = true))
        }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(0, CHIN_PX, 0, 0))
                .build()
        )

        // A hidden bar still reports an inset. Padding by it moves content off
        // an edge nothing occupies, which is why the default leaves them out.
        assertThat(byDefault.getTop(density)).isZero()
        assertThat(withBars.getTop(density)).isEqualTo(CHIN_PX)
    }

    @Test
    fun `Should drop the cutout term When the policy excludes it`() {
        lateinit var density: Density
        lateinit var insets: WindowInsets
        compose.setContent {
            density = LocalDensity.current
            insets = hardwareInsets(policy = HardwarePolicy(isCutoutIncluded = false))
        }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.displayCutout(), topInset())
                .build()
        )

        assertThat(insets.getTop(density)).isZero()
    }

    @Test
    fun `Should take a bottom-anchored panel off the bottom edge and not the top`() {
        lateinit var density: Density
        lateinit var atTop: WindowInsets
        lateinit var atBottom: WindowInsets
        compose.setContent {
            density = LocalDensity.current
            atTop = hardwareInsets(ScreenEdge.TOP, isFarEdgeIgnored = true)
            atBottom = hardwareInsets(ScreenEdge.BOTTOM, isFarEdgeIgnored = true)
        }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setInsets(
                    WindowInsetsCompat.Type.displayCutout(),
                    Insets.of(0, INSET_PX, 0, CHIN_PX),
                )
                .build()
        )

        // Each anchoring takes its own edge and zeroes the other. The far side
        // would grow the ComposeView hosting the panel, and a ComposeView
        // swallows every touch inside its bounds, so that growth is surface
        // nobody can draw on.
        assertThat(atTop.getTop(density)).isEqualTo(INSET_PX)
        assertThat(atTop.getBottom(density)).isZero()
        assertThat(atBottom.getBottom(density)).isEqualTo(CHIN_PX)
        assertThat(atBottom.getTop(density)).isZero()
    }

    @Test
    fun `Should leave a corner control in the corner When the camera is elsewhere`() {
        val camera = Rect(485, 0, 595, 142)

        val clearance = clearanceFor(listOf(camera))

        // The inset would say 142 across the whole width. The handle is not
        // under the camera and should not move for it.
        assertThat(clearance(HANDLE_WIDTH_PX)).isEqualTo(IntOffset.Zero)
    }

    @Test
    fun `Should drop a corner control below a camera it does share the corner with`() {
        val notch = Rect(0, 0, HANDLE_WIDTH_PX + 10, 142)

        val clearance = clearanceFor(listOf(notch))

        assertThat(clearance(HANDLE_WIDTH_PX)).isEqualTo(IntOffset(0, 142))
    }

    @Test
    fun `Should take a corner control's side clearance from the curve`() {
        val clearance = clearanceFor(
            cutoutBounds = emptyList(),
            insets = WindowInsetsCompat.Builder()
                .setDisplayCutout(waterfallOf(Insets.of(INSET_PX, 0, INSET_PX, 0)))
                .build(),
        )

        assertThat(clearance(HANDLE_WIDTH_PX)).isEqualTo(IntOffset(INSET_PX, 0))
    }

    @Test
    fun `Should measure a corner control from the end edge When the layout is right to left`() {
        // Mirrored: this rect is in the top-right corner, which is where the
        // handle sits under RTL and is nowhere near it under LTR. Both
        // directions are composed at once because the rule allows one
        // setContent per test.
        val rootWidth = compose.activity.window.decorView.rootView.width
        val rightCorner = Rect(rootWidth - HANDLE_WIDTH_PX, 0, rootWidth, 142)
        lateinit var rtl: (Int) -> IntOffset
        lateinit var ltr: (Int) -> IntOffset

        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                rtl = cornerClearance(listOf(rightCorner))
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ltr = cornerClearance(listOf(rightCorner))
            }
        }

        assertThat(rtl(HANDLE_WIDTH_PX).y).isEqualTo(142)
        assertThat(ltr(HANDLE_WIDTH_PX).y).isZero()
    }

    @Test
    fun `Should leave a rail's handle alone Where a corner one would move`() {
        lateinit var onARail: (Int) -> IntOffset
        lateinit var inACorner: (Int) -> IntOffset
        compose.setContent {
            onARail = cornerClearance(emptyList(), ScreenEdge.LEFT)
            inACorner = cornerClearance(emptyList(), ScreenEdge.TOP)
        }

        applyInsets(
            WindowInsetsCompat.Builder()
                .setDisplayCutout(waterfallOf(Insets.of(INSET_PX, 0, INSET_PX, 0)))
                .build()
        )

        // Same curve, and only the corner control owes anything to it. A rail's
        // control sits inboard of a panel hardwareInsets already moved.
        assertThat(inACorner(HANDLE_WIDTH_PX)).isEqualTo(IntOffset(INSET_PX, 0))
        assertThat(onARail(HANDLE_WIDTH_PX)).isEqualTo(IntOffset.Zero)
    }

    private fun clearanceFor(
        cutoutBounds: List<Rect>,
        insets: WindowInsetsCompat? = null,
    ): (Int) -> IntOffset {
        lateinit var clearance: (Int) -> IntOffset
        compose.setContent { clearance = cornerClearance(cutoutBounds) }
        insets?.let(::applyInsets)
        return clearance
    }

    /** A background, then the inset, then the content, which is the order that matters. */
    private fun showPanel() {
        compose.setContent {
            Box(
                Modifier
                    .testTag(BACKGROUND)
                    .fillMaxWidth()
                    .background(Color.Red)
                    .clearOfTheHardware(isFarEdgeIgnored = true),
            ) {
                Box(Modifier.size(PANEL_SIZE).testTag(PANEL))
            }
        }
    }

    private fun applyInsets(insets: WindowInsetsCompat) {
        val content = compose.activity.findViewById<View>(android.R.id.content)
        compose.activity.runOnUiThread { ViewCompat.dispatchApplyWindowInsets(content, insets) }
        compose.waitForIdle()
    }

    private fun panelBounds() = boundsOf(PANEL)

    private fun backgroundBounds() = boundsOf(BACKGROUND)

    private fun rootBounds() = with(compose.density) {
        compose.onRoot().getBoundsInRoot().let { Bounds(it.left.roundToPx(), it.top.roundToPx(), it.right.roundToPx()) }
    }

    private fun boundsOf(tag: String) = with(compose.density) {
        compose.onNodeWithTag(tag).getBoundsInRoot()
            .let { Bounds(it.left.roundToPx(), it.top.roundToPx(), it.right.roundToPx()) }
    }

    private data class Bounds(val left: Int, val top: Int, val right: Int)

    private fun topInset() = Insets.of(0, INSET_PX, 0, 0)

    /**
     * The safe insets have to carry the same depth: the platform drops a cutout
     * whose safe insets are empty, so a waterfall-only one never reaches the
     * view. They are not set as `Type.displayCutout()` insets, which is what
     * `safeDrawing` reads -- leaving this test measuring the waterfall alone.
     */
    private fun waterfallOf(insets: Insets) = DisplayCutoutCompat(
        insets,
        null,
        null,
        null,
        null,
        insets,
    )

    private companion object {
        const val PANEL = "panel"
        const val BACKGROUND = "background"
        val PANEL_SIZE = 10.dp

        /** Deeper than any real bar, so a partly-applied inset cannot pass. */
        const val INSET_PX = 96

        /** Different from [INSET_PX], so neither edge can pass on the other's value. */
        const val CHIN_PX = 64

        /** About what a corner control measures on a phone-density screen. */
        const val HANDLE_WIDTH_PX = 160
    }
}
