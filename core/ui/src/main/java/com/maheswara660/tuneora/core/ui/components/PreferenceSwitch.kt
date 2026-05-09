package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceSwitch(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    isChecked: Boolean = false,
    enabled: Boolean = true,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: () -> Unit = {},
) {
    TuneoraSegmentedListItem(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        isFirstItem = isFirstItem,
        isLastItem = isLastItem,
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        supportingContent = description?.let {
            {
                Text(text = description)
            }
        },
        content = {
            Text(text = title)
        },
        trailingContent = {
            TuneoraSwitch(
                checked = isChecked,
                onCheckedChange = null,
                enabled = enabled
            )
        },
    )
}
