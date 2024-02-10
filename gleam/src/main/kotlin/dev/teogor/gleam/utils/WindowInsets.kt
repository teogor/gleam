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

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable

/**
 * Provides `WindowInsets` with only the vertical system bars (top and bottom), excluding horizontal ones.
 *
 * Use this when visual components like backgrounds or layout elements need to respect system bars in their
 * dimensions without affecting interactive elements or content placement.
 *
 * This property is convenient for:
 * - Applying vertical padding/margins based on system bar heights.
 * - Positioning visuals like logos or toolbars within available vertical space.
 *
 * **Example:**
 *
 * ```kotlin
 * Column(
 *   modifier = Modifier.windowInsetsPadding(WindowInsets.systemBarsForVerticalVisualComponents)
 * ) {
 *   // Content respecting vertical system bars for spacing
 * }
 * ```
 */
val WindowInsets.Companion.verticalSystemBarsForVisuals: WindowInsets
  @Composable
  get() = systemBars.only(WindowInsetsSides.Vertical)

/**
 * Provides `WindowInsets` with only the horizontal system bars (left and right), excluding vertical ones.
 *
 * Use this when visual components like backgrounds or layout elements need to respect system bars in their
 * dimensions without affecting interactive elements or content placement on the horizontal axis.
 *
 * This property is convenient for:
 * - Applying horizontal padding/margins based on system bar widths.
 * - Positioning visuals like left/right sidebars within available horizontal space.
 *
 * **Example:**
 *
 * ```kotlin
 * Row(
 *   modifier = Modifier.windowInsetsPadding(WindowInsets.systemBarsForHorizontalVisualComponents)
 * ) {
 *   // Content respecting horizontal system bars for spacing
 * }
 * ```
 */
val WindowInsets.Companion.horizontalSystemBarsForVisuals: WindowInsets
  @Composable
  get() = systemBars.only(WindowInsetsSides.Horizontal)

/**
 * Provides `WindowInsets` with all insets set to zero, effectively ignoring system bars.
 *
 * **Use with caution!** This can potentially disrupt layouts and cause content to overlap with system bars.
 * Consider alternative approaches:
 *
 * - **Adjust Specific Insets:** Modify only necessary insets for controlled layout.
 * - **Use `Modifier.fillMaxSize()`:** Expand content to fill available space without considering insets.
 * - **Custom `windowInsets` Logic:** Implement custom logic for complex inset handling scenarios.
 *
 * **Example:**
 *
 * ```kotlin
 * Gleam(
 *   modifier = Modifier.windowInsets(WindowInsets.none)
 * ) {
 *   // Gleam content might overlap with system bars
 * }
 * ```
 */
val WindowInsets.Companion.none: WindowInsets
  @Composable
  get() = systemBars.exclude(WindowInsets.systemBars)
