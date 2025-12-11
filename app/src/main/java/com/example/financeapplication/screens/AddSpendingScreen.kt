package com.example.financeapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import com.example.financeapplication.classes.NecessityLevel
import com.example.financeapplication.datastores.EssentialsDataStore
import com.example.financeapplication.datastores.SpendingDataStore
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.datastores.WishlistDatastore
import com.example.financeapplication.ui.theme.appColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("DefaultLocale")
@Composable
fun AddSpendingScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    val essentialsFlow = EssentialsDataStore.getEssentials(context)
    val essentials by essentialsFlow.collectAsState(initial = emptyList())

    val wishlistItemsFlow = WishlistDatastore.getWishlistItems(context)
    val wishlistItems by wishlistItemsFlow.collectAsState(initial = emptyList())

    val balanceFlow = UserPreferencesDataStore.getOverallBalance(context)
    val currentBalance by balanceFlow.collectAsState(initial = 0f)

    val savingsFlow = UserPreferencesDataStore.getSavingsBalance(context)
    val savingsBalance by savingsFlow.collectAsState(initial = 0f)

    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var necessityLevel by remember { mutableStateOf(NecessityLevel.NECESSARY) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("") }
    var withdrawError by remember { mutableStateOf("") }
    var insufficientFundsMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    val keyboardOffset = with(density) { (imeHeight * 0.3f).toDp() }

    // Calculate total price of selected items with prices
    val totalSelectedPrice = selectedItems.sumOf { itemName ->
        wishlistItems.find { it.name == itemName }?.price?.toDouble() ?: 0.0
    }.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = keyboardOffset)
    ) {
        // Header and balance in a non-scrollable section
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Add Spending",
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
        }

        // Scrollable content area
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Essentials Section
            if (essentials.isNotEmpty()) {
                item {
                    Text(
                        text = "Essentials",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(essentials) { item ->
                    ItemCheckBox(
                        item = item,
                        isSelected = item in selectedItems,
                        onSelectionChange = { isSelected ->
                            selectedItems = if (isSelected) {
                                selectedItems + item
                            } else {
                                selectedItems - item
                            }
                        },
                        colors = colors
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Wishlisted Section
            if (wishlistItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Wishlisted",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(wishlistItems) { item ->
                    ItemCheckBox(
                        item = item.name,
                        price = if (item.price > 0f) "$${String.format("%.2f", item.price)}" else null,
                        isSelected = item.name in selectedItems,
                        onSelectionChange = { isSelected ->
                            selectedItems = if (isSelected) {
                                selectedItems + item.name
                            } else {
                                selectedItems - item.name
                            }
                        },
                        colors = colors
                    )
                }
            }
        }

        // Fixed bottom section with amount input and button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Amount input
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Necessity level selector
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Necessity:", color = colors.primaryText) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(2.dp, colors.border, CircleShape)
                                    .background(
                                        color = when (necessityLevel) {
                                            NecessityLevel.NECESSARY -> Color(33, 117, 30)
                                            NecessityLevel.MEDIUM -> Color(212, 130, 49)
                                            NecessityLevel.NOT_NEEDED -> Color(179, 52, 52)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        necessityLevel = when (necessityLevel) {
                                            NecessityLevel.NECESSARY -> NecessityLevel.MEDIUM
                                            NecessityLevel.MEDIUM -> NecessityLevel.NOT_NEEDED
                                            NecessityLevel.NOT_NEEDED -> NecessityLevel.NECESSARY
                                        }
                                    }
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText,
                            cursorColor = colors.primaryText,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.primaryText,
                            unfocusedLabelColor = colors.primaryText,
                            disabledTextColor = Color.Transparent,
                            disabledBorderColor = colors.border,
                            disabledLabelColor = colors.primaryText
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                        label = { Text("Amount Spent", color = colors.primaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
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

                    if (isError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }            // Add Spending button
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
                            
                            // Check if we have enough balance for wishlist items with prices
                            if (totalSelectedPrice > currentBalance) {
                                val deficit = totalSelectedPrice - currentBalance
                                // Check if savings can cover the deficit
                                if (savingsBalance >= deficit) {
                                    // Show dialog to withdraw from savings
                                    insufficientFundsMessage = ""
                                    withdrawAmount = String.format("%.2f", deficit)
                                    showWithdrawDialog = true
                                } else {
                                    // Not enough even with savings
                                    val totalAvailable = currentBalance + savingsBalance
                                    insufficientFundsMessage = "Insufficient funds for selected items! You need $${String.format("%.2f", totalSelectedPrice)} but have only $${String.format("%.2f", totalAvailable)} available (balance: $${String.format("%.2f", currentBalance)} + savings: $${String.format("%.2f", savingsBalance)})"
                                }
                            } else {
                                // Enough balance, proceed with spending
                                scope.launch {
                                    // Add spending record
                                    SpendingDataStore.addSpending(
                                        context,
                                        roundedAmount,
                                        selectedItems.toList(),
                                        necessityLevel
                                    )

                                    // Update balance (subtract spent amount + wishlist prices)
                                    UserPreferencesDataStore.updateBalance(context, -(roundedAmount + totalSelectedPrice))

                                    // Remove selected items from essentials and wishlist
                                    selectedItems.forEach { item ->
                                        if (essentials.contains(item)) {
                                            EssentialsDataStore.removeEssential(context, item)
                                        }
                                        val wishlistItem = wishlistItems.find { it.name == item }
                                        if (wishlistItem != null) {
                                            WishlistDatastore.removeWishlistItem(context, item)
                                        }
                                    }

                                    // Navigate back
                                    onBackPress()
                                }
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
                Text("Add Spending")
            }

            // Show insufficient funds message
            if (insufficientFundsMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4A1E1E)
                    )
                ) {
                    Text(
                        text = insufficientFundsMessage,
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }

    // Withdraw from Savings Dialog
    if (showWithdrawDialog) {
        AlertDialog(
            onDismissRequest = {
                showWithdrawDialog = false
                withdrawAmount = ""
                withdrawError = ""
            },
            title = {
                Text(
                    text = "Withdraw from Savings",
                    color = colors.primaryText
                )
            },
            text = {
                Column {
                    Text(
                        text = "Selected wishlist items cost $${String.format("%.2f", totalSelectedPrice)} but you only have $${String.format("%.2f", currentBalance)} in balance.",
                        color = colors.primaryText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "You need at least $${String.format("%.2f", totalSelectedPrice - currentBalance)} from savings.",
                        color = colors.primaryText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Available savings: $${String.format("%.2f", savingsBalance)}",
                        color = colors.placeholderText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { newValue ->
                            val filtered = if (newValue.isEmpty()) {
                                ""
                            } else {
                                val parts = newValue.replace(',', '.').split('.')
                                when {
                                    parts.size > 2 -> withdrawAmount
                                    parts.size == 2 && parts[1].length > 2 -> withdrawAmount
                                    else -> newValue
                                }
                            }
                            withdrawAmount = filtered
                            withdrawError = ""
                        },
                        label = { Text("Amount to withdraw", color = colors.primaryText) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(8.dp),
                        isError = withdrawError.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.primaryText,
                            unfocusedTextColor = colors.primaryText,
                            cursorColor = colors.primaryText,
                            focusedBorderColor = colors.border,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.primaryText,
                            unfocusedLabelColor = colors.primaryText
                        )
                    )
                    if (withdrawError.isNotEmpty()) {
                        Text(
                            text = withdrawError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sanitizedWithdraw = withdrawAmount.replace(',', '.')
                        val withdrawVal = sanitizedWithdraw.toFloatOrNull()

                        when {
                            withdrawVal == null -> {
                                withdrawError = "Please enter a valid amount"
                            }
                            withdrawVal <= 0 -> {
                                withdrawError = "Amount must be greater than 0"
                            }
                            withdrawVal > savingsBalance -> {
                                withdrawError = "Amount exceeds available savings"
                            }
                            else -> {
                                val roundedAmount = (amountText.replace(',', '.').toFloatOrNull() ?: 0f)
                                val roundedWithdraw = (withdrawVal * 100).roundToInt() / 100f
                                val totalCost = roundedAmount + totalSelectedPrice
                                val balanceAfterWithdraw = currentBalance + roundedWithdraw
                                
                                // Check if total cost exceeds new balance after withdrawal
                                if (totalCost > balanceAfterWithdraw) {
                                    val stillNeeded = totalCost - balanceAfterWithdraw
                                    withdrawError = "Not enough funds! You need additional $${String.format("%.2f", stillNeeded)} even after withdrawing $${String.format("%.2f", roundedWithdraw)}"
                                } else {
                                    // Process the spending with withdrawn amount
                                    scope.launch {
                                        // Add spending record
                                        SpendingDataStore.addSpending(
                                            context,
                                            roundedAmount,
                                            selectedItems.toList(),
                                            necessityLevel
                                        )

                                        // Withdraw from savings first
                                        UserPreferencesDataStore.updateSavingsBalance(context, -roundedWithdraw)
                                        UserPreferencesDataStore.updateBalance(context, roundedWithdraw)
                                        
                                        // Then subtract total cost from balance
                                        UserPreferencesDataStore.updateBalance(context, -totalCost)

                                        // Remove selected items from essentials and wishlist
                                        selectedItems.forEach { item ->
                                            if (essentials.contains(item)) {
                                                EssentialsDataStore.removeEssential(context, item)
                                            }
                                            val wishlistItem = wishlistItems.find { it.name == item }
                                            if (wishlistItem != null) {
                                                WishlistDatastore.removeWishlistItem(context, item)
                                            }
                                        }

                                        showWithdrawDialog = false
                                        // Navigate back
                                        onBackPress()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.primaryText
                    )
                ) {
                    Text("Withdraw")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showWithdrawDialog = false
                        withdrawAmount = ""
                        withdrawError = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.primaryText
                    )
                ) {
                    Text("Cancel")
                }
            },
            textContentColor = colors.primaryText
        )
    }
}

@Composable
fun ItemCheckBox(
    item: String,
    price: String? = null,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    colors: com.example.financeapplication.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circle checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .shadow(2.dp, CircleShape)
                .background(
                    color = if (isSelected) colors.border else Color.Transparent,
                    shape = CircleShape
                )
                .border(2.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = colors.primaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.primaryText
            )
            if (price != null) {
                Text(
                    text = price,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.placeholderText
                )
            }
        }
    }
}
