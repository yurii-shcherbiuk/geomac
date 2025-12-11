package io.silentsea.geomac.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import io.silentsea.geomac.R

@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    onSearch: () -> Unit,
    onFocusChanged: (Boolean) -> Unit
) {
    OutlinedTextField(
        state = textFieldState,
        placeholder = {
            Text(
                text = stringResource(R.string.mac_search)
            )
        },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.search_24px),
                contentDescription = null
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = textFieldState.text.isNotEmpty(),
                enter = slideInHorizontally { it } + expandHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + shrinkHorizontally { it } + fadeOut()
            ) {
                IconButton(
                    onClick = {
                        textFieldState.clearText()
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.cancel_24px),
                        contentDescription = null
                    )
                }
            }
        },
        inputTransformation = InputTransformation {
            val filtered = toString().uppercase().filter { it.isDigit() || it in 'A'..'F' }

            if (filtered != toString()) replace(0, length, filtered)
        },
        outputTransformation = OutputTransformation {
            for (index in (0 until length step 2).reversed()) {
                if (index > 0) insert(index, if (index % 12 == 0) ", " else ":")
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Search
        ),
        onKeyboardAction = {
            onSearch()
        },
        shape = CircleShape,
        modifier = modifier.onFocusChanged { onFocusChanged(it.isFocused) }
    )
}