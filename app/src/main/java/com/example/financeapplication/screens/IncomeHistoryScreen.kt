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
import com.example.financeapplication.datastores.IncomeDataStore
import com.example.financeapplication.datastores.IncomeRecord
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.ui.theme.appColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun IncomeHistoryScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = appColors()
    val scope = rememberCoroutineScope()

    val incomesFlow = IncomeDataStore.getIncomes(context)
    val incomes by incomesFlow.collectAsState(initial = emptyList())

    var selectedIncome by remember { mutableStateOf<IncomeRecord?>(null) }
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
        if (isEditingMode) {
            IncomeEditDialog(
                income = selectedIncome!!,
                colors = colors,
                onDismiss = {
                    selectedIncome = null
                    isEditingMode = false
                },
                onSave = { newAmount, newDescription ->
                    scope.launch {
                        val oldIncome = selectedIncome!!
                        val amountDifference = newAmount - oldIncome.amount
                        
                        // Update income record
                        IncomeDataStore.updateIncome(
                            context,
                            oldIncome,
                            newAmount,
                            newDescription
                        )
                        
                        // Update balance with the difference
                        UserPreferencesDataStore.updateBalance(context, amountDifference)
                        
                        selectedIncome = null
                        isEditingMode = false
                    }
                }
            )
        } else {
            IncomeDetailDialog(
                income = selectedIncome!!,
                colors = colors,
                onDismiss = { selectedIncome = null },
                onEdit = { isEditingMode = true },
                onDelete = {
                    scope.launch {
                        val income = selectedIncome!!
                        // Delete income record
                        IncomeDataStore.deleteIncome(context, income)
                        
                        // Update balance (subtract income amount)
                        UserPreferencesDataStore.updateBalance(context, -income.amount)
                        
                        selectedIncome = null
                    }
                }
            )
        }
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
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    Text("")
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
fun IncomeEditDialog(
    income: IncomeRecord,
    colors: com.example.financeapplication.ui.theme.AppColors,
    onDismiss: () -> Unit,
    onSave: (Float, String) -> Unit
) {
    var amountText by remember { mutableStateOf(income.amount.toString()) }
    var descriptionText by remember { mutableStateOf(income.description) }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Edit Income",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.primaryText
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
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
                        descriptionText.trim().isEmpty() -> {
                            isError = true
                            errorMessage = "Description cannot be empty"
                        }
                        else -> {
                            val roundedAmount = (amount * 100).roundToInt() / 100f
                            onSave(roundedAmount, descriptionText.trim())
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
