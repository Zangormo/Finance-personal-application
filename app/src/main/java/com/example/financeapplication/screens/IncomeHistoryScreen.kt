package com.example.financeapplication.screens

import android.annotation.SuppressLint
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
import com.example.financeapplication.datastores.IncomeDataStore
import com.example.financeapplication.datastores.IncomeRecord
import com.example.financeapplication.ui.theme.appColors
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IncomeHistoryScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = appColors()

    val incomesFlow = IncomeDataStore.getIncomes(context)
    val incomes by incomesFlow.collectAsState(initial = emptyList())

    var selectedIncome by remember { mutableStateOf<IncomeRecord?>(null) }

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
                text = "Income History",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        if (incomes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No income records yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.placeholderText
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(incomes.reversed()) { income ->
                    IncomeHistoryItem(
                        income = income,
                        colors = colors,
                        onClick = { selectedIncome = income }
                    )
                }
            }
        }
    }

    // Show detail dialog when income is selected
    if (selectedIncome != null) {
        IncomeDetailDialog(
            income = selectedIncome!!,
            colors = colors,
            onDismiss = { selectedIncome = null }
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun IncomeHistoryItem(
    income: IncomeRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val date = dateFormat.format(Date(income.timestamp))

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
                    text = income.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primaryText
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.placeholderText
                )
            }
            Text(
                text = "$${String.format("%.2f", income.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.primaryText
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun IncomeDetailDialog(
    income: IncomeRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val dateTime = dateFormat.format(Date(income.timestamp))

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Income Details",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.primaryText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRowItem("Description:", income.description, colors)
                DetailRowItem("Amount:", "$${String.format("%.2f", income.amount)}", colors, colors.primaryText)
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
