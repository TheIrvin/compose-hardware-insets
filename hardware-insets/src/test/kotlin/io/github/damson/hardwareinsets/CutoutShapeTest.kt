package io.github.damson.hardwareinsets

import android.graphics.Rect
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.DisplayCutoutCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The handle avoids a camera by its shape rather than by its inset, so where the
 * shapes come from is load-bearing. Reading them off `View.rootWindowInsets` --
 * the obvious way -- cannot be tested at all, because Robolectric has no window
 * manager to answer it; taking them off the dispatch is what makes this
 * possible, and it is also the only version that updates when the window does.
 */
@RunWith(RobolectricTestRunner::class)
class CutoutShapeTest {

    private lateinit var host: FrameLayout
    private lateinit var shape: androidx.compose.runtime.State<CutoutShape>

    private val bounds: List<Rect> get() = shape.value.bounds

    @Before
    fun setUp() {
        host = FrameLayout(ApplicationProvider.getApplicationContext())
        shape = host.cutoutShape()
    }

    @Test
    fun `Should start empty Before the window has said anything`() {
        assertThat(shape.value).isEqualTo(CutoutShape())
    }

    @Test
    fun `Should stop publishing Once the listener is removed`() {
        val camera = Rect(485, 0, 595, 142)
        dispatch(cutoutOf(camera))

        host.stopReportingCutoutShape()
        dispatch(cutoutOf(Rect(0, 485, 142, 595)))

        // Frozen at the last thing it saw, not cleared: the state is still a
        // valid reading, it has simply stopped following the window.
        assertThat(bounds).containsExactly(camera)
    }

    @Test
    fun `Should publish the rectangles a camera occupies`() {
        val camera = Rect(485, 0, 595, 142)

        dispatch(cutoutOf(camera))

        assertThat(bounds).containsExactly(camera)
    }

    @Test
    fun `Should replace the rectangles When the window changes`() {
        val portrait = Rect(485, 0, 595, 142)
        val landscape = Rect(0, 485, 142, 595)

        dispatch(cutoutOf(portrait))
        dispatch(cutoutOf(landscape))

        // The whole reason this is a listener and not a one-off read: rotating
        // moves the camera relative to the layout.
        assertThat(bounds).containsExactly(landscape)
    }

    @Test
    fun `Should report nothing When the window has no cutout`() {
        dispatch(cutoutOf(Rect(485, 0, 595, 142)))

        dispatch(WindowInsetsCompat.Builder().build())

        assertThat(shape.value).isEqualTo(CutoutShape())
    }

    @Test
    fun `Should leave the insets for the views below it`() {
        val statusBar = Insets.of(0, 96, 0, 0)
        val child = FrameLayout(host.context)
        var seenByChild: Insets? = null
        host.addView(child)
        ViewCompat.setOnApplyWindowInsetsListener(child) { _, insets ->
            seenByChild = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            insets
        }

        dispatch(
            WindowInsetsCompat.Builder()
                .setInsets(WindowInsetsCompat.Type.statusBars(), statusBar)
                .build()
        )

        // Consuming here would starve the composition, and anything behind
        // it -- of every inset, which is a silent failure rather than a crash.
        assertThat(seenByChild).isEqualTo(statusBar)
    }

    @Test
    fun `Should publish how deep the cutout goes into each horizontal edge`() {
        dispatch(cutoutOf(Rect(485, 0, 595, 142)))

        // The depth, not the shape: it is what says whether an edge has hardware
        // in it at all, and it needs no window size to be read against, so it can
        // be read during a composition -- when the window's own is still zero.
        assertThat(shape.value.topInset).isEqualTo(142)
        assertThat(shape.value.bottomInset).isZero()
    }

    @Test
    fun `Should tell a cutout in the bottom edge from one in the top`() {
        val chin = Rect(485, 2258, 595, 2400)

        dispatch(
            WindowInsetsCompat.Builder()
                .setDisplayCutout(
                    DisplayCutoutCompat(
                        Insets.of(0, 0, 0, 142),
                        null,
                        null,
                        null,
                        chin,
                        Insets.NONE,
                    )
                )
                .build()
        )

        assertThat(shape.value.topInset).isZero()
        assertThat(shape.value.bottomInset).isEqualTo(142)
    }

    private fun dispatch(insets: WindowInsetsCompat) =
        ViewCompat.dispatchApplyWindowInsets(host, insets)

    /**
     * The safe insets have to be non-empty: the platform drops a cutout whose
     * safe insets are empty, and the rectangles go with it.
     */
    private fun cutoutOf(rect: Rect) = WindowInsetsCompat.Builder()
        .setDisplayCutout(
            DisplayCutoutCompat(
                Insets.of(0, rect.bottom, 0, 0),
                null,
                rect,
                null,
                null,
                Insets.NONE,
            )
        )
        .build()
}
