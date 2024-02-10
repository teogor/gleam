/*
 * Copyright 2024 teogor (Teodor Grigor)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.teogor.gleam.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import dev.teogor.gleam.tokens.ColorSchemeKeyTokens

// Taken from AndroidX Material3
/**
 * Helper function for component color tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.colorScheme.fromToken(ExtendedFabBranded.BrandedContainerColor)``
 */
@Stable
fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
  return when (value) {
    ColorSchemeKeyTokens.Background -> background
    ColorSchemeKeyTokens.Error -> error
    ColorSchemeKeyTokens.ErrorContainer -> errorContainer
    ColorSchemeKeyTokens.InverseOnSurface -> inverseOnSurface
    ColorSchemeKeyTokens.InversePrimary -> inversePrimary
    ColorSchemeKeyTokens.InverseSurface -> inverseSurface
    ColorSchemeKeyTokens.OnBackground -> onBackground
    ColorSchemeKeyTokens.OnError -> onError
    ColorSchemeKeyTokens.OnErrorContainer -> onErrorContainer
    ColorSchemeKeyTokens.OnPrimary -> onPrimary
    ColorSchemeKeyTokens.OnPrimaryContainer -> onPrimaryContainer
    ColorSchemeKeyTokens.OnSecondary -> onSecondary
    ColorSchemeKeyTokens.OnSecondaryContainer -> onSecondaryContainer
    ColorSchemeKeyTokens.OnSurface -> onSurface
    ColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
    ColorSchemeKeyTokens.SurfaceTint -> surfaceTint
    ColorSchemeKeyTokens.OnTertiary -> onTertiary
    ColorSchemeKeyTokens.OnTertiaryContainer -> onTertiaryContainer
    ColorSchemeKeyTokens.Outline -> outline
    ColorSchemeKeyTokens.OutlineVariant -> outlineVariant
    ColorSchemeKeyTokens.Primary -> primary
    ColorSchemeKeyTokens.PrimaryContainer -> primaryContainer
    ColorSchemeKeyTokens.Scrim -> scrim
    ColorSchemeKeyTokens.Secondary -> secondary
    ColorSchemeKeyTokens.SecondaryContainer -> secondaryContainer
    ColorSchemeKeyTokens.Surface -> surface
    ColorSchemeKeyTokens.SurfaceVariant -> surfaceVariant
    ColorSchemeKeyTokens.SurfaceBright -> surfaceBright
    ColorSchemeKeyTokens.SurfaceContainer -> surfaceContainer
    ColorSchemeKeyTokens.SurfaceContainerHigh -> surfaceContainerHigh
    ColorSchemeKeyTokens.SurfaceContainerHighest -> surfaceContainerHighest
    ColorSchemeKeyTokens.SurfaceContainerLow -> surfaceContainerLow
    ColorSchemeKeyTokens.SurfaceContainerLowest -> surfaceContainerLowest
    ColorSchemeKeyTokens.SurfaceDim -> surfaceDim
    ColorSchemeKeyTokens.Tertiary -> tertiary
    ColorSchemeKeyTokens.TertiaryContainer -> tertiaryContainer
    else -> Color.Unspecified
  }
}

// Taken from AndroidX Material3
/**
 * Converts a color token key to the local color scheme provided by the theme
 * The color is subscribed to [LocalColorScheme] changes.
 */
val ColorSchemeKeyTokens.value: Color
  @ReadOnlyComposable
  @Composable
  get() = MaterialTheme.colorScheme.fromToken(this)
