package com.devddagnet.hardwareinsets.sample.app

import android.os.Bundle
import com.devddagnet.hardwareinsets.sample.model.ViewerOptions
import com.devddagnet.hardwareinsets.lib.domain.CutoutMode
import com.devddagnet.hardwareinsets.lib.domain.HardwarePolicy
import com.devddagnet.hardwareinsets.lib.domain.ScreenEdge

/**
 * Writes the options into a `Bundle`, for the activity to carry across its own
 * recreation.
 *
 * Every field goes in by hand rather than through a parcelable, because the two
 * enums are the library's and the sample does not own their serialised form.
 * Enum names are written rather than ordinals, so reordering either enum in the
 * library cannot silently turn a saved TOP into a BOTTOM.
 */
fun ViewerOptions.saveInto(out: Bundle): Bundle = out.apply {
    putString(ANCHOR, anchor.name)
    putBoolean(CORNER_AT_THE_END, isCornerAtTheEnd)
    putBoolean(CUTOUT_INCLUDED, policy.isCutoutIncluded)
    putBoolean(WATERFALL_INCLUDED, policy.isWaterfallIncluded)
    putBoolean(BARS_INCLUDED, policy.areSystemBarsIncluded)
    putBoolean(FAR_EDGE_IGNORED, isFarEdgeIgnored)
    putString(CUTOUT_MODE, cutoutMode.name)
    putBoolean(BARS_HIDDEN, areBarsHidden)
    putBoolean(MARKERS_SHOWN, areMarkersShown)
}

/**
 * Reads back what [saveInto] wrote, falling back to the defaults for anything
 * absent, so a first launch and a restore go down the same path.
 */
fun viewerOptionsFrom(saved: Bundle?): ViewerOptions {
    val defaults = ViewerOptions()
    if (saved == null) return defaults
    return ViewerOptions(
        anchor = enumOrNull<ScreenEdge>(saved.getString(ANCHOR)) ?: defaults.anchor,
        isCornerAtTheEnd = saved.getBoolean(CORNER_AT_THE_END, defaults.isCornerAtTheEnd),
        policy = HardwarePolicy(
            isCutoutIncluded = saved.getBoolean(CUTOUT_INCLUDED, defaults.policy.isCutoutIncluded),
            isWaterfallIncluded =
                saved.getBoolean(WATERFALL_INCLUDED, defaults.policy.isWaterfallIncluded),
            areSystemBarsIncluded =
                saved.getBoolean(BARS_INCLUDED, defaults.policy.areSystemBarsIncluded),
        ),
        isFarEdgeIgnored = saved.getBoolean(FAR_EDGE_IGNORED, defaults.isFarEdgeIgnored),
        cutoutMode = enumOrNull<CutoutMode>(saved.getString(CUTOUT_MODE)) ?: defaults.cutoutMode,
        areBarsHidden = saved.getBoolean(BARS_HIDDEN, defaults.areBarsHidden),
        areMarkersShown = saved.getBoolean(MARKERS_SHOWN, defaults.areMarkersShown),
    )
}

// A name written by an older build of the app, or by a newer one, is a name
// this build may not have. valueOf throws on it; the caller wants the default.
private inline fun <reified T : Enum<T>> enumOrNull(name: String?): T? =
    enumValues<T>().firstOrNull { it.name == name }

private const val ANCHOR = "anchor"
private const val CORNER_AT_THE_END = "isCornerAtTheEnd"
private const val CUTOUT_INCLUDED = "isCutoutIncluded"
private const val WATERFALL_INCLUDED = "isWaterfallIncluded"
private const val BARS_INCLUDED = "areSystemBarsIncluded"
private const val FAR_EDGE_IGNORED = "isFarEdgeIgnored"
private const val CUTOUT_MODE = "cutoutMode"
private const val BARS_HIDDEN = "areBarsHidden"
private const val MARKERS_SHOWN = "areMarkersShown"
