package com.maheswara660.tuneora.core.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : NavigationItem("home", "Home", Icons.Rounded.Home)
    object Library : NavigationItem("library", "Library", Icons.Rounded.LibraryMusic)
    object History : NavigationItem("history", "History", Icons.Rounded.History)
    object Settings : NavigationItem("settings", "Settings", Icons.Rounded.Settings)
    object Blacklist : NavigationItem("blacklist", "Blacklist", Icons.Rounded.Settings)

    companion object {
        val items = listOf(Home, Library, History, Settings)
    }
}

