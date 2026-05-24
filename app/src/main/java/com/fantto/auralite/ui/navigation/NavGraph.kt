package com.fantto.auralite.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fantto.auralite.App
import com.fantto.auralite.di.ViewModelFactory
import com.fantto.auralite.ui.screen.chat.ChatScreen
import com.fantto.auralite.ui.screen.chat.ChatViewModel
import com.fantto.auralite.ui.screen.history.HistoryScreen
import com.fantto.auralite.ui.screen.settings.SettingsScreen
import com.fantto.auralite.ui.screen.settings.SettingsViewModel
// 导航图，定义了应用的导航结构
@Composable
fun AuraliteNavGraph() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as App
    val viewModelFactory = ViewModelFactory(app.appModule)

    val screens = listOf(
        Screen.Chat,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Chat.route) {
                val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)
                ChatScreen(viewModel = chatViewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}