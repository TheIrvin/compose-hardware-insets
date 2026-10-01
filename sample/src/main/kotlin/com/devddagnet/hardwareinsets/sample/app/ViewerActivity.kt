package com.devddagnet.hardwareinsets.sample.app

import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.devddagnet.hardwareinsets.lib.domain.CutoutShape
import com.devddagnet.hardwareinsets.lib.domain.ScreenRotation
import com.devddagnet.hardwareinsets.lib.platform.cutoutShape
import com.devddagnet.hardwareinsets.lib.platform.screenRotation
import com.devddagnet.hardwareinsets.lib.platform.drawBehindTheHardware
import com.devddagnet.hardwareinsets.lib.platform.hideTheSystemBars
import com.devddagnet.hardwareinsets.lib.platform.stopReportingCutoutShape
import com.devddagnet.hardwareinsets.sample.R
import com.devddagnet.hardwareinsets.sample.model.ViewerOptions
import com.devddagnet.hardwareinsets.sample.ui.SampleTheme
import com.devddagnet.hardwareinsets.sample.ui.ViewerScreen

/**
 * A full-bleed gallery, which wants the opposite of what a form wants: the
 * plate under the camera, and the wall label kept off it.
 *
 * It closes on the system Back gesture and offers no button for it. The corner
 * is spent on favouriting and sharing, which is what a viewer actually needs
 * there, and a screen whose only corner control closed it would not be
 * demonstrating a corner worth defending.
 *
 * The window-level options live here because they are window-level: the cutout
 * mode is an attribute, hiding a bar is a controller call, and so is deciding
 * whether the bar's icons are dark. None of the three is a composable's.
 */
class ViewerActivity : ComponentActivity() {

    private lateinit var insetHost: View
    private lateinit var cutout: State<CutoutShape>
    private var options by mutableStateOf(ViewerOptions())
    // This activity declares the orientation config changes itself, so it is
    // not recreated when the device turns and nothing re-reads the rotation on
    // its own. Held as state and refreshed where the change arrives.
    private var rotation by mutableStateOf(ScreenRotation.NONE)
    private var isPlatePaleAtTheTop = true
    private var isPlatePaleAtTheBottom = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A dark mode, locale, font scale or rotation change recreates this
        // activity, and these are the things the visitor came to change. Read
        // before the window is asked for anything, since two of them are window
        // attributes and the restored values are what the window should open on.
        options = viewerOptionsFrom(savedInstanceState)
        rotation = readRotation()
        applyWindow(options)
        setContentView(buildContentView())
        cutout = insetHost.cutoutShape()
    }

    // A configuration change is not enough on its own. Turning the device from
    // one landscape to the other keeps the orientation, the size and the
    // layout, so the Configuration can be identical and no callback arrives,
    // while the rotation has gone from a quarter turn to three quarters and a
    // side-anchored control belongs on the opposite edge. The display says so
    // even when the configuration does not.
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit

        override fun onDisplayChanged(displayId: Int) {
            rotation = readRotation()
        }
    }

    override fun onStart() {
        super.onStart()
        rotation = readRotation()
        getSystemService(DisplayManager::class.java)
            ?.registerDisplayListener(displayListener, null)
    }

    override fun onStop() {
        getSystemService(DisplayManager::class.java)?.unregisterDisplayListener(displayListener)
        super.onStop()
    }

    private fun readRotation(): ScreenRotation =
        ContextCompat.getDisplayOrDefault(this).screenRotation

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Returning from another app or dismissing a dialog can leave a bar
        // behind, so this is asked for again rather than once.
        if (hasFocus) applyWindow(options)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        options.saveInto(outState)
    }

    override fun onDestroy() {
        insetHost.stopReportingCutoutShape()
        super.onDestroy()
    }

    /**
     * Sending a plate on is an `Intent`, so it lives here rather than in the
     * composable that asked for it. Text, because what is on screen is painted
     * at the window's size and there is no file to attach.
     */
    private fun onShare(line: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, line)
        }
        startActivity(Intent.createChooser(send, getString(R.string.share)))
    }

    private fun onOptions(next: ViewerOptions) {
        applyWindow(next)
        options = next
    }

    /**
     * A transparent bar has the plate behind it, and the platform cannot see
     * what colour that is: with no theme attribute and no listener for it, the
     * icons stay whatever they were until something says otherwise.
     */
    private fun onBarsOver(isPaleAtTheTop: Boolean, isPaleAtTheBottom: Boolean) {
        isPlatePaleAtTheTop = isPaleAtTheTop
        isPlatePaleAtTheBottom = isPaleAtTheBottom
        applyBarIcons()
    }

    /**
     * Applied after every window call and not only when the plate changes.
     *
     * `drawBehindTheHardware` goes through `enableEdgeToEdge`, which sets the
     * icon appearance itself from the night resources, so it undoes this every
     * time it runs. In a dark theme that means light icons over a pale plate,
     * and the only symptom is a clock that has disappeared.
     */
    private fun applyBarIcons() {
        with(WindowCompat.getInsetsController(window, window.decorView)) {
            isAppearanceLightStatusBars = isPlatePaleAtTheTop
            isAppearanceLightNavigationBars = isPlatePaleAtTheBottom
        }
    }

    /**
     * The library hides bars but has no way to show them again, so the sample
     * reaches past it for that half. Worth knowing before you build an app that
     * toggles them.
     */
    private fun applyWindow(options: ViewerOptions) {
        drawBehindTheHardware(cutoutMode = options.cutoutMode)
        val bars = WindowInsetsCompat.Type.systemBars()
        if (options.areBarsHidden) {
            hideTheSystemBars(types = bars)
        } else {
            WindowCompat.getInsetsController(window, window.decorView).show(bars)
        }
        applyBarIcons()
    }

    /**
     * A sibling for the listener, because a `View` holds exactly one
     * `OnApplyWindowInsetsListener` and `ComposeView.setContent` claims it. On
     * the `ComposeView` the dispatch would still arrive, the listener would just
     * no longer be ours, and the cutout would read empty forever with nothing
     * saying so.
     */
    private fun buildContentView(): FrameLayout = FrameLayout(this).apply {
        insetHost = View(context).also { addView(it, 1, 1) }
        addView(
            ComposeView(context).apply {
                setContent {
                    SampleTheme {
                        ViewerScreen(
                            cutout = cutout.value,
                            options = options,
                            // Qualified: this sits inside ComposeView.apply,
                            // where a bare rotation is the View's own Float.
                            rotation = this@ViewerActivity.rotation,
                            onOptions = ::onOptions,
                            onBarsOver = ::onBarsOver,
                            onShare = ::onShare,
                        )
                    }
                }
            }
        )
    }
}
