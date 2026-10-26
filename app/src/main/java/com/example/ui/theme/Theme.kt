package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = NeonEmerald,
  onPrimary = CyberBackground,
  primaryContainer = DarkNavyShield,
  onPrimaryContainer = NeonCyan,
  secondary = NeonTeal,
  onSecondary = CyberBackground,
  secondaryContainer = CyberSurfaceVariant,
  onSecondaryContainer = NeonCyan,
  tertiary = CyberBlue,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  error = AlertRed,
  onError = TextPrimary,
  outline = CyberBorder,
  outlineVariant = CyberBorderSubtle
)

@Composable
fun ShieldXTheme(
  darkTheme: Boolean = true, // Cybersecurity apps look best in intentional deep dark
  content: @Composable () -> Unit
) {
  val colorScheme = DarkColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = CyberBackground.toArgb()
      window.navigationBarColor = CyberBackground.toArgb()
      WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = false
        isAppearanceLightNavigationBars = false
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
