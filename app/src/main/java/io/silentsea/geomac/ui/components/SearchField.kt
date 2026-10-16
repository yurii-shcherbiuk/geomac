package io.silentsea.geomac.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import io.silentsea.geomac.R
import io.silentsea.geomac.domain.model.Mac

@Composable
fun SearchField(
    textFieldState: TextFieldState,
    searchBarState: SearchBarState,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBarDefaults.InputField(
        textFieldState = textFieldState,
        searchBarState = searchBarState,
        onSearch = { onSearch() },
        modifier = modifier,
        placeholder = { Text(stringResource(R.string.mac_search)) },
        leadingIcon = {
            Icon(painterResource(R.drawable.search_24px), contentDescription = null)
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = textFieldState.text.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(onClick = { textFieldState.clearText() }) {
                    Icon(
                        painterResource(R.drawable.cancel_24px),
                        contentDescription = stringResource(R.string.clear)
                    )
                }
            }
        },
        inputTransformation = MacInputTransformation,
        outputTransformation = MacOutputTransformation,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            autoCorrectEnabled = false
        )
    )
}

private val MacInputTransformation = InputTransformation {
    val text = toString()
    val sanitized = Mac.sanitize(text)

    if (sanitized != text) replace(0, length, sanitized)
}

private val MacOutputTransformation = OutputTransformation {
    for (index in (2 until length step 2).reversed()) {
        insert(index, if (index % Mac.HEX_LENGTH == 0) ", " else ":")
    }
}
