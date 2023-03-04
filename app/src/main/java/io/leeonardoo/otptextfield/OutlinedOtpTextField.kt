package io.leeonardoo.otptextfield

import androidx.compose.animation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import io.leeonardoo.otptextfield.ui.theme.OtpTextFieldComposeTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun OutlinedOtpTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
    onFilled: () -> Unit = {},
    errorMessage: String? = null,
    helperText: String? = null,
    helperTextColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    ),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    visualTransformation: VisualTransformation = PasswordVisualTransformation(),
    requestFocus: Boolean,
    clearFocusWhenFilled: Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val updatedOnValueChange by rememberUpdatedState(onValueChange)
    val updatedOnFilled by rememberUpdatedState(onFilled)

    val code by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(value.length)))
    }

    DisposableEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
        onDispose { }
    }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val customTextSelectionColors = TextSelectionColors(
                handleColor = Color.Transparent,
                backgroundColor = Color.Transparent,
            )

            CompositionLocalProvider(
                LocalTextToolbar provides EmptyTextToolbar,
                LocalTextSelectionColors provides customTextSelectionColors
            ) {
                BasicTextField(
                    modifier = Modifier
                        .focusRequester(focusRequester = focusRequester)
                        .fillMaxWidth(),
                    value = code,
                    onValueChange = {
                        if (!it.text.isDigitsOnly() || it.text.length > length)
                            return@BasicTextField

                        updatedOnValueChange(it.text)

                        if (it.text.length == length) {
                            keyboardController?.hide()
                            if (clearFocusWhenFilled) {
                                focusRequester.freeFocus()
                                focusManager.clearFocus()
                            }
                            updatedOnFilled()
                        }
                    },
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = textStyle,
                    enabled = enabled,
                    readOnly = readOnly,
                    decorationBox = {
                        OtpInputDecoration(
                            code = code.text,
                            length = length,
                            textStyle = textStyle,
                            enabled = enabled,
                            isError = !errorMessage.isNullOrBlank(),
                            visualTransformation = visualTransformation
                        )
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (!errorMessage.isNullOrBlank()) 1f else 0f)
            )

            if (helperText != null && errorMessage.isNullOrBlank()) {
                Text(
                    text = helperText,
                    textAlign = TextAlign.Center,
                    color = helperTextColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OtpInputDecoration(
    modifier: Modifier = Modifier,
    code: String,
    length: Int,
    textStyle: TextStyle,
    enabled: Boolean,
    isError: Boolean,
    visualTransformation: VisualTransformation
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (i in 0 until length) {
                val text = if (i < code.length) code[i].toString() else ""
                OtpEntry(
                    modifier = Modifier.weight(1f, fill = false),
                    text = text,
                    textStyle = textStyle,
                    enabled = enabled,
                    isError = isError,
                    visualTransformation = visualTransformation
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun OtpEntry(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle,
    enabled: Boolean,
    isError: Boolean,
    visualTransformation: VisualTransformation
) {
    val transformedText = remember(text, visualTransformation) {
        visualTransformation.filter(AnnotatedString(text))
    }.text.text

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.disabled)
            transformedText.isNotEmpty() -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ContentAlpha.medium)
        },
        label = "textColor"
    )

    Box(
        modifier = modifier
            .width(42.dp)
            .height(48.dp)
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        val textColor by animateColorAsState(
            targetValue = when {
                isError -> MaterialTheme.colorScheme.error
                !enabled -> textStyle.color.copy(alpha = ContentAlpha.disabled)
                else -> textStyle.color
            },
            label = "textColor"
        )

        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = transformedText,
            transitionSpec = {
                ContentTransform(
                    targetContentEnter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    initialContentExit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                    sizeTransform = null
                )
            },
            contentAlignment = Alignment.Center,
            label = "textVisibility"
        ) { text ->
            if (text.isNotBlank()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = text,
                    color = textColor,
                    style = textStyle
                )
            }
        }
    }
}

private object EmptyTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun hide() {}

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?,
    ) {
    }
}

@Preview
@Composable
private fun OutlinedOtpTextFieldPreview() {
    OtpTextFieldComposeTheme {
        Surface {
            Column {
                var text by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var helperText by remember { mutableStateOf<String?>(null) }
                var enabled by remember { mutableStateOf(true) }
                var readOnly by remember { mutableStateOf(false) }
                var visualTransformation by remember { mutableStateOf(VisualTransformation.None) }

                OutlinedOtpTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    onValueChange = { text = it },
                    errorMessage = errorMessage,
                    helperText = helperText,
                    enabled = enabled,
                    readOnly = readOnly,
                    visualTransformation = visualTransformation,
                    requestFocus = true,
                    clearFocusWhenFilled = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        errorMessage = if (errorMessage.isNullOrBlank()) {
                            LoremIpsum(3).values.joinToString(" ")
                        } else {
                            null
                        }
                    }
                ) {
                    Text(text = "Toggle error")
                }

                Button(
                    onClick = {
                        helperText = if (helperText.isNullOrBlank()) {
                            LoremIpsum(3).values.joinToString(" ")
                        } else {
                            null
                        }
                    }
                ) {
                    Text(text = "Toggle helper text")
                }

                Button(onClick = { enabled = !enabled }) {
                    Text(text = "Toggle enabled")
                }

                Button(onClick = { readOnly = !readOnly }) {
                    Text(text = "Toggle read-only")
                }

                Button(
                    onClick = {
                        visualTransformation =
                            if (visualTransformation == VisualTransformation.None) {
                                PasswordVisualTransformation()
                            } else {
                                VisualTransformation.None
                            }
                    }
                ) {
                    Text(text = "Toggle visualTransformation")
                }
            }
        }
    }
}
