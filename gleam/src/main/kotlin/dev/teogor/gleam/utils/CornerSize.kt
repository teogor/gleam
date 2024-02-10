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

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.platform.InspectableValue
import androidx.compose.ui.unit.Dp

fun CornerSize.toDp() = if (this is InspectableValue) {
  valueOverride as? Dp ?: Dp.Unspecified
} else {
  Dp.Unspecified
}

fun CornerSize.toPx() = if (this is InspectableValue) {
  valueOverride as? Float ?: 0f
} else {
  0f
}
