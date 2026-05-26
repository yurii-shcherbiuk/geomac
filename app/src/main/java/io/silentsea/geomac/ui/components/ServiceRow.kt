package io.silentsea.geomac.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.silentsea.geomac.R
import io.silentsea.geomac.data.db.entities.GeomacCoordinates
import io.silentsea.geomac.domain.entites.Services
import io.silentsea.geomac.domain.entites.resId
import io.silentsea.geomac.utils.coordinatesString
import io.silentsea.geomac.utils.copy
import io.silentsea.geomac.utils.showToast
import kotlinx.coroutines.launch

@Composable
fun ServiceRow(
    service: Services,
    coordinates: GeomacCoordinates?
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current

    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(service.resId),
            style = MaterialTheme.typography.titleMedium
        )

        coordinates?.let { coordinates ->
            val label = stringResource(R.string.coordinates, stringResource(service.resId))
            val text = stringResource(R.string.coordinates_copied, stringResource(service.resId))

            Text(
                text = coordinates.coordinatesString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clickable(
                    enabled = true,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    coroutineScope.launch {
                        clipboard.copy(
                            label = label,
                            text = coordinates.coordinatesString()
                        )

                        context.showToast(text = text)
                    }
                }
            )
        } ?: Text(
            text = stringResource(R.string.not_found),
            style = MaterialTheme.typography.labelLarge
        )
    }
}