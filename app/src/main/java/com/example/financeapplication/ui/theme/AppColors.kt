package com.example.financeapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val primaryText: Color,
    val border: Color,
    val placeholderText: Color
)

@Composable
fun appColors(): AppColors {
    return AppColors(
        primaryText = MaterialTheme.colorScheme.onSurface,
        border = MaterialTheme.colorScheme.outline,
        placeholderText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}