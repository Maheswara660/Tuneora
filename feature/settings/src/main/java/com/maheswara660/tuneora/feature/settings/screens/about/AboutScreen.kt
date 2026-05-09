package com.maheswara660.tuneora.feature.settings.screens.about

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maheswara660.tuneora.core.common.extensions.appIcon
import com.maheswara660.tuneora.core.common.extensions.appVersion
import com.maheswara660.tuneora.core.ui.components.TuneoraTopAppBar
import com.maheswara660.tuneora.core.ui.components.TuneoraScreen
import com.maheswara660.tuneora.core.ui.designsystem.TuneoraIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onLibrariesClick: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    TuneoraScreen(
        topBar = {
            TuneoraTopAppBar(
                title = { Text("About") },
                
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = TuneoraIcons.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {

            AboutApp()
            
            Spacer(modifier = Modifier.height(32.dp))

            DeveloperCard()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Connect & Support",
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            AboutActionItem(
                icon = Icons.Rounded.Code,
                title = "GitHub Repository",
                description = "View the source code and contribute",
                isFirstItem = true,
                onClick = {
                    uriHandler.openUri("https://github.com/maheswara660/Tuneora")
                }
            )

            AboutActionItem(
                icon = Icons.Rounded.Favorite,
                title = "Support Development",
                description = "Donate to help me continue developing Tuneora",
                onClick = {
                    uriHandler.openUri("https://ko-fi.com/maheswara660")
                }
            )

            AboutActionItem(
                icon = Icons.Rounded.Description,
                title = "Open Source License",
                description = "View the project's license",
                onClick = {
                    uriHandler.openUri("https://github.com/maheswara660/Tuneora/blob/main/LICENSE")
                }
            )

            AboutActionItem(
                icon = TuneoraIcons.Library,
                title = "Libraries",
                description = "Third-party libraries used in Tuneora",
                isLastItem = true,
                onClick = onLibrariesClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "© 2026 Maheswara660\nMade with ❤️ for the community",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun AboutApp() {
    val context = LocalContext.current
    val appVersion = remember { context.appVersion() }
    val appIcon = remember { context.appIcon()?.asImageBitmap() }

    Column(
        modifier = Modifier
            .padding(vertical = 32.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
        } else {
            Image(
                painter = painterResource(id = context.applicationInfo.icon),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tuneora",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = "v$appVersion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun DeveloperCard() {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                uriHandler.openUri("https://github.com/maheswara660")
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Developed by Maheswara660",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Passionate Android Developer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun AboutActionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        shape = RoundedCornerShape(
            topStart = if (isFirstItem) 16.dp else 4.dp,
            topEnd = if (isFirstItem) 16.dp else 4.dp,
            bottomStart = if (isLastItem) 16.dp else 4.dp,
            bottomEnd = if (isLastItem) 16.dp else 4.dp,
        ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
