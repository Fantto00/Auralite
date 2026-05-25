package com.fantto.auralite.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.fantto.auralite.ui.icons.chat
import com.fantto.auralite.ui.icons.history
import com.fantto.auralite.ui.icons.settings

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Chat : Screen(
        route = "chat",
        title = "对话",
        icon = chat
    )

    data object History : Screen(
        route = "history",
        title = "历史",
        icon = history
    )

    data object Settings : Screen(
        route = "settings",
        title = "设置",
        icon = settings
    )
}