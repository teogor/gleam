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

package dev.teogor.gleam.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import dev.teogor.gleam.ExperimentalGleamApi
import dev.teogor.gleam.Gleam
import dev.teogor.gleam.GleamDefaults
import dev.teogor.gleam.GleamProperties
import dev.teogor.gleam.GleamScaffold
import dev.teogor.gleam.GleamValue.Hidden
import kotlinx.coroutines.launch

/**
 * Helper function to create a [GleamScaffold] from a [GleamNavigator].
 *
 * @see [GleamScaffold]
 */
@ExperimentalGleamApi
@Composable
fun GleamScaffold(
  navigator: GleamNavigator,
  modifier: Modifier = Modifier,
  gleamMaxWidth: Dp = GleamDefaults.GleamMaxWidth,
  shape: Shape = GleamDefaults.ExpandedShape,
  containerColor: Color = GleamDefaults.ContainerColor,
  contentColor: Color = contentColorFor(containerColor),
  tonalElevation: Dp = GleamDefaults.Elevation,
  scrimColor: Color = GleamDefaults.ScrimColor,
  dragHandle: @Composable (() -> Unit)? = { GleamDefaults.DragHandle() },
  windowInsets: WindowInsets = GleamDefaults.windowInsets,
  properties: GleamProperties = GleamDefaults.properties(),
  content: @Composable () -> Unit,
) {
  val scope = rememberCoroutineScope()
  Box(Modifier.fillMaxSize()) {
    content()

    if (navigator.shouldBeVisible) {
      Gleam(
        onDismissRequest = {
          if (navigator.gleamState.confirmValueChange(Hidden)) {
            scope.launch { navigator.gleamState.hide() }
          }
        },
        modifier = modifier,
        gleamState = navigator.gleamState,
        gleamMaxWidth = gleamMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        properties = properties,
        content = navigator.gleamContent,
      )
    }
  }
}
