package com.example.financeapplication.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import com.example.financeapplication.ui.theme.appColors
import com.example.financeapplication.datastores.WishlistDatastore
import androidx.compose.ui.platform.LocalContext


@SuppressLint("DefaultLocale")
@Composable
fun WishlistScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    var inputText by remember { mutableStateOf("") }
    var inputPrice by remember { mutableStateOf("") }
    val wishlistItemsFlow = WishlistDatastore.getWishlistItems(context)
    val wishlistItems by wishlistItemsFlow.collectAsState(initial = emptyList())

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
                text = "Wishlist",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Input Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Item name...", color = colors.primaryText) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = inputPrice,
                    onValueChange = { newValue ->
                        val filtered = if (newValue.isEmpty()) {
                            ""
                        } else {
                            val parts = newValue.replace(',', '.').split('.')
                            when {
                                parts.size > 2 -> inputPrice
                                parts.size == 2 && parts[1].length > 2 -> inputPrice
                                else -> newValue
                            }
                        }
                        inputPrice = filtered
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Price (optional)", color = colors.primaryText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.primaryText,
                        unfocusedTextColor = colors.primaryText,
                        cursorColor = colors.primaryText,
                        focusedBorderColor = colors.border,
                        unfocusedBorderColor = colors.border,
                        focusedPlaceholderColor = colors.placeholderText,
                        unfocusedPlaceholderColor = colors.placeholderText
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val price = inputPrice.replace(',', '.').toFloatOrNull() ?: 0f
                                scope.launch {
                                    WishlistDatastore.addWishlistItem(context, inputText.trim(), price)
                                    inputText = ""
                                    inputPrice = ""
                                }
                            }
                        },
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = colors.primaryText)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Items List
        if (wishlistItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No wished items yet. Add one above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.border
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = wishlistItems,
                    key = { it.name }
                ) { item ->
                    var editMode by remember { mutableStateOf(false) }
                    var editText by remember { mutableStateOf("") }
                    var editPrice by remember { mutableStateOf("") }

                    LaunchedEffect(item) {
                        editText = item.name
                        editPrice = if (item.price > 0f) item.price.toString() else ""
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (editMode) {
                                BasicTextField(
                                    value = editText,
                                    onValueChange = { editText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = colors.primaryText
                                    ),
                                    cursorBrush = SolidColor(colors.primaryText)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 14.dp)
                                ) {
                                    if (editPrice.isEmpty()) {
                                        Text(
                                            text = "Price (optional)",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.placeholderText
                                        )
                                    }
                                    BasicTextField(
                                        value = editPrice,
                                        onValueChange = { newValue ->
                                            val filtered = if (newValue.isEmpty()) {
                                                ""
                                            } else {
                                                val parts = newValue.replace(',', '.').split('.')
                                                when {
                                                    parts.size > 2 -> editPrice
                                                    parts.size == 2 && parts[1].length > 2 -> editPrice
                                                    else -> newValue
                                                }
                                            }
                                            editPrice = filtered
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                                            color = colors.primaryText
                                        ),
                                        cursorBrush = SolidColor(colors.primaryText)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                val price = editPrice.replace(',', '.').toFloatOrNull() ?: 0f
                                                WishlistDatastore.updateWishlistItem(context, item.name, editText, price)
                                                editMode = false
                                            }
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = colors.primaryText
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Save")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = colors.primaryText
                                        )
                                        if (item.price > 0f) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "$${String.format("%.2f", item.price)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colors.placeholderText
                                            )
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editText = item.name
                                                editPrice = if (item.price > 0f) item.price.toString() else ""
                                                editMode = true
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = colors.primaryText
                                            )
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                                        }
                                        IconButton(
                                            onClick = {
                                                scope.launch { WishlistDatastore.removeWishlistItem(context, item.name) }
                                            },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                contentColor = colors.primaryText
                                            )
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}