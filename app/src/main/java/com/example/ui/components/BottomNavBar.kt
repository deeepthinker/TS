package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.TextGray

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Scanner : BottomNavItem("scanner", "SCANNER", Icons.Default.Camera)
    object History : BottomNavItem("history", "HISTORY", Icons.Default.History)
    object Analytics : BottomNavItem("analytics", "ANALYTICS", Icons.Default.Analytics)
    object Settings : BottomNavItem("settings", "SETTINGS", Icons.Default.Settings)
    object About : BottomNavItem("about", "ABOUT", Icons.Default.Info)
}

val bottomNavItems = listOf(
    BottomNavItem.Scanner,
    BottomNavItem.History,
    BottomNavItem.Analytics,
    BottomNavItem.Settings,
    BottomNavItem.About
)

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar(
        containerColor = BlackBackground,
        contentColor = CyanGlow
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyanGlow,
                    selectedTextColor = CyanGlow,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray,
                    indicatorColor = DarkBorder
                ),
                modifier = Modifier.testTag("nav_${item.route}")
            )
        }
    }
}
