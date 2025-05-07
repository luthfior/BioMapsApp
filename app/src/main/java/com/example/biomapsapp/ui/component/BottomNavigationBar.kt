package com.example.biomapsapp.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.example.biomapsapp.utils.BottomNavItem
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.biomapsapp.utils.NavIcon


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Maps,
        BottomNavItem.SafeZone
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    when (val icon = item.icon) {
                        is NavIcon.VectorIcon -> Icon(
                            imageVector = icon.icon,
                            contentDescription = item.icon.toString()
                        )
                        is NavIcon.DrawableIcon -> Icon(
                            painter = painterResource(id = icon.resId),
                            contentDescription = item.icon.toString()
                        )
                    }
                },
            )
        }
    }
}

