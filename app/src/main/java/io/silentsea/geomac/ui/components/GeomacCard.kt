package io.silentsea.geomac.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.silentsea.geomac.R
import io.silentsea.geomac.domain.model.Coordinates
import io.silentsea.geomac.domain.model.Geomac
import io.silentsea.geomac.domain.model.Service
import io.silentsea.geomac.domain.model.coordinatesString
import io.silentsea.geomac.domain.model.macString
import io.silentsea.geomac.ui.model.labelRes
import io.silentsea.geomac.utils.copy
import kotlinx.coroutines.launch

private val services = Service.entries

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GeomacCard(
    item: Geomac,
    isSearching: Boolean,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
    onOpenMap: (Service, Coordinates) -> Unit,
    onCopied: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(Unit) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    fun copy(label: String, text: String, message: String) {
        coroutineScope.launch {
            clipboard.copy(label, text)
            onCopied(message)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        gesturesEnabled = !isSearching,
        onDismiss = { onDelete() },
        backgroundContent = {
            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                DeleteBackground(isActive = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            val count = services.size + 1

            val macLabel = stringResource(R.string.mac)
            val macCopied = stringResource(R.string.mac_copied)

            SegmentedListItem(
                onClick = { copy(macLabel, item.mac.macString(), macCopied) },
                shapes = ListItemDefaults.segmentedShapes(index = 0, count = count),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                leadingContent = {
                    FoundBadge(found = item.locations.size, total = services.size)
                },
                supportingContent = {
                    Text(
                        text = if (isSearching) {
                            stringResource(R.string.searching)
                        } else {
                            stringResource(R.string.found_by, item.locations.size, services.size)
                        }
                    )
                },
                trailingContent = {
                    AnimatedContent(
                        targetState = isSearching,
                        transitionSpec = { (fadeIn() + scaleIn()) togetherWith (fadeOut() + scaleOut()) }
                    ) { searching ->
                        if (searching) {
                            LoadingIndicator(modifier = Modifier.size(48.dp))
                        } else {
                            FilledTonalIconButton(
                                onClick = onRefresh,
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painterResource(R.drawable.refresh_24px),
                                    contentDescription = stringResource(R.string.refresh)
                                )
                            }
                        }
                    }
                }
            ) {
                Text(
                    text = item.mac.macString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }

            services.forEachIndexed { index, service ->
                ServiceItem(
                    service = service,
                    coordinates = item.locations[service],
                    index = index + 1,
                    count = count,
                    onCopy = ::copy,
                    onOpenMap = onOpenMap
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ServiceItem(
    service: Service,
    coordinates: Coordinates?,
    index: Int,
    count: Int,
    onCopy: (label: String, text: String, message: String) -> Unit,
    onOpenMap: (Service, Coordinates) -> Unit
) {
    val name = stringResource(service.labelRes)
    val label = stringResource(R.string.coordinates, name)
    val message = stringResource(R.string.coordinates_copied, name)

    val colors = MaterialTheme.colorScheme

    SegmentedListItem(
        onClick = { coordinates?.let { onCopy(label, it.coordinatesString(), message) } },
        onLongClick = coordinates?.let { { onOpenMap(service, it) } },
        onLongClickLabel = stringResource(R.string.open_in_maps),
        enabled = coordinates != null,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = colors.surfaceContainer,
            disabledContainerColor = colors.surfaceContainer,
            disabledContentColor = colors.onSurface.copy(alpha = 0.7f),
            disabledLeadingContentColor = colors.onSurfaceVariant,
            disabledSupportingContentColor = colors.onSurfaceVariant.copy(alpha = 0.7f)
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialShapes.Cookie4Sided.toShape())
                    .background(if (coordinates != null) colors.secondaryContainer else colors.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (coordinates != null) colors.onSecondaryContainer else colors.onSurfaceVariant
                )
            }
        },
        supportingContent = {
            Text(
                text = coordinates?.coordinatesString() ?: stringResource(R.string.not_found),
                fontFamily = if (coordinates != null) FontFamily.Monospace else null
            )
        },
        trailingContent = coordinates?.let {
            {
                IconButton(onClick = { onOpenMap(service, it) }) {
                    Icon(
                        painterResource(R.drawable.map_24px),
                        contentDescription = stringResource(R.string.open_in_maps)
                    )
                }
            }
        }
    ) {
        Text(text = name)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FoundBadge(found: Int, total: Int) {
    val colors = MaterialTheme.colorScheme

    val container by animateColorAsState(if (found > 0) colors.primaryContainer else colors.surfaceContainerHighest)
    val content by animateColorAsState(if (found > 0) colors.onPrimaryContainer else colors.onSurfaceVariant)

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.found_ratio, found, total),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = content
        )
    }
}

@Composable
private fun DeleteBackground(isActive: Boolean) {
    val colors = MaterialTheme.colorScheme

    val container by animateColorAsState(if (isActive) colors.errorContainer else Color.Transparent)
    val content by animateColorAsState(if (isActive) colors.onErrorContainer else colors.onSurfaceVariant)

    val description = stringResource(R.string.delete)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(container)
            .padding(horizontal = 24.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            painterResource(R.drawable.delete_24px),
            contentDescription = null,
            tint = content
        )
    }
}
