package com.fantto.auralite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Chat : Screen(
        route = "chat",
        title = "对话",
        icon = Icons.Default.Phone
    )

    data object History : Screen(
        route = "history",
        title = "历史",
        icon = Icons.Default.Menu
    )

    data object Settings : Screen(
        route = "settings",
        title = "设置",
        icon = Icons.Default.Settings
    )
}