package io.github.damson.hardwareinsets

import android.os.Build
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * How far the window is allowed to extend into the display cutout.
 *
 * The platform constant this maps to is only honoured from API 28, and
 * [ALWAYS] only exists from API 30, so each case says what it does below that.
 */
enum class CutoutMode {
    /**
     * Into the cutout on every edge, falling back to [SHORT_EDGES] below API 30.
     *
     * The right answer for a surface that fills the window, and the wrong one
     * for most apps: content lands under the camera unless something insets it.
     */
    ALWAYS,

    SHORT_EDGES,
    DEFAULT,
    NEVER,
    ;

    internal fun toLayoutMode(): Int = when (this) {
        ALWAYS ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            else LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        SHORT_EDGES -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        DEFAULT -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        NEVER -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
    }
}

/**
 * Lays the window out behind the system bars and the display cutout, so content
 * covers the hardware rather than stopping short of it.
 *
 * This is layout, not visibility: [hideTheSystemBars] is what takes the bars off
 * screen, and it is a separate decision. Edge-to-edge is not optional -- Android
 * 15 enforces it for anything targeting SDK 35 or later -- so this runs whether
 * or not the bars are showing.
 *
 * @param statusBarStyle the scrim the status bar draws over content, or null to
 *   keep the one `enableEdgeToEdge` picks. Null rather than a restated default,
 *   because that default's scrim colours are private to androidx.
 * @param navigationBarStyle the same, for the navigation bar.
 * @param cutoutMode how far into the cutout the window may go. [CutoutMode.ALWAYS]
 *   suits a full-window drawing surface; most apps want [CutoutMode.SHORT_EDGES].
 *   Ignored below API 28, where the platform has no such attribute.
 */
fun ComponentActivity.drawBehindTheHardware(
    statusBarStyle: SystemBarStyle? = null,
    navigationBarStyle: SystemBarStyle? = null,
    cutoutMode: CutoutMode = CutoutMode.ALWAYS,
) {
    when {
        statusBarStyle != null && navigationBarStyle != null ->
            enableEdgeToEdge(statusBarStyle, navigationBarStyle)
        statusBarStyle != null -> enableEdgeToEdge(statusBarStyle = statusBarStyle)
        navigationBarStyle != null -> enableEdgeToEdge(navigationBarStyle = navigationBarStyle)
        else -> enableEdgeToEdge()
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return

    window.attributes = window.attributes.apply {
        layoutInDisplayCutoutMode = cutoutMode.toLayoutMode()
    }
}

/**
 * Takes system bars off screen and leaves them off.
 *
 * Idempotent, and meant to be called again whenever the window regains focus:
 * returning from another app, or dismissing a dialog, can leave a bar behind.
 *
 * @param types which bars to hide, as a [WindowInsetsCompat.Type] mask. Both by
 *   default, which suits immersive content; pass
 *   `WindowInsetsCompat.Type.navigationBars()` to keep the clock.
 * @param behavior what a swipe from an edge does. The default brings a bar back
 *   for a few seconds and then hides it again, so nothing is unreachable; the
 *   alternative leaves the revealed bar sitting on the content until something
 *   else hides it.
 */
fun ComponentActivity.hideTheSystemBars(
    types: Int = WindowInsetsCompat.Type.systemBars(),
    behavior: Int = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE,
) {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = behavior
        hide(types)
    }
}
