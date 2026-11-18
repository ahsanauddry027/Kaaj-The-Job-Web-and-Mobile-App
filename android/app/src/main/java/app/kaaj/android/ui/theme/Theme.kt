package app.kaaj.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// The web app's palette (web/src/index.css): brand blue, emerald for "verified", amber for "waiting".
val Brand = Color(0xFF1D4ED8)
val BrandDark = Color(0xFF1E40AF)
val BrandSoft = Color(0xFFDBEAFE)
val BrandFaint = Color(0xFFEFF6FF)
val Page = Color(0xFFF5F7FB)
val Ink = Color(0xFF0F172A)
val InkSoft = Color(0xFF475569)
val Line = Color(0xFFE2E8F0)

val Emerald = Color(0xFF047857)
val EmeraldSoft = Color(0xFFD1FAE5)
val Amber = Color(0xFFB45309)
val AmberSoft = Color(0xFFFEF3C7)
val Red = Color(0xFFB91C1C)
val RedSoft = Color(0xFFFEE2E2)
val Slate = Color(0xFF475569)
val SlateSoft = Color(0xFFF1F5F9)

private val colors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = BrandDark,
    secondary = Emerald,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSoft,
    onSecondaryContainer = Emerald,
    tertiary = Amber,
    tertiaryContainer = AmberSoft,
    background = Page,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = SlateSoft,
    onSurfaceVariant = InkSoft,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF8FAFC),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Line,
    error = Red,
    errorContainer = RedSoft,
)

private val shapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

/** Light theme only, like the web app. Android's system font covers Bangla script. */
@Composable
fun KaajTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, shapes = shapes, content = content)
}
