package dev.zacsweers.catchup.service

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import io.sweers.catchup.R
import io.sweers.catchup.service.api.CatchUpItem

@Stable
class ClickableItemState {
  val interactionSource = MutableInteractionSource()
  var enabled by mutableStateOf(true)
  var contentColor by mutableStateOf(Color.Unspecified)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableItem(
  lazyItems: LazyPagingItems<CatchUpItem>,
  item: CatchUpItem?,
  eventSink: (ServiceScreen.Event) -> Unit,
  modifier: Modifier = Modifier,
  clickableItemState: ClickableItemState = remember { ClickableItemState() },
  content: @Composable (CatchUpItem) -> Unit
) {
  if (item == null) {
    ErrorItem(text = "Item was null!", modifier = modifier, onRetryClick = lazyItems::retry)
  } else {
    if (clickableItemState.enabled) {
      val interactionSource = clickableItemState.interactionSource
      val isPressed by interactionSource.collectIsPressedAsState()
      val targetElevation =
        if (isPressed) {
          dimensionResource(R.dimen.touch_raise)
        } else {
          0.dp
        }
      val elevation by animateDpAsState(targetElevation)
      Surface(
        modifier = modifier,
        tonalElevation = elevation,
        shadowElevation = elevation,
        interactionSource = interactionSource,
        contentColor = clickableItemState.contentColor,
        onClick = { eventSink(ServiceScreen.Event.ItemClicked(item)) }
      ) {
        content(item)
      }
    } else {
      content(item)
    }
  }
}
