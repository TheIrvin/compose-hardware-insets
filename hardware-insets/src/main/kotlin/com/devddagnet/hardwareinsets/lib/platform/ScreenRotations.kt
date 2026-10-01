package com.devddagnet.hardwareinsets.lib.platform

import android.view.Display
import android.view.Surface
import com.devddagnet.hardwareinsets.lib.domain.ScreenRotation

/**
 * The display's rotation, named.
 *
 * The only place in this library that reads `Surface.ROTATION_*`. Everything
 * above it takes a [ScreenRotation], so no caller is holding an `Int` that
 * three unrelated constants would also satisfy.
 *
 * Anything outside the four documented values is read as [ScreenRotation.NONE]
 * rather than throwing: this is a value the platform handed you, and a viewer
 * that crashes on an unexpected one is worse than a viewer that treats it as
 * upright.
 */
val Display.screenRotation: ScreenRotation
    get() = when (rotation) {
        Surface.ROTATION_90 -> ScreenRotation.QUARTER
        Surface.ROTATION_180 -> ScreenRotation.HALF
        Surface.ROTATION_270 -> ScreenRotation.THREE_QUARTERS
        else -> ScreenRotation.NONE
    }
