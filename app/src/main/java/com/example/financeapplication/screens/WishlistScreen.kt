package com.example.financeapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
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
import kotlinx.coroutines.launch
import com.example.financeapplication.ui.theme.appColors
import com.example.financeapplication.datastores.WishlistDatastore
import androidx.compose.ui.platform.LocalContext


@Composable
fun WishlistScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    var inputText by remember { mutableStateOf("") }
    val wishlistFlow = WishlistDatastore.getWishlist(context)
    val wishedItems by wishlistFlow.collectAsState(initial = emptyList())

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
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add new wishlist item...", color = colors.primaryText) },
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
                Spacer(modifier = Modifier.width(12.dp))
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            scope.launch {
                                WishlistDatastore.addWishlistItem(context, inputText.trim())
                                inputText = ""
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

        Spacer(modifier = Modifier.height(24.dp))

        // Items List
        if (wishedItems.isEmpty()) {
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
                    items = wishedItems,
                    key = { it }
                ) { item ->
                    var editMode by remember { mutableStateOf(false) }
                    var editText by remember { mutableStateOf("") }

                    LaunchedEffect(item) {
                        editText = item
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (editMode) {
                                BasicTextField(
                                    value = editText,
                                    onValueChange = { editText = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(68.dp)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = colors.primaryText
                                    ),
                                    cursorBrush = SolidColor(colors.primaryText)
                                )

                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            WishlistDatastore.updateWishlistItem(context, item, editText)
                                            editMode = false
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = colors.primaryText
                                    )
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            } else {
                                Text(
                                    text = item,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp, vertical = 14.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.primaryText
                                )

                                IconButton(
                                    onClick = {
                                        editText = item
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
                                        scope.launch { WishlistDatastore.removeWishlistItem(context, item) }
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