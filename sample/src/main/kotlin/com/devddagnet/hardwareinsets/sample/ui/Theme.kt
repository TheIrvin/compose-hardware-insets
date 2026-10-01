package com.devddagnet.hardwareinsets.sample.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Paper, mist, sea and ink: the four the sample was given.
 *
 * [Sea] is the only one that can be a fill behind light text or a text colour on
 * a light fill, and it cannot be both at once, so it is the accent and never
 * body copy. [Mist] is a surface, not an outline: at 1.2:1 against [Paper] it
 * disappears as a hairline.
 */
private val Paper = Color(0xFFF9F7F7)
private val Mist = Color(0xFFDBE2EF)
private val Sea = Color(0xFF3F72AF)
private val Ink = Color(0xFF112D4E)

private val LightScheme = lightColorScheme(
    primary = Sea,
    onPrimary = Paper,
    primaryContainer = Mist,
    onPrimaryContainer = Ink,
    secondary = Ink,
    onSecondary = Paper,
    secondaryContainer = Mist,
    onSecondaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Mist,
    onSurfaceVariant = Color(0xFF33527D),
    // The container family, set explicitly because Material derives it from its
    // own defaults otherwise: a sheet or a menu then arrives in a grey that is
    // not in the palette, and nothing in the code says where it came from.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Paper,
    surfaceContainer = Color(0xFFF1F3F8),
    surfaceContainerHigh = Color(0xFFE7ECF5),
    surfaceContainerHighest = Mist,
    outline = Sea,
    outlineVariant = Color(0xFFA9BEDC),
)

private val DarkScheme = darkColorScheme(
    primary = Mist,
    onPrimary = Ink,
    primaryContainer = Sea,
    onPrimaryContainer = Paper,
    secondary = Sea,
    onSecondary = Paper,
    secondaryContainer = Color(0xFF1C3F68),
    onSecondaryContainer = Mist,
    background = Ink,
    onBackground = Paper,
    surface = Ink,
    onSurface = Paper,
    surfaceVariant = Color(0xFF1C3F68),
    onSurfaceVariant = Color(0xFFC3D2E8),
    surfaceContainerLowest = Color(0xFF0C1F36),
    surfaceContainerLow = Color(0xFF142F4F),
    surfaceContainer = Color(0xFF173A5C),
    surfaceContainerHigh = Color(0xFF1C3F68),
    surfaceContainerHighest = Color(0xFF234A76),
    outline = Sea,
    outlineVariant = Color(0xFF2B5687),
)

/**
 * The ground a wall label sits on.
 *
 * Translucent rather than solid, so the markers underneath stay readable
 * through it. That is the one thing this screen exists to show.
 */
val MaterialTheme.plaque: Color get() = Ink.copy(alpha = 0.72f)

val MaterialTheme.onPlaque: Color get() = Paper

val MaterialTheme.onPlaqueVariant: Color get() = Mist

/** The interface's own colour: what marks a control as a control. */
val MaterialTheme.accent: Color get() = Color(0xFFFFD400)

val MaterialTheme.onAccent: Color get() = Ink

val MaterialTheme.plaqueShadow: Color get() = Ink

/** The markers, which must not belong: in the palette they would read as decoration. */
val CutoutMarker = Color(0xFFE5484D)

/** The hardware insets, which are a depth per edge rather than a rectangle. */
val InsetMarker = Color(0xFFE08A1E)

private val SampleShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun SampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkScheme else LightScheme,
        shapes = SampleShapes,
        content = content,
    )
}
