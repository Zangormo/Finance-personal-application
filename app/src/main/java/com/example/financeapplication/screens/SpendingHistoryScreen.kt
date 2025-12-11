package com.example.financeapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.financeapplication.datastores.SpendingDataStore
import com.example.financeapplication.datastores.SpendingRecord
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.ui.theme.appColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun SpendingHistoryScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = appColors()
    val scope = rememberCoroutineScope()

    val spendingsFlow = SpendingDataStore.getSpendings(context)
    val spendings by spendingsFlow.collectAsState(initial = emptyList())

    var selectedSpending by remember { mutableStateOf<SpendingRecord?>(null) }
    var isEditingMode by remember { mutableStateOf(false) }

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
        if (isEditingMode) {
            SpendingEditDialog(
                spending = selectedSpending!!,
                colors = colors,
                onDismiss = {
                    selectedSpending = null
                    isEditingMode = false
                },
                onSave = { newAmount ->
                    scope.launch {
                        val oldSpending = selectedSpending!!
                        val amountDifference = newAmount - oldSpending.amount
                        
                        // Update spending record
                        SpendingDataStore.updateSpending(
                            context,
                            oldSpending,
                            newAmount,
                            oldSpending.items,
                            oldSpending.necessity
                        )
                        
                        // Update balance with the difference (spending reduces balance, so add negative amount)
                        UserPreferencesDataStore.updateBalance(context, -amountDifference)
                        
                        selectedSpending = null
                        isEditingMode = false
                    }
                }
            )
        } else {
            SpendingDetailDialog(
                spending = selectedSpending!!,
                colors = colors,
                onDismiss = { selectedSpending = null },
                onEdit = { isEditingMode = true },
                onDelete = {
                    scope.launch {
                        val spending = selectedSpending!!
                        // Delete spending record
                        SpendingDataStore.deleteSpending(context, spending)
                        
                        // Update balance (add back the spending amount)
                        UserPreferencesDataStore.updateBalance(context, spending.amount)
                        
                        selectedSpending = null
                    }
                }
            )
        }
    }
}

@SuppressLint("DefaultLocale")
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

@SuppressLint("DefaultLocale")
@Composable
fun SpendingDetailDialog(
    spending: SpendingRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDelete() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.primaryText
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Button(
                    onClick = { onEdit() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.primaryText
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Button(
                    onClick = { onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.primaryText
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    )
}

@Composable
fun SpendingEditDialog(
    spending: SpendingRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var amountText by remember { mutableStateOf(spending.amount.toString()) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Edit Spending Amount",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.primaryText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        val filtered = if (newValue.isEmpty()) {
                            ""
                        } else {
                            val parts = newValue.replace(',', '.').split('.')
                            when {
                                parts.size > 2 -> amountText
                                parts.size == 2 && parts[1].length > 2 -> amountText
                                else -> newValue.replace(',', '.')
                            }
                        }
                        amountText = filtered
                        isError = false
                    },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.primaryText),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        focusedBorderColor = colors.primaryText,
                        unfocusedBorderColor = colors.placeholderText,
                        focusedLabelColor = colors.primaryText,
                        unfocusedLabelColor = colors.placeholderText
                    )
                )

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sanitizedText = amountText.replace(',', '.')
                    val amount = sanitizedText.toFloatOrNull()

                    when {
                        amount == null -> {
                            isError = true
                            errorMessage = "Please enter a valid amount (e.g. 100.00)"
                        }
                        amount <= 0 -> {
                            isError = true
                            errorMessage = "Amount must be greater than 0"
                        }
                        else -> {
                            val roundedAmount = (amount * 100).roundToInt() / 100f
                            onSave(roundedAmount)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.primaryText
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colors.primaryText
                )
            ) {
                Text("Cancel")
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
