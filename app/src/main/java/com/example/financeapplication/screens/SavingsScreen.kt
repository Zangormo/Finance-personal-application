package com.example.financeapplication.screens

import android.annotation.SuppressLint
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
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.ui.theme.appColors
import kotlinx.coroutines.launch
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@SuppressLint("DefaultLocale")
@Composable
fun SavingsScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    val balanceFlow = UserPreferencesDataStore.getOverallBalance(context)
    val currentBalance by balanceFlow.collectAsState(initial = 0f)

    val savingsFlow = UserPreferencesDataStore.getSavingsBalance(context)
    val currentSavings by savingsFlow.collectAsState(initial = 0f)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Save", "Withdraw")

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
                text = "Savings",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Savings balance display
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
                    text = "Savings Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.placeholderText
                )
                Text(
                    text = "$${String.format("%.2f", currentSavings)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.primaryText
                )
            }
        }

        // Overall balance display
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
                    text = "Overall Balance",
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

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.primaryText
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedTab == index) colors.primaryText else colors.placeholderText
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on selected tab
        when (selectedTab) {
            0 -> SaveTab(
                currentBalance = currentBalance,
                onSave = { amount ->
                    scope.launch {
                        UserPreferencesDataStore.updateBalance(context, -amount)
                        UserPreferencesDataStore.updateSavingsBalance(context, amount)
                    }
                }
            )
            1 -> WithdrawTab(
                currentSavings = currentSavings,
                onWithdraw = { amount ->
                    scope.launch {
                        UserPreferencesDataStore.updateSavingsBalance(context, -amount)
                        UserPreferencesDataStore.updateBalance(context, amount)
                    }
                }
            )
        }
    }
}

@Composable
fun SaveTab(
    currentBalance: Float,
    onSave: (Float) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val colors = appColors()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    ),
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
                    },
                    placeholder = { Text("Enter amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                val amount = amountText.toFloatOrNull()
                if (amount == null || amount <= 0) {
                    isError = true
                    errorMessage = "Please enter a valid positive amount"
                    return@Button
                }
                if (amount > currentBalance) {
                    isError = true
                    errorMessage = "Amount cannot exceed overall balance"
                    return@Button
                }
                onSave(amount)
                amountText = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Add to Savings",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun WithdrawTab(
    currentSavings: Float,
    onWithdraw: (Float) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val colors = appColors()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    ),
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
                    },
                    placeholder = { Text("Enter amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Button(
            onClick = {
                val amount = amountText.toFloatOrNull()
                if (amount == null || amount <= 0) {
                    isError = true
                    errorMessage = "Please enter a valid positive amount"
                    return@Button
                }
                if (amount > currentSavings) {
                    isError = true
                    errorMessage = "Amount cannot exceed savings balance"
                    return@Button
                }
                onWithdraw(amount)
                amountText = ""
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Withdraw from Savings",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}