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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.teogor.gleam.tokens.ShapeKeyTokens

// Taken from AndroidX Material3
/** Helper function for component shape tokens. Used to grab the top values of a shape parameter. */
internal fun CornerBasedShape.top(): CornerBasedShape {
  return copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

// Taken from AndroidX Material3
/**
 * Helper function for component shape tokens. Used to grab the bottom values of a shape parameter.
 */
internal fun CornerBasedShape.bottom(): CornerBasedShape {
  return copy(topStart = CornerSize(0.0.dp), topEnd = CornerSize(0.0.dp))
}

// Taken from AndroidX Material3
/** Helper function for component shape tokens. Used to grab the start values of a shape parameter. */
internal fun CornerBasedShape.start(): CornerBasedShape {
  return copy(topEnd = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

// Taken from AndroidX Material3
/** Helper function for component shape tokens. Used to grab the end values of a shape parameter. */
internal fun CornerBasedShape.end(): CornerBasedShape {
  return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

// Taken from AndroidX Material3
/**
 * Helper function for component shape tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.shapes.fromToken(FabPrimarySmallTokens.ContainerShape)``
 */
fun Shapes.fromToken(value: ShapeKeyTokens): Shape {
  return when (value) {
    ShapeKeyTokens.CornerExtraLarge -> extraLarge
    ShapeKeyTokens.CornerExtraLargeTop -> extraLarge.top()
    ShapeKeyTokens.CornerExtraSmall -> extraSmall
    ShapeKeyTokens.CornerExtraSmallTop -> extraSmall.top()
    ShapeKeyTokens.CornerFull -> CircleShape
    ShapeKeyTokens.CornerLarge -> large
    ShapeKeyTokens.CornerLargeEnd -> large.end()
    ShapeKeyTokens.CornerLargeTop -> large.top()
    ShapeKeyTokens.CornerMedium -> medium
    ShapeKeyTokens.CornerNone -> RectangleShape
    ShapeKeyTokens.CornerSmall -> small
  }
}

// Taken from AndroidX Material3
/**
 * Converts a shape token key to the local shape provided by the theme
 */
val ShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.shapes.fromToken(this)
