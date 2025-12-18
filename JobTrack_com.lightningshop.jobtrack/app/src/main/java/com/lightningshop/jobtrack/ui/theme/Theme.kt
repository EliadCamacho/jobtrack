package com.lightningshop.jobtrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.lightningshop.jobtrack.settings.ThemeMode
import com.lightningshop.jobtrack.settings.ThemeState
import kotlin.math.roundToInt

@Composable
fun JobTrackTheme(
  themeState: ThemeState,
  content: @Composable () -> Unit,
) {
  val dark = when (themeState.mode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
  }

  val context = LocalContext.current
  val seed = Color(themeState.seedColorArgb)

  val colorScheme = remember(themeState, dark) {
    createColorScheme(
      dark = dark,
      dynamic = themeState.dynamicColor,
      seed = seed,
      contextProvider = { context },
    )
  }

  val baseTypography = MaterialTheme.typography
  val scaledTypography = remember(themeState.typographyScale) {
    scaleTypography(baseTypography, themeState.typographyScale)
  }

  val shapes = remember(themeState.cornerRadiusDp) {
    val r = themeState.cornerRadiusDp.dp
    Shapes(
      extraSmall = androidx.compose.foundation.shape.RoundedCornerShape((r.value * 0.45f).dp),
      small = androidx.compose.foundation.shape.RoundedCornerShape((r.value * 0.7f).dp),
      medium = androidx.compose.foundation.shape.RoundedCornerShape(r),
      large = androidx.compose.foundation.shape.RoundedCornerShape((r.value * 1.25f).dp),
      extraLarge = androidx.compose.foundation.shape.RoundedCornerShape((r.value * 1.6f).dp),
    )
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = scaledTypography,
    shapes = shapes,
    content = content,
  )
}

private fun createColorScheme(
  dark: Boolean,
  dynamic: Boolean,
  seed: Color,
  contextProvider: () -> android.content.Context,
): ColorScheme {
  if (dynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val ctx = contextProvider()
    return if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
  }

  // Seed-based scheme (close to Material You without wallpaper).
  return if (dark) {
    darkColorScheme(
      primary = seed,
      surface = darkColorScheme().surfaceColorAtElevation(1.dp),
    )
  } else {
    lightColorScheme(
      primary = seed,
      surface = lightColorScheme().surfaceColorAtElevation(1.dp),
    )
  }
}

private fun scaleTypography(typography: Typography, scale: Float): Typography {
    fun TextStyle.scaled(): TextStyle =
    copy(
      fontSize = if (fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) fontSize * scale else fontSize,
      lineHeight = if (lineHeight != androidx.compose.ui.unit.TextUnit.Unspecified) lineHeight * scale else lineHeight,
    )

  return Typography(
    displayLarge = typography.displayLarge.scaled(),
    displayMedium = typography.displayMedium.scaled(),
    displaySmall = typography.displaySmall.scaled(),
    headlineLarge = typography.headlineLarge.scaled(),
    headlineMedium = typography.headlineMedium.scaled(),
    headlineSmall = typography.headlineSmall.scaled(),
    titleLarge = typography.titleLarge.scaled(),
    titleMedium = typography.titleMedium.scaled(),
    titleSmall = typography.titleSmall.scaled(),
    bodyLarge = typography.bodyLarge.scaled(),
    bodyMedium = typography.bodyMedium.scaled(),
    bodySmall = typography.bodySmall.scaled(),
    labelLarge = typography.labelLarge.scaled(),
    labelMedium = typography.labelMedium.scaled(),
    labelSmall = typography.labelSmall.scaled(),
  )
}
