package com.example.financeapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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

    val wishlistFlow = WishlistDatastore.getWishlist(context)
    val wishedItems by wishlistFlow.collectAsState(initial = emptyList())

    val balanceFlow = UserPreferencesDataStore.getOverallBalance(context)
    val currentBalance by balanceFlow.collectAsState(initial = 0f)

    var selectedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var necessityLevel by remember { mutableStateOf(NecessityLevel.NECESSARY) }

    Column(
        modifier = Modifier.fillMaxSize()
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
            if (wishedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Wishlisted",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.primaryText,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                items(wishedItems) { item ->
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
            }
        }

        // Fixed bottom section with amount input and button
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        amount > currentBalance -> {
                            isError = true
                            errorMessage = "Amount exceeds your current balance"
                        }
                        else -> {
                            val roundedAmount = (amount * 100).roundToInt() / 100f
                            scope.launch {
                                // Add spending record
                                SpendingDataStore.addSpending(
                                    context,
                                    roundedAmount,
                                    selectedItems.toList(),
                                    necessityLevel
                                )

                                // Update balance (subtract spent amount)
                                UserPreferencesDataStore.updateBalance(context, -roundedAmount)

                                // Remove selected items from essentials and wishlist
                                selectedItems.forEach { item ->
                                    if (essentials.contains(item)) {
                                        EssentialsDataStore.removeEssential(context, item)
                                    }
                                    if (wishedItems.contains(item)) {
                                        WishlistDatastore.removeWishlistItem(context, item)
                                    }
                                }

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
                Text("Add Spending")
            }
        }
    }
}

@Composable
fun ItemCheckBox(
    item: String,
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

        Text(
            text = item,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.primaryText
        )
    }
}
