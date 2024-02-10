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

package dev.teogor.gleam

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

/**
 * Displays a [Gleam], a modal window that slides up from the bottom of the screen.
 * [GleamScaffold]s are suitable for presenting long lists of actions, detailed
 * information with icons, or confirmations. They temporarily disable other app content
 * and remain on screen until dismissed or a required action is taken.
 *
 * **Example:**
 * A simple example showcasing a Gleam with a button and some text:
 *
 * ```kotlin
 *   // Set a flag to control Gleam visibility (replace with your logic)
 *   var isGleamVisible by remember { mutableStateOf(false) }
 *
 *   GleamScaffold(
 *     isVisible = isGleamVisible,
 *     modifier = Modifier,
 *     gleamContent = {
 *       Column(modifier = Modifier.padding(16.dp)) {
 *         Text("This is the Gleam content!")
 *
 *         Button(
 *           onClick = { isGleamVisible = false },
 *           modifier = Modifier.align(Alignment.CenterHorizontally)
 *         ) {
 *           Text("Close")
 *         }
 *       }
 *     },
 *     gleamState = rememberGleamState(
 *       skipPartiallyExpanded = true
 *     ),
 *     gleamMaxWidth = Dp.Unspecified,
 *     shape = RoundedCornerShape(16.dp),
 *     containerColor = Color.White,
 *     contentColor = Color.Black,
 *     tonalElevation = 4.dp,
 *     scrimColor = Color.Black.copy(alpha = 0.6f),
 *     dragHandle = { GleamDefaults.DragHandle() },
 *     windowInsets = WindowInsets.Companion.navigationBars,
 *     properties = GleamDefaults.properties(
 *       shouldDismissOnBackPress = true,
 *     ),
 *   ) {
 *     Column(modifier = Modifier.padding(16.dp)) {
 *       Text("This is the screen.")
 *
 *       Button(
 *         onClick = { isGleamVisible = true },
 *         modifier = Modifier.fillMaxWidth()
 *       ) {
 *         Text("Show Gleam")
 *       }
 *     }
 *   }
 * ```
 *
 * @param gleamContent The content to be displayed inside the [Gleam]. Use this to define
 * the [Gleam]'s layout and UI components.
 * @param isVisible Whether the [Gleam] should be currently visible. Set this to `true`
 * to show the [Gleam] and `false` to hide it.
 * @param modifier Optional modifiers to apply to the [Gleam].
 * @param gleamState The state object managing the [Gleam]'s visibility and animation.
 * Use this to programmatically control the Gleam's behavior.
 * @param gleamMaxWidth The maximum width of the [Gleam] in [Dp]. Use [Dp.Unspecified] for
 * full screen width.
 * @param shape The shape of the [Gleam]'s container.
 * @param containerColor The background color of the [Gleam]'s container.
 * @param contentColor The preferred color for content inside the [Gleam]. Defaults to the
 * appropriate color based on the [containerColor] or the current theme.
 * @param tonalElevation The tonal elevation of the [Gleam], defining its shadow level.
 * @param scrimColor The color of the scrim that overlays the background content when the
 * [Gleam] is open.
 * @param dragHandle An optional visual marker to allow users to swipe the [Gleam] up and
 * down.
 * @param windowInsets Insets to be applied to the [Gleam]'s window for proper layout and
 * positioning.
 * @param properties Additional customization options for the [Gleam]'s behavior through
 * [GleamProperties].
 * @param content The content to be displayed inside the [Gleam].
 *
 * @see Gleam
 * @see GleamState
 * @see GleamProperties
 */
@Composable
@ExperimentalGleamApi
fun GleamScaffold(
  gleamContent: @Composable ColumnScope.() -> Unit,
  isVisible: Boolean,
  modifier: Modifier = Modifier,
  gleamState: GleamState = rememberGleamState(),
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

    if (isVisible) {
      Gleam(
        onDismissRequest = {
          if (gleamState.confirmValueChange(GleamValue.Hidden)) {
            scope.launch { gleamState.hide() }
          }
        },
        modifier = modifier,
        gleamState = gleamState,
        gleamMaxWidth = gleamMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        windowInsets = windowInsets,
        properties = properties,
        content = gleamContent,
      )
    }
  }
}
