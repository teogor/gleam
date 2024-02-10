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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewRootForInspector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.dismiss
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.popup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.teogor.gleam.GleamValue.Expanded
import dev.teogor.gleam.GleamValue.Hidden
import dev.teogor.gleam.GleamValue.PartiallyExpanded
import dev.teogor.gleam.utils.toDp
import dev.teogor.gleam.utils.top
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.max

/**
 * [Gleam]s are overlays that slide up from the bottom of the screen, offering an alternative
 * to inline menus or simple dialogs on mobile. They're ideal for presenting long lists of
 * actions, detailed information with icons, or confirmations while temporarily disabling
 * other app content. [Gleam]s remain on screen until dismissed, confirmed, or a required
 * action is taken.
 *
 * @param onDismissRequest Executes when the user clicks outside of the [Gleam], after it
 * animates to [Hidden].
 * @param modifier Optional [Modifier] for the [Gleam].
 * @param gleamState The state object managing the [Gleam]'s visibility and animation. Use
 * this to programmatically control the [Gleam]'s behavior.
 * @param gleamMaxWidth [Dp] defining the maximum width of the [Gleam]. Use [Dp.Unspecified]
 * for full screen width.
 * @param shape The shape of the [Gleam]'s container.
 * @param containerColor The background color of the [Gleam]'s container.
 * @param contentColor The preferred color for content inside the [Gleam]. Defaults to the
 * appropriate color based on the [containerColor] or the current theme.
 * @param tonalElevation The tonal elevation of the [Gleam], defining its shadow level.
 * @param scrimColor The color of the scrim that overlays the background content when
 * the [Gleam] is open.
 * @param dragHandle An optional visual marker to allow users to swipe the [Gleam] up and
 * down.
 * @param windowInsets Insets to be applied to the [Gleam]'s window for proper layout and
 * positioning.
 * @param properties Additional customization options for the Gleam's behavior through
 * [GleamProperties].
 * @param content The content to be displayed inside the Gleam.
 *
 * @see GleamState
 * @see GleamProperties
 */
@Composable
@ExperimentalGleamApi
fun Gleam(
  onDismissRequest: () -> Unit,
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
  content: @Composable ColumnScope.() -> Unit,
) {
  val density = LocalDensity.current
  SideEffect {
    gleamState.density = density
  }
  val scope = rememberCoroutineScope()
  val animateToDismiss: () -> Unit = {
    if (gleamState.anchoredDraggableState.confirmValueChange(Hidden)) {
      scope.launch { gleamState.hide() }.invokeOnCompletion {
        if (!gleamState.isVisible) {
          onDismissRequest()
        }
      }
    }
  }
  val settleToDismiss: (velocity: Float) -> Unit = {
    scope.launch { gleamState.settle(it) }.invokeOnCompletion {
      if (!gleamState.isVisible) onDismissRequest()
    }
  }

  GleamPopup(
    properties = properties,
    onDismissRequest = {
      if (gleamState.currentValue == Expanded && gleamState.hasPartiallyExpandedState) {
        scope.launch { gleamState.partialExpand() }
      } else { // Is expanded without collapsed state or is collapsed.
        scope.launch { gleamState.hide() }.invokeOnCompletion { onDismissRequest() }
      }
    },
    windowInsets = windowInsets,
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      propagateMinConstraints = false,
    ) {
      // region TODO: Handle customization
      //         perhaps
      val animateCorners = properties.animateCorners
      val animateHorizontalMargins = properties.animateHorizontalEdge
      val topAnimationOffset = 120.dp
      // todo better way to achieve this
      val position = if (gleamState.anchoredDraggableState.offset.isNaN()) {
        0f
      } else {
        gleamState.anchoredDraggableState.requireOffset()
      }
      val offsetDp = (position / LocalDensity.current.density).dp

      val topAnimationOffsetPx = with(LocalDensity.current) { topAnimationOffset.toPx() }
      val maxHorizontalEdge = if (!animateHorizontalMargins) {
        properties.maxHorizontalEdge
      } else {
        if (position <= topAnimationOffsetPx) {
          properties.maxHorizontalEdge.value - properties.maxHorizontalEdge.value * (1 - position / topAnimationOffsetPx)
        } else {
          properties.maxHorizontalEdge.value
        }.coerceAtLeast(0f).dp
      }

      // Animated Shape
      val animatedShape = if (animateCorners) {
        val roundedCornersInterpolation = position.div(topAnimationOffsetPx).coerceIn(0f, 1f)
        if (shape is CornerBasedShape) {
          shape.top().copy(
            topStart = CornerSize(shape.topStart.toDp().times(roundedCornersInterpolation)),
            topEnd = CornerSize(shape.topEnd.toDp().times(roundedCornersInterpolation)),
          )
        } else {
          shape
        }
      } else {
        shape
      }
      // endregion

      Scrim(
        color = scrimColor,
        onDismissRequest = animateToDismiss,
        onForcedDismissRequest = {
          scope.launch {
            gleamState.snapTo(PartiallyExpanded)
            animateToDismiss()
          }
        },
        gleamState = gleamState,
        visible = gleamState.targetValue != Hidden,
      )
      val gleamPanelTitle = "Gleam"
      Surface(
        modifier = modifier
          .widthIn(max = gleamMaxWidth)
          .fillMaxWidth()
          .padding(horizontal = maxHorizontalEdge)
          .align(Alignment.TopCenter)
          .semantics { paneTitle = gleamPanelTitle }
          .nestedScroll(
            remember(gleamState) {
              ConsumeSwipeWithinGleamBoundsNestedScrollConnection(
                gleamState = gleamState,
                orientation = Orientation.Vertical,
                onFling = settleToDismiss,
              )
            },
          )
          .draggableAnchors(
            gleamState.anchoredDraggableState,
            Orientation.Vertical,
          ) { gleamSize, constraints ->
            val fullHeight = constraints.maxHeight.toFloat()
            val newAnchors = DraggableAnchors {
              Hidden at fullHeight
              if (gleamSize.height > (fullHeight / 2) &&
                !gleamState.skipPartiallyExpanded
              ) {
                PartiallyExpanded at fullHeight / 2f
              }
              if (gleamSize.height != 0) {
                Expanded at max(0f, fullHeight - gleamSize.height)
              }
            }
            val newTarget = when (gleamState.anchoredDraggableState.targetValue) {
              Hidden -> Hidden
              PartiallyExpanded, Expanded -> {
                val hasPartiallyExpandedState = newAnchors
                  .hasAnchorFor(PartiallyExpanded)
                val newTarget = if (hasPartiallyExpandedState) {
                  PartiallyExpanded
                } else if (newAnchors.hasAnchorFor(Expanded)) Expanded else Hidden
                newTarget
              }
            }
            return@draggableAnchors newAnchors to newTarget
          }
          .draggable(
            state = gleamState.anchoredDraggableState.draggableState,
            orientation = Orientation.Vertical,
            enabled = gleamState.isVisible,
            startDragImmediately = gleamState.anchoredDraggableState.isAnimationRunning,
            onDragStopped = { settleToDismiss(it) },
          ),
        shape = animatedShape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
      ) {
        val topPaddingWI = windowInsets.asPaddingValues(LocalDensity.current).calculateTopPadding()
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val drawUnderStatusBar = remember(topPaddingWI, statusBarHeight) {
          topPaddingWI != statusBarHeight
        }
        Column(Modifier.fillMaxWidth()) {
          if (drawUnderStatusBar) {
            val targetHeight = if (offsetDp < statusBarHeight) {
              val offsetPercentage = 1f - offsetDp.div(statusBarHeight)
              statusBarHeight * offsetPercentage
            } else {
              0.dp
            }
            Spacer(
              modifier = Modifier
                .fillMaxWidth()
                .height(targetHeight),
            )
          }

          if (dragHandle != null) {
            val collapseActionLabel = "Collapse"
            val dismissActionLabel = "Dismiss"
            val expandActionLabel = "Expand"
            Box(
              Modifier
                .align(Alignment.CenterHorizontally)
                .semantics(mergeDescendants = true) {
                  // Provides semantics to interact with the gleam based on its
                  // current value.
                  with(gleamState) {
                    dismiss(dismissActionLabel) {
                      animateToDismiss()
                      true
                    }
                    if (currentValue == PartiallyExpanded) {
                      expand(expandActionLabel) {
                        if (anchoredDraggableState.confirmValueChange(
                            Expanded,
                          )
                        ) {
                          scope.launch { gleamState.expand() }
                        }
                        true
                      }
                    } else if (hasPartiallyExpandedState) {
                      collapse(collapseActionLabel) {
                        if (anchoredDraggableState.confirmValueChange(
                            PartiallyExpanded,
                          )
                        ) {
                          scope.launch { partialExpand() }
                        }
                        true
                      }
                    }
                  }
                },
            ) {
              dragHandle()
            }
          }
          content()
        }
      }
    }
  }
  if (gleamState.hasExpandedState) {
    LaunchedEffect(gleamState) {
      gleamState.show()
    }
  }
}

/**
 * Properties used to customize the behavior of a [Gleam].
 *
 * @param securePolicy Policy for setting [WindowManager.LayoutParams.FLAG_SECURE] on the
 * [Gleam]'s window. This flag restricts screenshots and screen recordings while the [Gleam]
 * is open. Use with caution as it can impact user experience.
 *
 * @param isFocusable Whether the [Gleam] is focusable. When `true`, it receives IME events
 * and key presses, like the back button. Set to `false` to prevent accidental interactions.
 *
 * @param shouldDismissOnBackPress Whether pressing the back button dismisses the [Gleam].
 * Requires [isFocusable] to be `true` to have effect. Use with caution as it can override
 * expected back button behavior.
 *
 * @param animateCorners Controls whether the [Gleam]'s corners animate during opening and
 * closing. Setting to `false` can improve performance but might look less visually appealing.
 *
 * @param animateHorizontalEdge Controls whether the Gleam's horizontal edge animates during
 * opening and closing. Setting to `false` can improve performance but might look less
 * visually appealing.
 *
 * @param maxHorizontalEdge Defines the maximum horizontal edge offset for animations. Use
 * [Dp.Unspecified] for full-width animation.
 */
@ExperimentalGleamApi
class GleamProperties(
  val securePolicy: SecureFlagPolicy,
  val isFocusable: Boolean,
  val shouldDismissOnBackPress: Boolean,
  val animateCorners: Boolean,
  val animateHorizontalEdge: Boolean,
  val maxHorizontalEdge: Dp,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GleamProperties) return false

    if (securePolicy != other.securePolicy) return false
    if (isFocusable != other.isFocusable) return false
    return shouldDismissOnBackPress == other.shouldDismissOnBackPress
  }

  override fun hashCode(): Int {
    var result = securePolicy.hashCode()
    result = 31 * result + isFocusable.hashCode()
    result = 31 * result + shouldDismissOnBackPress.hashCode()
    return result
  }
}

/**
 * Creates and remembers a [GleamState] object for managing the visibility and animation
 * of a [Gleam]. This function is equivalent to calling `remember { GleamState(...) }`.
 *
 * @param skipPartiallyExpanded (Optional) Controls whether to skip the [PartiallyExpanded]
 * state. When `true`, the [Gleam] always expands fully while opening and hides directly
 * when closing, skipping the intermediate state where it's partially visible based on
 * screen height.
 *
 * @param confirmValueChange (Optional) Callback triggered when the [Gleam]'s state is about
 * to change. This allows you to control or prevent state transitions based on custom logic.
 * The callback receives the new desired state ([GleamValue]) and returns `true` to allow the
 * change or `false` to block it.
 *
 * @return A new or previously remembered [GleamState] object.
 */
@Composable
@ExperimentalGleamApi
fun rememberGleamState(
  skipPartiallyExpanded: Boolean = false,
  confirmValueChange: (GleamValue) -> Boolean = { true },
) = rememberGleamState(
  skipPartiallyExpanded,
  confirmValueChange,
  Hidden,
)

@ExperimentalGleamApi
@Composable
private fun Scrim(
  color: Color,
  onDismissRequest: () -> Unit,
  onForcedDismissRequest: () -> Unit,
  gleamState: GleamState,
  visible: Boolean,
) {
  val isVisible = remember(gleamState.isVisible) { gleamState.isVisible }
  val isBecomingVisible = remember(isVisible, gleamState.targetValue) {
    !isVisible && gleamState.targetValue != Hidden
  }
  if (color.isSpecified) {
    val alpha by animateFloatAsState(
      targetValue = if (visible) 1f else 0f,
      animationSpec = TweenSpec(),
    )
    val dismissGleam = if (visible) {
      Modifier
        .pointerInput(onDismissRequest) {
          detectTapGestures {
            if (!isBecomingVisible) {
              onDismissRequest()
            } else {
              onForcedDismissRequest()
            }
          }
        }
        .clearAndSetSemantics {}
    } else {
      Modifier
    }
    Canvas(
      Modifier
        .fillMaxSize()
        .then(dismissGleam),
    ) {
      drawRect(color = color, alpha = alpha)
    }
  }
}

/**
 * Popup specific for [Gleam].
 */
@OptIn(ExperimentalGleamApi::class)
@Composable
internal fun GleamPopup(
  properties: GleamProperties,
  onDismissRequest: () -> Unit,
  windowInsets: WindowInsets,
  content: @Composable () -> Unit,
) {
  val view = LocalView.current
  val id = rememberSaveable { UUID.randomUUID() }
  val parentComposition = rememberCompositionContext()
  val currentContent by rememberUpdatedState(content)
  val layoutDirection = LocalLayoutDirection.current
  val gleamWindow = remember {
    GleamWindow(
      properties = properties,
      onDismissRequest = onDismissRequest,
      composeView = view,
      saveId = id,
    ).apply {
      setCustomContent(
        parent = parentComposition,
        content = {
          Box(
            Modifier
              .semantics { this.popup() }
              .windowInsetsPadding(windowInsets)
              .then(
                // TODO(b/290893168): Figure out a solution for APIs < 30.
                if (Build.VERSION.SDK_INT >= 33) {
                  Modifier.imePadding()
                } else {
                  Modifier
                },
              ),
          ) {
            currentContent()
          }
        },
      )
    }
  }

  DisposableEffect(gleamWindow) {
    gleamWindow.show()
    gleamWindow.superSetLayoutDirection(layoutDirection)
    onDispose {
      gleamWindow.disposeComposition()
      gleamWindow.dismiss()
    }
  }
}

/** Custom compose view for [Gleam] */
@SuppressLint("ViewConstructor")
@OptIn(ExperimentalGleamApi::class)
private class GleamWindow(
  private val properties: GleamProperties,
  private var onDismissRequest: () -> Unit,
  private val composeView: View,
  saveId: UUID,
) :
  AbstractComposeView(composeView.context),
  ViewTreeObserver.OnGlobalLayoutListener,
  ViewRootForInspector {

  private var backCallback: Any? = null

  init {
    id = android.R.id.content
    // Set up view owners
    setViewTreeLifecycleOwner(composeView.findViewTreeLifecycleOwner())
    setViewTreeViewModelStoreOwner(composeView.findViewTreeViewModelStoreOwner())
    setViewTreeSavedStateRegistryOwner(composeView.findViewTreeSavedStateRegistryOwner())
    setTag(androidx.compose.ui.R.id.compose_view_saveable_id_tag, "Popup:$saveId")
    // Enable children to draw their shadow by not clipping them
    clipChildren = false
  }

  private val windowManager = composeView.context.getSystemService(
    Context.WINDOW_SERVICE,
  ) as WindowManager

  private val displayWidth: Int
    get() = context.resources.displayMetrics.widthPixels

  private val params: WindowManager.LayoutParams =
    WindowManager.LayoutParams().apply {
      // Position gleam from the bottom of the screen
      gravity = Gravity.BOTTOM or Gravity.START
      // Application panel window
      type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
      // Fill up the entire app view
      width = displayWidth
      height = WindowManager.LayoutParams.MATCH_PARENT

      // Format of screen pixels
      format = PixelFormat.TRANSLUCENT
      // Title used as fallback for a11y services
      // TODO: Provide bottom sheet window resource
      title = composeView.context.resources.getString(
        androidx.compose.ui.R.string.default_popup_window_title,
      )
      // Get the Window token from the parent view
      token = composeView.applicationWindowToken

      // Flags specific to gleam.
      flags = flags and (
        WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES or
          WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        ).inv()

      flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

      // Security flag
      val secureFlagEnabled =
        properties.securePolicy.shouldApplySecureFlag(composeView.isFlagSecureEnabled())
      if (secureFlagEnabled) {
        flags = flags or WindowManager.LayoutParams.FLAG_SECURE
      } else {
        flags = flags and (WindowManager.LayoutParams.FLAG_SECURE.inv())
      }

      // Focusable
      if (!properties.isFocusable) {
        flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
      } else {
        flags = flags and (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv())
      }
    }

  private var content: @Composable () -> Unit by mutableStateOf({})

  override var shouldCreateCompositionOnAttachedToWindow: Boolean = false
    private set

  @Composable
  override fun Content() {
    content()
  }

  fun setCustomContent(
    parent: CompositionContext? = null,
    content: @Composable () -> Unit,
  ) {
    parent?.let { setParentCompositionContext(it) }
    this.content = content
    shouldCreateCompositionOnAttachedToWindow = true
  }

  fun show() {
    windowManager.addView(this, params)
  }

  fun dismiss() {
    setViewTreeLifecycleOwner(null)
    setViewTreeSavedStateRegistryOwner(null)
    composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    windowManager.removeViewImmediate(this)
  }

  /**
   * Taken from PopupWindow. Calls [onDismissRequest] when back button is pressed.
   */
  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    if (event.keyCode == KeyEvent.KEYCODE_BACK && properties.shouldDismissOnBackPress) {
      if (keyDispatcherState == null) {
        return super.dispatchKeyEvent(event)
      }
      if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
        val state = keyDispatcherState
        state?.startTracking(event, this)
        return true
      } else if (event.action == KeyEvent.ACTION_UP) {
        val state = keyDispatcherState
        if (state != null && state.isTracking(event) && !event.isCanceled) {
          onDismissRequest()
          return true
        }
      }
    }
    return super.dispatchKeyEvent(event)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()

    maybeRegisterBackCallback()
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()

    maybeUnregisterBackCallback()
  }

  private fun maybeRegisterBackCallback() {
    if (!properties.shouldDismissOnBackPress || Build.VERSION.SDK_INT < 33) {
      return
    }
    if (backCallback == null) {
      backCallback = Api33Impl.createBackCallback(onDismissRequest)
    }
    Api33Impl.maybeRegisterBackCallback(this, backCallback)
  }

  private fun maybeUnregisterBackCallback() {
    if (Build.VERSION.SDK_INT >= 33) {
      Api33Impl.maybeUnregisterBackCallback(this, backCallback)
    }
    backCallback = null
  }

  override fun onGlobalLayout() {
    // No-op
  }

  override fun setLayoutDirection(layoutDirection: Int) {
    // Do nothing. ViewRootImpl will call this method attempting to set the layout direction
    // from the context's locale, but we have one already from the parent composition.
  }

  // Sets the "real" layout direction for our content that we obtain from the parent composition.
  fun superSetLayoutDirection(layoutDirection: LayoutDirection) {
    val direction = when (layoutDirection) {
      LayoutDirection.Ltr -> android.util.LayoutDirection.LTR
      LayoutDirection.Rtl -> android.util.LayoutDirection.RTL
    }
    super.setLayoutDirection(direction)
  }

  @RequiresApi(33)
  private object Api33Impl {
    @JvmStatic
    @DoNotInline
    fun createBackCallback(onDismissRequest: () -> Unit) =
      OnBackInvokedCallback(onDismissRequest)

    @JvmStatic
    @DoNotInline
    fun maybeRegisterBackCallback(view: View, backCallback: Any?) {
      if (backCallback is OnBackInvokedCallback) {
        view.findOnBackInvokedDispatcher()?.registerOnBackInvokedCallback(
          OnBackInvokedDispatcher.PRIORITY_OVERLAY,
          backCallback,
        )
      }
    }

    @JvmStatic
    @DoNotInline
    fun maybeUnregisterBackCallback(view: View, backCallback: Any?) {
      if (backCallback is OnBackInvokedCallback) {
        view.findOnBackInvokedDispatcher()?.unregisterOnBackInvokedCallback(backCallback)
      }
    }
  }
}

// Taken from AndroidPopup.android.kt
private fun View.isFlagSecureEnabled(): Boolean {
  val windowParams = rootView.layoutParams as? WindowManager.LayoutParams
  if (windowParams != null) {
    return (windowParams.flags and WindowManager.LayoutParams.FLAG_SECURE) != 0
  }
  return false
}

// Taken from AndroidPopup.android.kt
private fun SecureFlagPolicy.shouldApplySecureFlag(isSecureFlagSetOnParent: Boolean): Boolean {
  return when (this) {
    SecureFlagPolicy.SecureOff -> false
    SecureFlagPolicy.SecureOn -> true
    SecureFlagPolicy.Inherit -> isSecureFlagSetOnParent
  }
}
