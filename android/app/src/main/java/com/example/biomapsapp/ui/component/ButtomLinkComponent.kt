package com.example.biomapsapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomLinkQuestion(
    onClick: () -> Unit,
    textQ: String,
    textB: String) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = textQ,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        TextButton(
            onClick = { onClick() }
        ) {
            Text(
                text = textB,
                fontSize = 16.sp,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}