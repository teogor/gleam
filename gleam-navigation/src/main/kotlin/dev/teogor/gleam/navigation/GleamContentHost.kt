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

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.snapshotFlow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.LocalOwnersProvider
import dev.teogor.gleam.ExperimentalGleamApi
import dev.teogor.gleam.GleamState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

/**
 * Hosts a [GleamNavigator.Destination]'s [NavBackStackEntry] and its
 * `content`, providing an `onGleamDismissed` callback. It also shows and hides the
 * [GleamScaffold] through the [GleamState] when the Gleam content enters or leaves
 * the composition.
 *
 * @param backStackEntry The [NavBackStackEntry] holding the [GleamNavigator.Destination],
 * or null if there is no [NavBackStackEntry]
 * @param gleamState The [GleamState] used to observe and control the Gleam visibility
 * @param onGleamShown Callback when the Gleam is shown. Typically, you'll want to
 * trigger actions here.
 * @param onGleamDismissed Callback when the Gleam has been dismissed. Typically, you'll want to
 * pop the back stack here.
 */
@ExperimentalGleamApi
@Composable
internal fun ColumnScope.GleamContentHost(
  backStackEntry: NavBackStackEntry?,
  gleamState: GleamState,
  saveableStateHolder: SaveableStateHolder,
  onGleamShown: (entry: NavBackStackEntry) -> Unit,
  onGleamDismissed: (entry: NavBackStackEntry) -> Unit,
) {
  if (backStackEntry != null) {
    val currentOnGleamShown by rememberUpdatedState(onGleamShown)
    val currentOnGleamDismissed by rememberUpdatedState(onGleamDismissed)
    LaunchedEffect(gleamState, backStackEntry) {
      snapshotFlow { gleamState.isVisible }
        // We are only interested in changes in the Gleam's visibility
        .distinctUntilChanged()
        // distinctUntilChanged emits the initial value which we don't need
        .drop(1)
        .collect { visible ->
          if (visible) {
            currentOnGleamShown(backStackEntry)
          } else {
            currentOnGleamDismissed(backStackEntry)
          }
        }
    }
    backStackEntry.LocalOwnersProvider(saveableStateHolder) {
      val content = (backStackEntry.destination as GleamNavigator.Destination).content
      content(backStackEntry)
    }
  }
}
