/*
 * Copyright 2023 teogor (Teodor Grigor)
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

package dev.teogor.gleam.tokens

import androidx.compose.ui.unit.dp

internal object GleamTokens {
  val DockedContainerColor = ColorSchemeKeyTokens.Surface
  val DockedContainerShape = ShapeKeyTokens.CornerExtraLargeTop
  val DockedContainerSurfaceTintLayerColor = ColorSchemeKeyTokens.SurfaceTint
  val DockedDragHandleColor = ColorSchemeKeyTokens.OnSurfaceVariant
  val DockedDragHandleHeight = 4.0.dp
  const val DockedDragHandleOpacity = 0.4f
  val DockedDragHandleWidth = 32.0.dp
  val DockedMinimizedContainerShape = ShapeKeyTokens.CornerNone
  val DockedModalContainerElevation = ElevationTokens.Level1
  val DockedStandardContainerElevation = ElevationTokens.Level1
}
