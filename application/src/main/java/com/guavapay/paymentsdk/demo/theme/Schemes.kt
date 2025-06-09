package com.guavapay.paymentsdk.demo.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val LightColorScheme = lightColorScheme(
  primary = Primary,
  onPrimary = Background,
  primaryContainer = SecondaryVariant,
  onPrimaryContainer = OnSurface,

  secondary = Secondary,
  onSecondary = Background,
  secondaryContainer = SecondaryVariant,
  onSecondaryContainer = OnSurface,

  tertiary = Accent,
  onTertiary = Background,
  tertiaryContainer = SecondaryVariant,
  onTertiaryContainer = OnSurface,

  error = Error,
  onError = Background,
  errorContainer = Error.copy(alpha = 0.1f),
  onErrorContainer = Error,

  background = Background,
  onBackground = OnSurface,
  surface = Surface,
  onSurface = OnSurface,
  surfaceVariant = Surface,
  onSurfaceVariant = OnSurface.copy(alpha = 0.7f),

  outline = Outline,
  outlineVariant = Outline.copy(alpha = 0.5f),
  scrim = OnSurface.copy(alpha = 0.32f),
  inverseSurface = OnSurface,
  inverseOnSurface = Surface,
  inversePrimary = PrimaryDark
)

val DarkColorScheme = darkColorScheme(
  primary = PrimaryDark,
  onPrimary = BackgroundDark,
  primaryContainer = PrimaryVariantDark,
  onPrimaryContainer = OnSurfaceDark,

  secondary = SecondaryDark,
  onSecondary = BackgroundDark,
  secondaryContainer = SecondaryVariantDark,
  onSecondaryContainer = OnSurfaceDark,

  tertiary = Accent,
  onTertiary = BackgroundDark,
  tertiaryContainer = SecondaryDark,
  onTertiaryContainer = OnSurfaceDark,

  error = Error,
  onError = BackgroundDark,
  errorContainer = Error.copy(alpha = 0.1f),
  onErrorContainer = Error,

  background = BackgroundDark,
  onBackground = OnSurfaceDark,
  surface = SurfaceDark,
  onSurface = OnSurfaceDark,
  surfaceVariant = SurfaceDark,
  onSurfaceVariant = OnSurfaceDark.copy(alpha = 0.7f),

  outline = OutlineDark,
  outlineVariant = OutlineDark.copy(alpha = 0.5f),
  scrim = OnSurfaceDark.copy(alpha = 0.32f),
  inverseSurface = OnSurfaceDark,
  inverseOnSurface = SurfaceDark,
  inversePrimary = Primary
)