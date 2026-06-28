package com.example.ordenesapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definimos únicamente la paleta de colores claros (puedes ajustar los colores si lo deseas)
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
    // Aquí puedes agregar más colores personalizados para el fondo, tarjetas, etc.
)

@Composable
fun OrdenesAppTheme(
    // 1. FORZAMOS A FALSE: Cambiamos el valor por defecto para ignorar 'isSystemInDarkTheme()'
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false, // Desactivamos el color dinámico de Android 12+ para asegurar consistencia
    content: @Composable () -> Unit
) {
    // 2. Nos aseguramos de que el esquema elegido sea siempre el Light
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()

            // 3. CONTROL DE LA BARRA DE ESTADO:
            // Le decimos al sistema que los íconos de la barra superior (batería, hora)
            // deben ser oscuros porque nuestro fondo siempre será claro.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}