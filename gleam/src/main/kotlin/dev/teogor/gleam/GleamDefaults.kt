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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import dev.teogor.gleam.GleamValue.Expanded
import dev.teogor.gleam.GleamValue.Hidden
import dev.teogor.gleam.GleamValue.PartiallyExpanded
import dev.teogor.gleam.tokens.GleamTokens
import dev.teogor.gleam.tokens.ScrimTokens
import dev.teogor.gleam.utils.value
import dev.teogor.gleam.utils.verticalSystemBarsForVisuals
import kotlinx.coroutines.CancellationException

/**
 * State of a Gleam composable, such as [Gleam].
 *
 * Contains states relating to its swipe position and animations between state values.
 *
 * @param skipPartiallyExpanded Whether the partially expanded state, if the Gleam is large
 * enough, should be skipped. If true, the Gleam will always expand to the [Expanded] state and
 * move to the [Hidden] state if available when hiding the Gleam, either programmatically or by
 * user interaction.
 * @param initialValue The initial value of the state, representing the Gleam's initial
 * visibility.
 * @param density The density that this state can use to convert values to and from dp.
 * @param confirmValueChange Optional callback invoked to confirm or veto a pending Gleam state
 * change.
 * @param skipHiddenState Whether the hidden state should be skipped. If true, the Gleam will
 * always expand to the [Expanded] state and move to the [PartiallyExpanded] if available,
 * either programmatically or by user interaction.
 */
@Stable
@ExperimentalGleamApi
class GleamState @ExperimentalGleamApi constructor(
  internal val skipPartiallyExpanded: Boolean,
  internal var density: Density,
  initialValue: GleamValue = Hidden,
  confirmValueChange: (GleamValue) -> Boolean = { true },
  internal val skipHiddenState: Boolean = false,
) {

  init {
    if (skipPartiallyExpanded) {
      require(initialValue != PartiallyExpanded) {
        "The initial value must not be set to PartiallyExpanded if skipPartiallyExpanded " +
          "is set to true."
      }
    }
    if (skipHiddenState) {
      require(initialValue != Hidden) {
        "The initial value must not be set to Hidden if skipHiddenState is set to true."
      }
    }
  }

  /**
   * The current visibility state of the [Gleam].
   *
   * If no swipe or animation is in progress, this represents the Gleam's current visibility.
   * If a swipe or animation is ongoing, this reflects the state before the interaction started.
   */
  val currentValue: GleamValue get() = anchoredDraggableState.currentValue

  /**
   * The target visibility state of the [Gleam].
   *
   * If a swipe is in progress, this is the state the Gleam would transition to upon completion.
   * During an animation, this is the targeted end state of the animation. Otherwise, it matches
   * the [currentValue].
   */
  val targetValue: GleamValue get() = anchoredDraggableState.targetValue

  /**
   * Checks if the [Gleam] is currently visible.
   */
  val isVisible: Boolean
    get() = anchoredDraggableState.currentValue != Hidden

  /**
   * Retrieves the current offset (in pixels) of the [Gleam].
   *
   * The offset is calculated during the first layout pass of the provided [Gleam] content.
   *
   * **Composition phases:**
   *
   * 1. Composition { -> Effects }
   * 2. Layout { Measurement -> Placement }
   * 3. Drawing
   *
   * Accessing the offset within the first composition will throw an [IllegalStateException].
   * In subsequent compositions, the offset is derived from previous anchors.
   *
   * It's recommended to access the offset using a [LaunchedEffect], ensuring execution
   * after layout has completed in the next frame.
   *
   * @throws IllegalStateException if the offset is not yet initialized
   */
  fun requireOffset(): Float = anchoredDraggableState.requireOffset()

  /**
   * Checks if the [Gleam] has a defined [Expanded] state.
   */
  val hasExpandedState: Boolean
    get() = anchoredDraggableState.anchors.hasAnchorFor(Expanded)

  /**
   * Checks if the [Gleam] has a defined [PartiallyExpanded] state.
   */
  val hasPartiallyExpandedState: Boolean
    get() = anchoredDraggableState.anchors.hasAnchorFor(PartiallyExpanded)

  /**
   * Animates the [Gleam] to its [Expanded] state and suspends the coroutine until the animation
   * finishes or is cancelled.
   *
   * @throws CancellationException if the animation is interrupted
   */
  suspend fun expand() {
    anchoredDraggableState.animateTo(Expanded)
  }

  /**
   * Animates the [Gleam] to its [PartiallyExpanded] state and suspends the coroutine until
   * the animation finishes or is cancelled.
   *
   * @throws CancellationException if the animation is interrupted
   * @throws IllegalStateException if [skipPartiallyExpanded] is set to true
   */
  suspend fun partialExpand() {
    check(!skipPartiallyExpanded) {
      "Attempted to animate to partially expanded when skipPartiallyExpanded was enabled. Set" +
        " skipPartiallyExpanded to false to use this function."
    }
    animateTo(PartiallyExpanded)
  }

  /**
   * Animates the [Gleam] to either its [PartiallyExpanded] state (if defined) or the [Expanded]
   * state, and suspends the coroutine until the animation finishes or is cancelled.
   *
   * @throws CancellationException if the animation is interrupted
   */
  suspend fun show() {
    val targetValue = when {
      hasPartiallyExpandedState -> PartiallyExpanded
      else -> Expanded
    }
    animateTo(targetValue)
  }

  /**
   * Hides the [Gleam] with animation and suspends the coroutine until it is fully hidden or
   * animation has been cancelled.
   *
   * @throws CancellationException if the animation is interrupted
   */
  suspend fun hide() {
    check(!skipHiddenState) {
      "Attempted to animate to hidden when skipHiddenState was enabled. Set skipHiddenState" +
        " to false to use this function."
    }
    animateTo(Hidden)
  }

  /**
   * **Forcibly** hides the [Gleam] without animation and suspends the coroutine until it is
   * fully hidden. This function differs from [hide] by ignoring any ongoing user interactions
   * like swipes or taps, ensuring the [Gleam] is hidden immediately.
   *
   * Use this function when you need to unconditionally hide the [Gleam], for example, in error
   * scenarios or when integrating with navigation.
   *
   * The animation will follow the default animation settings specified in [GleamState].
   *
   * @throws CancellationException if the interaction is interrupted by another interaction
   * like a gesture interaction or another programmatic interaction like [animateTo] or
   * [snapTo].
   */
  suspend fun forcedHide() {
    snapTo(Hidden)
  }

  /**
   * Delegates the confirmation of a proposed value change to the underlying
   * [anchoredDraggableState]. This allows for centralized control over value change logic,
   * including potential vetoing of changes.
   *
   * @param newValue The proposed new value for the state.
   * @return True if the value change is confirmed, false otherwise.
   */
  fun confirmValueChange(newValue: GleamValue) = anchoredDraggableState.confirmValueChange(newValue)

  /**
   * Animate to a [targetValue].
   * If the [targetValue] is not in the set of anchors, the [currentValue] will be updated to
   * the [targetValue] without updating the offset.
   *
   * @throws CancellationException if the interaction interrupted by another interaction like a
   * gesture interaction or another programmatic interaction like a [animateTo] or [snapTo] call.
   *
   * @param targetValue The target value of the animation
   */
  internal suspend fun animateTo(
    targetValue: GleamValue,
    velocity: Float = anchoredDraggableState.lastVelocity,
  ) {
    anchoredDraggableState.animateTo(targetValue, velocity)
  }

  /**
   * Snap to a [targetValue] without any animation.
   *
   * @throws CancellationException if the interaction interrupted by another interaction like
   * a gesture interaction or another programmatic interaction like a [animateTo] or [snapTo]
   * call.
   *
   * @param targetValue The target value of the animation
   */
  internal suspend fun snapTo(targetValue: GleamValue) {
    anchoredDraggableState.snapTo(targetValue)
  }

  /**
   * Find the closest anchor taking into account the velocity and settle at it with an
   * animation.
   */
  internal suspend fun settle(velocity: Float) {
    anchoredDraggableState.settle(velocity)
  }

  /**
   * Internal instance of [AnchoredDraggableState] that manages the core draggable and
   * animatable state of the gleam. It handles animations, value changes, and user
   * interactions.
   */
  internal var anchoredDraggableState = AnchoredDraggableState(
    initialValue = initialValue,
    animationSpec = AnchoredDraggableDefaults.AnimationSpec,
    confirmValueChange = confirmValueChange,
    positionalThreshold = { with(density) { 56.dp.toPx() } },
    velocityThreshold = { with(density) { 125.dp.toPx() } },
  )

  /**
   * The current offset of the gleam in pixels. This offset is calculated and managed by
   * the [anchoredDraggableState] based on the current state and user interactions.
   */
  internal val offset: Float get() = anchoredDraggableState.offset

  companion object {
    /**
     * The default [Saver] implementation for [GleamState].
     */
    fun Saver(
      skipPartiallyExpanded: Boolean,
      confirmValueChange: (GleamValue) -> Boolean,
      density: Density,
    ) = Saver<GleamState, GleamValue>(
      save = { it.currentValue },
      restore = { savedValue ->
        GleamState(skipPartiallyExpanded, density, savedValue, confirmValueChange)
      },
    )
  }
}

/**
 * Possible values of [GleamState].
 */
@ExperimentalGleamApi
enum class GleamValue {
  /**
   * The Gleam is not visible.
   */
  Hidden,

  /**
   * The Gleam is visible at full height.
   */
  Expanded,

  /**
   * The Gleam is partially visible.
   */
  PartiallyExpanded,
}

/**
 * Contains the default values used by [Gleam] and [GleamScaffold].
 */
@Stable
@ExperimentalGleamApi
object GleamDefaults {
  /** The default shape for Gleams in a [Hidden] state. */
  val HiddenShape: Shape
    @Composable get() = GleamTokens.DockedMinimizedContainerShape.value

  /** The default shape for a Gleam in [PartiallyExpanded] and [Expanded] states. */
  val ExpandedShape: Shape
    @Composable get() = GleamTokens.DockedContainerShape.value

  /** The default container color for a Gleam. */
  val ContainerColor: Color
    @Composable get() = GleamTokens.DockedContainerColor.value

  /** The default elevation for a Gleam. */
  val Elevation = GleamTokens.DockedModalContainerElevation

  /** The default color of the scrim overlay for background content. */
  val ScrimColor: Color
    @Composable get() = ScrimTokens.ContainerColor.value.copy(ScrimTokens.ContainerOpacity)

  /**
   * The default peek height used by [GleamScaffold].
   */
  val GleamPeekHeight = 56.dp

  /**
   * The default max width used by [Gleam] and [GleamScaffold]
   */
  val GleamMaxWidth = Dp.Unspecified

  /**
   * Default insets to be used and consumed by the [Gleam] window.
   */
  val windowInsets: WindowInsets
    @Composable
    get() = WindowInsets.verticalSystemBarsForVisuals

  /**
   * The optional visual marker placed on top of a Gleam to indicate it may be dragged.
   */
  @Composable
  fun DragHandle(
    modifier: Modifier = Modifier,
    width: Dp = GleamTokens.DockedDragHandleWidth,
    height: Dp = GleamTokens.DockedDragHandleHeight,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = GleamTokens.DockedDragHandleColor.value.copy(
      GleamTokens.DockedDragHandleOpacity,
    ),
  ) {
    val dragHandleDescription = "getString(Strings.GleamDragHandleDescription)"
    Surface(
      modifier = modifier
        .padding(vertical = DragHandleVerticalPadding)
        .semantics { contentDescription = dragHandleDescription },
      color = color,
      shape = shape,
    ) {
      Box(
        Modifier
          .size(
            width = width,
            height = height,
          ),
      )
    }
  }

  /**
   * Properties used to customize the behavior of a [Gleam].
   */
  fun properties(
    securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    isFocusable: Boolean = true,
    shouldDismissOnBackPress: Boolean = true,
    animateCorners: Boolean = false,
    animateHorizontalEdge: Boolean = false,
    maxHorizontalEdge: Dp = 0.dp,
  ) = GleamProperties(
    securePolicy = securePolicy,
    isFocusable = isFocusable,
    shouldDismissOnBackPress = shouldDismissOnBackPress,
    animateCorners = animateCorners,
    animateHorizontalEdge = animateHorizontalEdge,
    maxHorizontalEdge = maxHorizontalEdge,
  )
}

@ExperimentalGleamApi
internal fun ConsumeSwipeWithinGleamBoundsNestedScrollConnection(
  gleamState: GleamState,
  orientation: Orientation,
  onFling: (velocity: Float) -> Unit,
): NestedScrollConnection = object : NestedScrollConnection {
  override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
    val delta = available.toFloat()
    return if (delta < 0 && source == NestedScrollSource.Drag) {
      gleamState.anchoredDraggableState.dispatchRawDelta(delta).toOffset()
    } else {
      Offset.Zero
    }
  }

  override fun onPostScroll(
    consumed: Offset,
    available: Offset,
    source: NestedScrollSource,
  ): Offset {
    return if (source == NestedScrollSource.Drag) {
      gleamState.anchoredDraggableState.dispatchRawDelta(available.toFloat()).toOffset()
    } else {
      Offset.Zero
    }
  }

  override suspend fun onPreFling(available: Velocity): Velocity {
    val toFling = available.toFloat()
    val currentOffset = gleamState.requireOffset()
    val minAnchor = gleamState.anchoredDraggableState.anchors.minAnchor()
    return if (toFling < 0 && currentOffset > minAnchor) {
      onFling(toFling)
      // since we go to the anchor with tween settling, consume all for the best UX
      available
    } else {
      Velocity.Zero
    }
  }

  override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
    onFling(available.toFloat())
    return available
  }

  private fun Float.toOffset(): Offset = Offset(
    x = if (orientation == Orientation.Horizontal) this else 0f,
    y = if (orientation == Orientation.Vertical) this else 0f,
  )

  @JvmName("velocityToFloat")
  private fun Velocity.toFloat() = if (orientation == Orientation.Horizontal) x else y

  @JvmName("offsetToFloat")
  private fun Offset.toFloat(): Float = if (orientation == Orientation.Horizontal) x else y
}

@Composable
@ExperimentalGleamApi
internal fun rememberGleamState(
  skipPartiallyExpanded: Boolean = false,
  confirmValueChange: (GleamValue) -> Boolean = { true },
  initialValue: GleamValue = Hidden,
  skipHiddenState: Boolean = false,
): GleamState {
  val density = LocalDensity.current
  return rememberSaveable(
    skipPartiallyExpanded,
    confirmValueChange,
    saver = GleamState.Saver(
      skipPartiallyExpanded = skipPartiallyExpanded,
      confirmValueChange = confirmValueChange,
      density = density,
    ),
  ) {
    GleamState(
      skipPartiallyExpanded,
      density,
      initialValue,
      confirmValueChange,
      skipHiddenState,
    )
  }
}

private val DragHandleVerticalPadding = 22.dp
