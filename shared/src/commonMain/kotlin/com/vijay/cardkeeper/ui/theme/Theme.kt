package com.vijay.cardkeeper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.vijay.cardkeeper.ui.theme.DarkBackground
import com.vijay.cardkeeper.ui.theme.CardSurface
import com.vijay.cardkeeper.ui.theme.TextPrimary
import com.vijay.cardkeeper.ui.theme.LightBackground
import com.vijay.cardkeeper.ui.theme.LightSurface
import com.vijay.cardkeeper.ui.theme.LightOnBackground

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = DarkBackground,
    surface = CardSurface,
    onBackground = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnBackground
)

@Composable
fun CardKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
