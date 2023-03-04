package io.leeonardoo.otptextfield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import io.leeonardoo.otptextfield.ui.theme.OtpTextFieldComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OtpTextFieldComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
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

        LabelSwitch(
            label = "Enabled",
            checked = enabled,
            onCheckedChange = {
                enabled = !enabled
            }
        )

        LabelSwitch(
            label = "Read-only",
            checked = readOnly,
            onCheckedChange = {
                readOnly = !readOnly
            }
        )

        LabelSwitch(
            label = "Error",
            checked = !errorMessage.isNullOrBlank(),
            onCheckedChange = {
                errorMessage = if (errorMessage.isNullOrBlank()) {
                    LoremIpsum(3).values.joinToString(" ")
                } else {
                    null
                }
            }
        )

        LabelSwitch(
            label = "Helper text",
            checked = !helperText.isNullOrBlank(),
            onCheckedChange = {
                helperText = if (helperText.isNullOrBlank()) {
                    LoremIpsum(3).values.joinToString(" ")
                } else {
                    null
                }
            }
        )

        LabelSwitch(
            label = "VisualTransformation",
            checked = visualTransformation != VisualTransformation.None,
            onCheckedChange = {
                visualTransformation =
                    if (visualTransformation == VisualTransformation.None) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    }
            }
        )
    }
}

@Composable
fun LabelSwitch(
    modifier: Modifier = Modifier,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Switch(enabled = enabled, checked = checked, onCheckedChange = onCheckedChange)

        Spacer(modifier = Modifier.width(16.dp))

        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    OtpTextFieldComposeTheme {
        Surface {
            MainContent()
        }
    }
}