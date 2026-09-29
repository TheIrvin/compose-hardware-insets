package io.github.damson.hardwareinsets.platform

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
import io.github.damson.hardwareinsets.domain.CutoutMode

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

/**
 * What the framework calls each [CutoutMode].
 *
 * Here rather than on the enum: the mode is a choice a caller makes, and the
 * constant it becomes is this layer's business. [CutoutMode.ALWAYS] only exists
 * from API 30, so below that it falls back to the next-widest thing.
 */
internal fun CutoutMode.toLayoutMode(): Int = when (this) {
    CutoutMode.ALWAYS ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        else LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    CutoutMode.SHORT_EDGES -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    CutoutMode.DEFAULT -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
    CutoutMode.NEVER -> LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
}
