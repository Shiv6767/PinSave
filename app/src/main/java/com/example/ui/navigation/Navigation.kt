package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val title: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    Home("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    Downloads("downloads", "Downloads", Icons.Outlined.Download, Icons.Filled.Download),
    History("history", "History", Icons.Outlined.History, Icons.Filled.History),
    Settings("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings),
    LinkAccount("link_account", "Link Account", Icons.Outlined.Settings, Icons.Filled.Settings)
}
