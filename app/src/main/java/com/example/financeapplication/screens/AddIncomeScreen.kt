package com.example.financeapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.financeapplication.datastores.IncomeDataStore
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.ui.theme.appColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AddIncomeScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    val balanceFlow = UserPreferencesDataStore.getOverallBalance(context)
    val currentBalance by balanceFlow.collectAsState(initial = 0f)

    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

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
                text = "Add Income",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Current balance display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.placeholderText
                )
                Text(
                    text = "$${String.format("%.2f", currentBalance)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.primaryText
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Amount input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
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
                                else -> newValue
                            }
                        }
                        amountText = filtered
                        isError = false
                        errorMessage = ""
                    },
                    label = { Text("Income Amount", color = colors.primaryText) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.primaryText,
                        unfocusedLabelColor = colors.primaryText,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description", color = colors.primaryText) },
                    placeholder = { Text("e.g., Salary, Bonus, etc.", color = colors.placeholderText) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedLabelColor = colors.primaryText,
                        unfocusedLabelColor = colors.primaryText,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    )
                )

                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        }

        // Add Income button
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
                        scope.launch {
                            // Add income record
                            IncomeDataStore.addIncome(
                                context,
                                roundedAmount,
                                descriptionText.trim()
                            )

                            // Update balance (add income amount)
                            UserPreferencesDataStore.updateBalance(context, roundedAmount)

                            // Navigate back
                            onBackPress()
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colors.primaryText
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Income")
        }
    }
}
