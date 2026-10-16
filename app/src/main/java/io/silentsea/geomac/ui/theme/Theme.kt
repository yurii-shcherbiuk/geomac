package io.silentsea.geomac.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun GeomacTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Color(0xFF98D4A3),
            onPrimary = Color(0xFF003919),
            primaryContainer = Color(0xFF17512C),
            onPrimaryContainer = Color(0xFFB4F1BE),
            secondary = Color(0xFFB7CCB7),
            onSecondary = Color(0xFF233426),
            secondaryContainer = Color(0xFF394B3B),
            onSecondaryContainer = Color(0xFFD3E8D3),
            tertiary = Color(0xFFA2CED8),
            onTertiary = Color(0xFF01363F),
            tertiaryContainer = Color(0xFF204D56),
            onTertiaryContainer = Color(0xFFBDEAF5),
            error = Color(0xFFFFB4AB),
            onError = Color(0xFF690005),
            errorContainer = Color(0xFF93000A),
            onErrorContainer = Color(0xFFFFDAD6),
            background = Color(0xFF101510),
            onBackground = Color(0xFFDFE4DC),
            surface = Color(0xFF101510),
            onSurface = Color(0xFFDFE4DC),
            surfaceVariant = Color(0xFF414941),
            onSurfaceVariant = Color(0xFFC1C9BF),
            outline = Color(0xFF8B938A),
            outlineVariant = Color(0xFF414941),
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFFDFE4DC),
            inverseOnSurface = Color(0xFF2D322D),
            inversePrimary = Color(0xFF316A41),
            surfaceDim = Color(0xFF101510),
            surfaceBright = Color(0xFF353A35),
            surfaceContainerLowest = Color(0xFF0B0F0B),
            surfaceContainerLow = Color(0xFF181D18),
            surfaceContainer = Color(0xFF1C211C),
            surfaceContainerHigh = Color(0xFF262B26),
            surfaceContainerHighest = Color(0xFF313631),
        )

        else -> lightColorScheme(
            primary = Color(0xFF316A41),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFB4F1BE),
            onPrimaryContainer = Color(0xFF17512C),
            secondary = Color(0xFF506352),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD3E8D3),
            onSecondaryContainer = Color(0xFF394B3B),
            tertiary = Color(0xFF3A656E),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFBDEAF5),
            onTertiaryContainer = Color(0xFF204D56),
            error = Color(0xFFBA1A1A),
            onError = Color(0xFFFFFFFF),
            errorContainer = Color(0xFFFFDAD6),
            onErrorContainer = Color(0xFF93000A),
            background = Color(0xFFF6FBF3),
            onBackground = Color(0xFF181D18),
            surface = Color(0xFFF6FBF3),
            onSurface = Color(0xFF181D18),
            surfaceVariant = Color(0xFFDDE5DA),
            onSurfaceVariant = Color(0xFF414941),
            outline = Color(0xFF717970),
            outlineVariant = Color(0xFFC1C9BF),
            scrim = Color(0xFF000000),
            inverseSurface = Color(0xFF2D322D),
            inverseOnSurface = Color(0xFFEEF2EA),
            inversePrimary = Color(0xFF98D4A3),
            surfaceDim = Color(0xFFD7DBD4),
            surfaceBright = Color(0xFFF6FBF3),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF1F5ED),
            surfaceContainer = Color(0xFFEBEFE7),
            surfaceContainerHigh = Color(0xFFE5EAE2),
            surfaceContainerHighest = Color(0xFFDFE4DC),
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content
    )
}