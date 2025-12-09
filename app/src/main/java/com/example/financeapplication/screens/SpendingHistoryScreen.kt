package com.example.financeapplication.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.financeapplication.datastores.SpendingDataStore
import com.example.financeapplication.datastores.SpendingRecord
import com.example.financeapplication.ui.theme.appColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SpendingHistoryScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = appColors()

    val spendingsFlow = SpendingDataStore.getSpendings(context)
    val spendings by spendingsFlow.collectAsState(initial = emptyList())

    var selectedSpending by remember { mutableStateOf<SpendingRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackPress() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.primaryText
                )
            }
            Text(
                text = "Spending History",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (spendings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spending records yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.placeholderText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spendings.reversed()) { spending ->
                    SpendingHistoryItem(
                        spending = spending,
                        colors = colors,
                        onClick = { selectedSpending = spending }
                    )
                }
            }
        }
    }

    // Show detail dialog when spending is selected
    if (selectedSpending != null) {
        SpendingDetailDialog(
            spending = selectedSpending!!,
            colors = colors,
            onDismiss = { selectedSpending = null }
        )
    }
}

@Composable
fun SpendingHistoryItem(
    spending: SpendingRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val date = dateFormat.format(Date(spending.timestamp))
    val itemsText = if (spending.items.isEmpty()) "-" else spending.items.take(2).joinToString(", ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemsText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primaryText,
                    maxLines = 1
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.placeholderText
                )
            }
            Text(
                text = "$${String.format("%.2f", spending.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.primaryText
            )
        }
    }
}

@Composable
fun SpendingDetailDialog(
    spending: SpendingRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateTime = dateFormat.format(Date(spending.timestamp))
    val itemsText = if (spending.items.isEmpty()) "-" else spending.items.joinToString("\n")

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Spending Details",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.primaryText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRowItem("Items:", itemsText, colors)
                DetailRowItem("Amount:", "$${String.format("%.2f", spending.amount)}", colors)
                DetailRowItem("Date:", dateTime, colors)
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Close",
                    color = colors.primaryText
                )

            }
        }
    )
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    colors: com.example.financeapplication.ui.theme.AppColors,
    valueColor: Color? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.placeholderText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor ?: colors.primaryText
        )
    }
}
