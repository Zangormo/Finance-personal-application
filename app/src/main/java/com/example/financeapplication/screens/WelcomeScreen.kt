package com.example.financeapplication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financeapplication.ui.theme.appColors

import kotlin.math.roundToInt

@Composable
fun WelcomeScreen(onContinueClicked: (Float) -> Unit) {
    var balanceText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val colors = appColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.primaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please enter your current overall balance to get started.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.primaryText
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = balanceText,
            onValueChange = { newValue ->
                // Allow only numbers, one decimal point, and max 2 decimal places
                val filtered = if (newValue.isEmpty()) {
                    ""
                } else {
                    val parts = newValue.replace(',', '.').split('.')
                    when {
                        parts.size > 2 -> balanceText // Ignore if more than one decimal point
                        parts.size == 2 && parts[1].length > 2 -> balanceText // Limit to 2 decimal places
                        else -> newValue
                    }
                }
                balanceText = filtered
                isError = false
            },
            label = { Text("Overall Balance", color = colors.primaryText) },
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

        if (isError) {
            Text(
                text = "Please enter a valid number (e.g. 100.00)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val sanitizedText = balanceText.replace(',', '.')
                val balance = sanitizedText.toFloatOrNull()
                if (balance != null) {
                    // Round to 2 decimal places safely
                    val roundedBalance = (balance * 100).roundToInt() / 100f
                    onContinueClicked(roundedBalance)
                } else {
                    isError = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = colors.primaryText
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}