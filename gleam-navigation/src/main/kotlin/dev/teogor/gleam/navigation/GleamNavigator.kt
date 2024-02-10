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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.navigation.FloatingWindow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorState
import androidx.navigation.plusAssign
import dev.teogor.gleam.ExperimentalGleamApi
import dev.teogor.gleam.GleamState
import dev.teogor.gleam.GleamValue
import dev.teogor.gleam.navigation.GleamNavigator.Destination
import dev.teogor.gleam.rememberGleamState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.transform

/**
 * The state of a [GleamScaffold] driven by the [GleamNavigator].
 *
 * @param gleamState The Gleam state that the [GleamNavigator] controls.
 */
@ExperimentalGleamApi
@Stable
public class GleamNavigatorState(internal val gleamState: GleamState) {
  /**
   * @see GleamState.isVisible
   */
  public val isVisible: Boolean
    get() = gleamState.isVisible

  /**
   * @see GleamState.currentValue
   */
  public val currentValue: GleamValue
    get() = gleamState.currentValue

  /**
   * @see GleamState.targetValue
   */
  public val targetValue: GleamValue
    get() = gleamState.targetValue
}

/**
 * Creates and remembers a [GleamNavigator] for use with Gleam navigations.
 *
 * This function creates a new [GleamNavigator] and stores it in the composition
 * context using `remember`. This ensures that the same instance of the navigator
 * is used throughout the composition hierarchy, even if the composable is recomposed.
 *
 * The navigator can be used to navigate between different Gleam screens by calling
 * methods like `navigate` and `popBackStack`.
 *
 * **Example:**
 * ```kotlin
 * @Composable
 * fun MyGleamScreen(navigator: GleamNavigator) {
 *   Button(onClick = { navigator.navigate("SecondScreen") }) {
 *     Text("Go to Second Screen")
 *   }
 * }
 *
 * @Composable
 * fun MyGleamApp() {
 *   val navigator = rememberGleamNavigator()
 *   MyGleamScreen(navigator)
 * }
 * ```
 *
 * @return A new instance of [GleamNavigator] associated with the current composition.
 */
@ExperimentalGleamApi
@Composable
public fun rememberGleamNavigator(): GleamNavigator {
  val gleamState = rememberGleamState()
  return remember { GleamNavigator(gleamState) }
}

/**
 * Associates a [NavHostController] with this [GleamNavigator], enabling navigation
 * using Gleam composables.
 *
 * This function adds the navigator to the `navigatorProvider` of the provided
 * navController`, making it available for navigation within Gleam screens.
 *
 * **Important:** Ensure that the `navController` you pass is the same instance used
 * to manage navigation in your application.
 *
 * **Example:**
 * ```kotlin
 * val navController = rememberNavController()
 * val navigator = rememberGleamNavigator()
 * navigator.provideNavController(navController)
 *
 * @Composable
 * fun MyGleamScreen(navigator: GleamNavigator) {
 *   Button(onClick = { navigator.navigate("SecondScreen") }) {
 *     Text("Go to Second Screen")
 *   }
 * }
 * ```
 *
 * @param navController The [NavHostController] instance responsible for
 * navigation operations.
 * @return The same [GleamNavigator] instance with the added [NavHostController].
 *
 */
@ExperimentalGleamApi
public fun GleamNavigator.provideNavController(
  navController: NavHostController,
) = also {
  navController.navigatorProvider += this
}

/**
 * Navigator that drives a [GleamState] for use with [GleamScaffold]s within the navigation
 * library. Every destination using this Navigator must set a valid [Composable] by defining
 * it directly on an instantiated [Destination] or calling [gleam].
 *
 * **Key behavior:**
 *
 * - The [gleamContent] [Composable] always displays the latest entry from the back stack.
 * - Navigating between Gleam destinations replaces the current content instead of creating
 * a new gleam.
 * - Dismissing the Gleam via user interaction pops the latest entry from the `state`'s back stack.
 *
 * **Public Usage:**
 *
 * Use [rememberGleamNavigator] to create and access a [GleamNavigator] instance in your composables.
 * This function handles memory management and ensures consistent usage throughout your application.
 *
 * @param gleamState The [GleamState] that the [GleamNavigator] will control.
 */
@ExperimentalGleamApi
@Navigator.Name("GleamNavigator")
public class GleamNavigator(
  internal val gleamState: GleamState,
) : Navigator<Destination>() {

  private var attached by mutableStateOf(false)

  /**
   * Get the back stack from the [state]. In some cases, the [gleamContent] might be composed
   * before the Navigator is attached, so we specifically return an empty flow if we aren't
   * attached yet.
   */
  private val backStack: StateFlow<List<NavBackStackEntry>>
    get() = if (attached) {
      state.backStack
    } else {
      MutableStateFlow(emptyList())
    }

  /**
   * Determines the overall visibility state of the Gleam content.
   *
   * This property combines two conditions:
   *
   * - `shouldBeVisible`: Whether the current navigation state should be
   * visible based on the back stack.
   * - `gleamState.isVisible`: The internal visibility state of the Gleam component.
   *
   * Both conditions must be true for the content to be considered visible.
   *
   * This allows for fine-grained control over visibility based on navigation
   * state and Gleam-specific logic.
   */
  val isVisible: Boolean
    @Composable
    get() = shouldBeVisible && gleamState.isVisible

  /**
   * Determines whether the current navigation state should be visible based
   * on the back stack.
   *
   * This property uses `produceState` to collect changes from the `backStack` `StateFlow`.
   *
   * - It transforms the `backStack` to emit `true` if there is at least one entry,
   * indicating the content should be visible.
   * - It collects the transformed value and updates the `shouldBeVisible` state.
   *
   * This ensures that `shouldBeVisible` reflects the current navigation state and changes
   * dynamically as the back stack evolves.
   */
  val shouldBeVisible: Boolean
    @Composable
    get() = produceState(
      initialValue = false,
      key1 = backStack,
    ) {
      backStack.transform { backStackEntries ->
        emit(backStackEntries.lastOrNull() != null)
      }.collect {
        value = it
      }
    }.value

  /**
   * Get the transitionsInProgress from the [state]. In some cases, the [gleamContent] might be
   * composed before the Navigator is attached, so we specifically return an empty flow if we
   * aren't attached yet.
   */
  internal val transitionsInProgress: StateFlow<Set<NavBackStackEntry>>
    get() = if (attached) {
      state.transitionsInProgress
    } else {
      MutableStateFlow(emptySet())
    }

  /**
   * Access properties of the [GleamScaffold]'s [GleamState]
   */
  public val navigatorGleamState: GleamNavigatorState = GleamNavigatorState(gleamState)

  /**
   * A [Composable] function that hosts the current gleam content. This should be set as
   * gleamContent of your [GleamScaffold].
   */
  public val gleamContent: @Composable ColumnScope.() -> Unit = {
    val saveableStateHolder = rememberSaveableStateHolder()
    val transitionsInProgressEntries by transitionsInProgress.collectAsState()

    // The latest back stack entry, retained until the gleam is completely hidden
    // While the back stack is updated immediately, we might still be hiding the gleam, so
    // we keep the entry around until the gleam is hidden
    val retainedEntry by produceState<NavBackStackEntry?>(
      initialValue = null,
      key1 = backStack,
    ) {
      backStack
        .transform { backStackEntries ->
          val targetPriorGleam = backStackEntries.getOrNull(backStackEntries.size - 2)
          // Always hide the gleam when the back stack is updated
          // Regardless of whether we're popping or pushing, we always want to hide
          // the gleam first before deciding whether to re-show it or keep it hidden
          // TODO bug - prior hide() but this triggers reshowing partially empty
          //  bottom gleam after new screen was launched. Also this makes the bug
          //  of gleam being hidden without animation when a new one is launched.
          try {
            if (targetPriorGleam != null) {
              gleamState.hide()
            } else {
              gleamState.forcedHide()
            }
          } catch (_: CancellationException) {
            // We catch but ignore possible cancellation exceptions as we don't want
            // them to bubble up and cancel the whole produceState coroutine
          } finally {
            emit(backStackEntries.lastOrNull())
          }
        }
        .collect {
          value = it
        }
    }

    if (retainedEntry != null) {
      LaunchedEffect(retainedEntry) {
        gleamState.show()
      }

      BackHandler {
        state.popWithTransition(popUpTo = retainedEntry!!, saveState = false)
      }
    }

    GleamContentHost(
      backStackEntry = retainedEntry,
      gleamState = gleamState,
      saveableStateHolder = saveableStateHolder,
      onGleamShown = {
        transitionsInProgressEntries.forEach(state::markTransitionComplete)
      },
      onGleamDismissed = { backStackEntry ->
        // Gleam dismissal can be started through popBackStack in which case we have a
        // transition that we'll want to complete
        if (transitionsInProgressEntries.contains(backStackEntry)) {
          state.markTransitionComplete(backStackEntry)
        }
        // If there is no transition in progress, the gleam has been dimissed by the
        // user (for example by tapping on the scrim or through an accessibility action)
        // In this case, we will immediately pop without a transition as the gleam has
        // already been hidden
        else {
          state.pop(popUpTo = backStackEntry, saveState = false)
        }
      },
    )
  }

  override fun onAttach(state: NavigatorState) {
    super.onAttach(state)
    attached = true
  }

  override fun createDestination(): Destination = Destination(
    navigator = this,
    content = {},
  )

  override fun navigate(
    entries: List<NavBackStackEntry>,
    navOptions: NavOptions?,
    navigatorExtras: Extras?,
  ) {
    entries.forEach { entry ->
      state.pushWithTransition(entry)
    }
  }

  override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
    state.popWithTransition(popUpTo, savedState)
  }

  /**
   * [NavDestination] specific to [GleamNavigator]
   */
  @NavDestination.ClassType(Composable::class)
  public class Destination(
    navigator: GleamNavigator,
    internal val content: @Composable ColumnScope.(NavBackStackEntry) -> Unit,
  ) : NavDestination(navigator), FloatingWindow
}
