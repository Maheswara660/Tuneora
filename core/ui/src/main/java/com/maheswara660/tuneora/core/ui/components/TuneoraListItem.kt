package com.maheswara660.tuneora.core.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.ui.theme.LocalApplicationPreferences

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TuneoraSegmentedListItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    isSegmented: Boolean = true,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
) {
    val preferences = LocalApplicationPreferences.current
    val cornerRadius = preferences.cornerRadius.dp
    
    val shape = RoundedCornerShape(
        topStart = if (isFirstItem) cornerRadius else 0.dp,
        topEnd = if (isFirstItem) cornerRadius else 0.dp,
        bottomStart = if (isLastItem) cornerRadius else 0.dp,
        bottomEnd = if (isLastItem) cornerRadius else 0.dp
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple().takeIf { interactionSource != null } ?: androidx.compose.foundation.LocalIndication.current
            ),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (isSegmented) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        } else {
            Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    ) {
        ListItem(
            headlineContent = content,
            overlineContent = overlineContent,
            supportingContent = supportingContent,
            leadingContent = leadingContent,
            trailingContent = trailingContent,
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun ListSectionTitle(
    modifier: Modifier = Modifier,
    text: String,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        top = 20.dp,
        bottom = 10.dp,
    ),
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Text(
        text = text,
        modifier = modifier.padding(contentPadding),
        color = color,
        style = MaterialTheme.typography.labelLarge,
    )
}
