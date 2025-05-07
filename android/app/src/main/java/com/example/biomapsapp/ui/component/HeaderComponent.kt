package com.example.biomapsapp.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

@Composable
fun HeaderWelcome(
    text1: String,
    text2: String
) {
    Column {
        Text(
            text = text1,
            fontSize = 18.sp,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = text2,
            fontSize = 24.sp,
            style = MaterialTheme.typography.titleLarge
        )
    }
}