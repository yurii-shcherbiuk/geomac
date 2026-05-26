package io.silentsea.geomac.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.silentsea.geomac.R
import io.silentsea.geomac.data.db.entities.GeomacItemWithCoordinates
import io.silentsea.geomac.domain.entites.Services
import io.silentsea.geomac.utils.copy
import io.silentsea.geomac.utils.macString
import io.silentsea.geomac.utils.showToast
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LazyItemScope.Card(
    item: GeomacItemWithCoordinates,
    isSearching: Boolean,
    isSwiped: Boolean,
    onDelete: () -> Unit,
    onUpdate: () -> Unit,
    onSwipe: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val density = LocalDensity.current

    val coroutineScope = rememberCoroutineScope()

    val anchoredDraggableState = remember {
        AnchoredDraggableState(
            initialValue = CardSwipeState.Settled,
            anchors = DraggableAnchors {
                CardSwipeState.Settled at 0f
                CardSwipeState.EndToStart at with(density) { -56.dp.toPx() }
            },
        )
    }

    val animatedTopEnd by animateDpAsState(
        targetValue = if (anchoredDraggableState.targetValue == CardSwipeState.Settled) 12.dp else 0.dp,
    )

    LaunchedEffect(isSearching, isSwiped) {
        if (isSearching || !isSwiped) {
            anchoredDraggableState.animateTo(CardSwipeState.Settled)
        }
    }

    LaunchedEffect(anchoredDraggableState.targetValue) {
        if (anchoredDraggableState.targetValue == CardSwipeState.EndToStart) {
            onSwipe()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .height(IntrinsicSize.Min)
            .animateItem()
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            onClick = onDelete,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painterResource(R.drawable.delete_24px),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .align(Alignment.End)
            )
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxSize()
                .offset {
                    IntOffset(
                        x = anchoredDraggableState
                            .requireOffset()
                            .roundToInt(),
                        y = 0,
                    )
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Horizontal,
                    enabled = !isSearching
                ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = animatedTopEnd,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = stringResource(R.string.mac)
                    val text = stringResource(R.string.mac_copied)

                    Text(
                        text = item.mac.macString(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.clickable(
                            enabled = true,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            coroutineScope.launch {
                                clipboard.copy(
                                    label = label,
                                    text = item.mac.macString()
                                )

                                context.showToast(text = text)
                            }
                        }
                    )

                    Crossfade(
                        targetState = isSearching
                    ) { isSearching ->
                        if (isSearching) {
                            LoadingIndicator()
                        } else {
                            IconButton(
                                onClick = onUpdate
                            ) {
                                Icon(
                                    painterResource(R.drawable.refresh_24px),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }

                for (service in Services.entries.sorted()) {
                    ServiceRow(
                        service = service,
                        coordinates = item.coordinates.firstOrNull { coordinates -> coordinates.service == service }
                    )

                    if (service != Services.entries.maxOrNull()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(
                                horizontal = 8.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

private enum class CardSwipeState {
    Settled,
    EndToStart
}