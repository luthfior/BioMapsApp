package com.example.biomapsapp.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.biomapsapp.R

sealed class BottomNavItem(val route: String, val icon: NavIcon) {
    object Home : BottomNavItem("home", NavIcon.VectorIcon(Icons.Default.Home))
    object Maps : BottomNavItem("maps", NavIcon.DrawableIcon(R.drawable.baseline_map_24))
    object SafeZone : BottomNavItem("safezone", NavIcon.VectorIcon(Icons.Default.List))
}
