package com.example.biomapsapp.utils

import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavIcon {
    data class VectorIcon(val icon: ImageVector) : NavIcon()
    data class DrawableIcon(val resId: Int) : NavIcon()
}