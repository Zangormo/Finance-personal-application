package com.example.financeapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.financeapplication.classes.NecessityLevel
import com.example.financeapplication.datastores.SpendingDataStore
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.ui.theme.appColors
import java.util.Calendar

@Composable
fun OverviewScreen(onBackPress: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = appColors()

    val balanceFlow = UserPreferencesDataStore.getOverallBalance(context)
    val currentBalance by balanceFlow.collectAsState(initial = 0f)

    val savingsFlow = UserPreferencesDataStore.getSavingsBalance(context)
    val currentSavings by savingsFlow.collectAsState(initial = 0f)

    val spendingsFlow = SpendingDataStore.getSpendings(context)
    val spendings by spendingsFlow.collectAsState(initial = emptyList())

    // Period selection states
    var selectedPeriod by remember { mutableStateOf(PeriodType.MONTH) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }
    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                text = "Overview",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.primaryText,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Savings balance card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
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

        // Overall balance card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
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

        // Period selection
        Text(
            text = "Spending Overview",
            style = MaterialTheme.typography.titleLarge,
            color = colors.primaryText,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Period buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodButton(
                text = "7 Days",
                isSelected = selectedPeriod == PeriodType.WEEK,
                onClick = { selectedPeriod = PeriodType.WEEK },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            PeriodButton(
                text = "Month",
                isSelected = selectedPeriod == PeriodType.MONTH,
                onClick = { selectedPeriod = PeriodType.MONTH },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
            PeriodButton(
                text = "Year",
                isSelected = selectedPeriod == PeriodType.YEAR,
                onClick = { selectedPeriod = PeriodType.YEAR },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        // Additional period selection based on type
        when (selectedPeriod) {
            PeriodType.MONTH -> {
                MonthYearSelector(
                    selectedMonth = selectedMonth,
                    selectedYear = selectedYear,
                    onMonthChange = { selectedMonth = it },
                    onYearChange = { selectedYear = it },
                    colors = colors,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            PeriodType.YEAR -> {
                YearSelector(
                    selectedYear = selectedYear,
                    onYearChange = { selectedYear = it },
                    colors = colors,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            PeriodType.WEEK -> {
                // No additional selector needed for 7 days
            }
        }

        // Calculate filtered spendings
        val (necessaryTotal, mediumTotal, notNeededTotal) = calculateSpendingByNecessity(
            spendings,
            selectedPeriod,
            selectedYear,
            selectedMonth
        )

        val grandTotal = necessaryTotal + mediumTotal + notNeededTotal

        // Pie chart
        if (grandTotal > 0) {
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PieChart(
                        necessaryAmount = necessaryTotal,
                        mediumAmount = mediumTotal,
                        notNeededAmount = notNeededTotal,
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 16.dp)
                    )

                    // Legend
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LegendItem(
                            label = "Necessary",
                            amount = necessaryTotal,
                            color = Color.Green,
                            total = grandTotal
                        )
                        LegendItem(
                            label = "Medium",
                            amount = mediumTotal,
                            color = Color.Yellow,
                            total = grandTotal
                        )
                        LegendItem(
                            label = "Not Needed",
                            amount = notNeededTotal,
                            color = Color.Red,
                            total = grandTotal
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                        text = "No spendings in this period",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.placeholderText,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Total spending
        if (grandTotal > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "Total Spending",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.placeholderText
                    )
                    Text(
                        text = "$${String.format("%.2f", grandTotal)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.example.financeapplication.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) colors.placeholderText else Color.Transparent,
            contentColor = if (isSelected) colors.primaryText else colors.primaryText
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) androidx.compose.material3.ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    colors: com.example.financeapplication.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month selector
        var expandedMonth by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = months[selectedMonth],
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { expandedMonth = !expandedMonth },
                shape = RoundedCornerShape(8.dp),
                readOnly = true,
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
            DropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                months.forEachIndexed { index, month ->
                    DropdownMenuItem(
                        text = { Text(month) },
                        onClick = {
                            onMonthChange(index)
                            expandedMonth = false
                        }
                    )
                }
            }
        }

        // Year selector
        var expandedYear by remember { mutableStateOf(false) }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearRange = (currentYear - 10)..currentYear
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = selectedYear.toString(),
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { expandedYear = !expandedYear },
                shape = RoundedCornerShape(8.dp),
                readOnly = true,
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
            DropdownMenu(
                expanded = expandedYear,
                onDismissRequest = { expandedYear = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                yearRange.reversed().forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearChange(year)
                            expandedYear = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun YearSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    colors: com.example.financeapplication.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    var expandedYear by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val yearRange = (currentYear - 10)..currentYear

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { expandedYear = !expandedYear },
            shape = RoundedCornerShape(8.dp),
            readOnly = true,
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
        DropdownMenu(
            expanded = expandedYear,
            onDismissRequest = { expandedYear = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            yearRange.reversed().forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearChange(year)
                        expandedYear = false
                    }
                )
            }
        }
    }
}

@Composable
fun PieChart(
    necessaryAmount: Float,
    mediumAmount: Float,
    notNeededAmount: Float,
    modifier: Modifier = Modifier
) {
    val total = necessaryAmount + mediumAmount + notNeededAmount
    if (total == 0f) return

    val necessaryPercentage = necessaryAmount / total
    val mediumPercentage = mediumAmount / total
    val notNeededPercentage = notNeededAmount / total

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw Necessary (Green)
        drawArc(
            color = Color.Green,
            startAngle = 0f,
            sweepAngle = necessaryPercentage * 360f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )

        // Draw Medium (Yellow)
        drawArc(
            color = Color.Yellow,
            startAngle = necessaryPercentage * 360f,
            sweepAngle = mediumPercentage * 360f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )

        // Draw Not Needed (Red)
        drawArc(
            color = Color.Red,
            startAngle = (necessaryPercentage + mediumPercentage) * 360f,
            sweepAngle = notNeededPercentage * 360f,
            useCenter = true,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

@Composable
fun Canvas(modifier: Modifier, onDraw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit) {
    androidx.compose.foundation.Canvas(modifier = modifier, onDraw = onDraw)
}

@Composable
fun LegendItem(
    label: String,
    amount: Float,
    color: Color,
    total: Float
) {
    val percentage = if (total > 0) (amount / total * 100) else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = "$${String.format("%.2f", amount)} (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

enum class PeriodType {
    WEEK, MONTH, YEAR
}

fun calculateSpendingByNecessity(
    spendings: List<com.example.financeapplication.datastores.SpendingRecord>,
    periodType: PeriodType,
    year: Int,
    month: Int
): Triple<Float, Float, Float> {
    val now = Calendar.getInstance()
    val startOfPeriod = Calendar.getInstance()

    when (periodType) {
        PeriodType.WEEK -> {
            startOfPeriod.add(Calendar.DAY_OF_YEAR, -6)
            startOfPeriod.set(Calendar.HOUR_OF_DAY, 0)
            startOfPeriod.set(Calendar.MINUTE, 0)
            startOfPeriod.set(Calendar.SECOND, 0)
            startOfPeriod.set(Calendar.MILLISECOND, 0)
        }
        PeriodType.MONTH -> {
            startOfPeriod.set(year, month, 1, 0, 0, 0)
            startOfPeriod.set(Calendar.MILLISECOND, 0)
        }
        PeriodType.YEAR -> {
            startOfPeriod.set(year, 0, 1, 0, 0, 0)
            startOfPeriod.set(Calendar.MILLISECOND, 0)
        }
    }

    val endOfPeriod = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
    }

    var necessaryTotal = 0f
    var mediumTotal = 0f
    var notNeededTotal = 0f

    spendings.forEach { spending ->
        val spendingCalendar = Calendar.getInstance().apply {
            timeInMillis = spending.timestamp
        }

        if (spendingCalendar.timeInMillis >= startOfPeriod.timeInMillis &&
            spendingCalendar.timeInMillis <= endOfPeriod.timeInMillis
        ) {
            when (spending.necessity) {
                NecessityLevel.NECESSARY -> necessaryTotal += spending.amount
                NecessityLevel.MEDIUM -> mediumTotal += spending.amount
                NecessityLevel.NOT_NEEDED -> notNeededTotal += spending.amount
            }
        }
    }

    return Triple(necessaryTotal, mediumTotal, notNeededTotal)
}
