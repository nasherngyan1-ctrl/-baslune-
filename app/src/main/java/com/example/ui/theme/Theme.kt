package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalAppLanguage
import com.example.core.localization.LocalStrings
import com.example.core.localization.stringsFor

private val BasluneLightColorScheme = lightColorScheme(
    primary = DeepEspresso,
    onPrimary = Ivory,
    primaryContainer = RichBrown,
    onPrimaryContainer = ChampagneGold,
    secondary = RichBrown,
    onSecondary = Ivory,
    secondaryContainer = ChampagneGold,
    onSecondaryContainer = DeepEspresso,
    tertiary = WarningAmber,
    onTertiary = Ivory,
    background = WarmCream,
    onBackground = DarkText,
    surface = Ivory,
    onSurface = DarkText,
    surfaceVariant = WarmCream,
    onSurfaceVariant = SecondaryText,
    outline = SoftBorders,
    outlineVariant = MutedTaupe,
    error = ErrorRed,
    onError = Ivory,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed
)

private val BasluneDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = DeepEspresso,
    primaryContainer = RichBrown,
    onPrimaryContainer = ChampagneGold,
    secondary = MutedTaupe,
    onSecondary = DeepEspresso,
    secondaryContainer = DarkCard,
    onSecondaryContainer = ChampagneGold,
    tertiary = WarningAmber,
    onTertiary = DeepEspresso,
    background = DarkSurface,
    onBackground = DarkOnSurface,
    surface = DarkCard,
    onSurface = DarkOnSurface,
    surfaceVariant = DeepEspresso,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    outlineVariant = MutedTaupe,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF42221F),
    onErrorContainer = Color(0xFFF2B8B5)
)

val BasluneShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun BasluneTheme(
    language: AppLanguage = AppLanguage.KURDISH_SORANI,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BasluneDarkColorScheme else BasluneLightColorScheme
    val typography = basluneTypography(useRtlScriptFont = language.isRtl)
    val strings = stringsFor(language)

    CompositionLocalProvider(
        LocalLayoutDirection provides language.layoutDirection,
        LocalStrings provides strings,
        LocalAppLanguage provides language
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = BasluneShapes,
            content = content
        )
    }
}
