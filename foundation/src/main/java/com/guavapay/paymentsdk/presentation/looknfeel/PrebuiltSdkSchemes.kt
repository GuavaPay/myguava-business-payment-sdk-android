package com.guavapay.paymentsdk.presentation.looknfeel

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

internal val PrebuiltLightColorScheme = lightColorScheme(
  primary = BrandFocus,
  onPrimary = BrandOnFocus,
  primaryContainer = BrandPrimary,
  onPrimaryContainer = BrandOnPrimary,
  secondary = BrandOutline,
  onSecondary = BrandSurface,
  secondaryContainer = BrandHint,
  onSecondaryContainer = BrandOnSurface,
  tertiary = BrandFocus,
  onTertiary = BrandOnFocus,
  tertiaryContainer = BrandPrimary,
  onTertiaryContainer = BrandOnPrimary,
  error = BrandError,
  onError = BrandSurface,
  errorContainer = BrandError.copy(alpha = 0.1f),
  onErrorContainer = BrandError,
  background = BrandSurface,
  onBackground = BrandOnSurface,
  surface = BrandSurface,
  onSurface = BrandOnSurface,
  surfaceVariant = BrandInputBackground,
  onSurfaceVariant = BrandHint,
  outline = BrandOutline,
  outlineVariant = BrandDivider,
  scrim = BrandOnSurface.copy(alpha = 0.32f),
  inverseSurface = BrandOnSurface,
  inverseOnSurface = BrandSurface,
  inversePrimary = BrandFocusDark,
  surfaceTint = BrandFocus
)

internal val PrebuiltDarkColorScheme = darkColorScheme(
  primary = BrandFocusDark,
  onPrimary = BrandOnFocus,
  primaryContainer = BrandPrimaryDark,
  onPrimaryContainer = BrandOnPrimaryDark,
  secondary = BrandOutlineDark,
  onSecondary = BrandSurfaceDark,
  secondaryContainer = BrandHintDark,
  onSecondaryContainer = BrandOnSurfaceDark,
  tertiary = BrandFocusDark,
  onTertiary = BrandOnFocus,
  tertiaryContainer = BrandPrimaryDark,
  onTertiaryContainer = BrandOnPrimaryDark,
  error = BrandErrorDark,
  onError = BrandSurfaceDark,
  errorContainer = BrandErrorDark.copy(alpha = 0.1f),
  onErrorContainer = BrandErrorDark,
  background = BrandSurfaceDark,
  onBackground = BrandOnSurfaceDark,
  surface = BrandSurfaceDark,
  onSurface = BrandOnSurfaceDark,
  surfaceVariant = BrandInputBackgroundDark,
  onSurfaceVariant = BrandHintDark,
  outline = BrandOutlineDark,
  outlineVariant = BrandDividerDark,
  scrim = BrandOnSurfaceDark.copy(alpha = 0.32f),
  inverseSurface = BrandOnSurfaceDark,
  inverseOnSurface = BrandSurfaceDark,
  inversePrimary = BrandFocus,

  surfaceTint = BrandFocusDark
)
