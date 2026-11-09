package elovaire.music.droidbeauty.app.ui.screens.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import elovaire.music.droidbeauty.app.ui.theme.ElovaireRadii

@Composable
internal fun EditorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    @DrawableRes leadingIconResId: Int? = null,
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        label = { Text(label, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)) },
        placeholder = placeholder?.takeIf(String::isNotEmpty)?.let { text -> { Text(text) } },
        leadingIcon = leadingIconResId?.let { iconResId ->
            {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = colors.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        shape = RoundedCornerShape(ElovaireRadii.input),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            disabledContainerColor = colors.surfaceVariant,
            errorContainerColor = colors.surfaceVariant,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = colors.error,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurface.copy(alpha = 0.7f),
            cursorColor = colors.primary,
        ),
    )
}
