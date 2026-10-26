package com.example.eudayan.redesign

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.eudayan.R

data class EudyaanPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val text: Color,
    val muted: Color,
    val secondary: Color,
    val accent: Color,
    val button: Color,
    val buttonEnd: Color,
    val peach: Color,
    val sage: Color,
    val border: Color,
    val coral: Color,
    val danger: Color,
    val dangerSurface: Color,
    val onButton: Color = Color.White
)

private val LightPalette = EudyaanPalette(
    isDark = false,
    background = Color(0xFFFFFDF8), surface = Color.White,
    text = Color(0xFF081127), muted = Color(0xFF52646B), secondary = Color(0xFF596C89),
    accent = Color(0xFF075941), button = Color(0xFF075941), buttonEnd = Color(0xFF3D7867),
    peach = Color(0xFFFCEDE4), sage = Color(0xFFEAF3EE), border = Color(0xFFE0E8E3), coral = Color(0xFFE88E70),
    danger = Color(0xFFB64B43), dangerSurface = Color(0xFFFFF1EF)
)

private val DarkPalette = EudyaanPalette(
    isDark = true,
    background = Color(0xFF101A1B), surface = Color(0xFF1A292A),
    text = Color(0xFFF1F5F1), muted = Color(0xFFB7C8C4), secondary = Color(0xFFB1C8D7),
    accent = Color(0xFF9DDCBB), button = Color(0xFF145D49), buttonEnd = Color(0xFF287B62),
    peach = Color(0xFF342923), sage = Color(0xFF20372F), border = Color(0xFF344744), coral = Color(0xFFFFB397),
    danger = Color(0xFFFFA99D), dangerSurface = Color(0xFF3A2525)
)

val LocalEudyaanPalette = staticCompositionLocalOf { LightPalette }

val Ivory: Color @Composable get() = LocalEudyaanPalette.current.background
val CardWhite: Color @Composable get() = LocalEudyaanPalette.current.surface
val Ink: Color @Composable get() = LocalEudyaanPalette.current.text
val Muted: Color @Composable get() = LocalEudyaanPalette.current.muted
val BlueGray: Color @Composable get() = LocalEudyaanPalette.current.secondary
val Forest: Color @Composable get() = LocalEudyaanPalette.current.accent
val ButtonForest: Color @Composable get() = LocalEudyaanPalette.current.button
val ForestLight: Color @Composable get() = LocalEudyaanPalette.current.buttonEnd
val Peach: Color @Composable get() = LocalEudyaanPalette.current.peach
val Sage: Color @Composable get() = LocalEudyaanPalette.current.sage
val CardOutline: Color @Composable get() = LocalEudyaanPalette.current.border
val Coral: Color @Composable get() = LocalEudyaanPalette.current.coral
val Danger: Color @Composable get() = LocalEudyaanPalette.current.danger
val DangerSurface: Color @Composable get() = LocalEudyaanPalette.current.dangerSurface
val OnButton: Color @Composable get() = LocalEudyaanPalette.current.onButton

val RubikFamily = FontFamily(
    Font(R.font.rubik_regular, FontWeight.Normal),
    Font(R.font.rubik_medium, FontWeight.Medium),
    Font(R.font.rubik_semibold, FontWeight.SemiBold),
    Font(R.font.rubik_bold, FontWeight.Bold)
)
val AwesomeTitleFamily = FontFamily(Font(R.font.awesome_title, FontWeight.Normal))

@Composable
fun EudyaanTheme(dark: Boolean, content: @Composable () -> Unit) {
    val palette = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) darkColorScheme(
        primary = palette.accent, onPrimary = Color(0xFF092018),
        background = palette.background, onBackground = palette.text,
        surface = palette.surface, onSurface = palette.text,
        surfaceVariant = palette.sage, onSurfaceVariant = palette.muted,
        outline = palette.border, error = palette.danger
    ) else lightColorScheme(
        primary = palette.accent, onPrimary = Color.White,
        background = palette.background, onBackground = palette.text,
        surface = palette.surface, onSurface = palette.text,
        surfaceVariant = palette.sage, onSurfaceVariant = palette.muted,
        outline = palette.border, error = palette.danger
    )
    CompositionLocalProvider(LocalEudyaanPalette provides palette) {
        MaterialTheme(colorScheme = scheme) {
            ProvideTextStyle(TextStyle(fontFamily = RubikFamily)) { content() }
        }
    }
}
