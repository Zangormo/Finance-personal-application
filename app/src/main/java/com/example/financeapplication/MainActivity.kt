package com.example.financeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.financeapplication.ui.theme.FinanceApplicationTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financeapplication.screens.EssentialsScreen
import com.example.financeapplication.screens.WishlistScreen
import com.example.financeapplication.screens.AddSpendingScreen
import com.example.financeapplication.screens.AddIncomeScreen

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.financeapplication.datastores.UserPreferencesDataStore
import com.example.financeapplication.screens.WelcomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceApplicationTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val isFirstRunState by UserPreferencesDataStore.isFirstRun(context).collectAsState(initial = null)

                if (isFirstRunState != null) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = if (isFirstRunState == true) "welcome_screen" else "main_screen",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("welcome_screen") {
                                WelcomeScreen(onContinueClicked = { balance ->
                                    scope.launch {
                                        UserPreferencesDataStore.setOverallBalance(context, balance)
                                        UserPreferencesDataStore.setFirstRunCompleted(context)
                                        navController.navigate("main_screen") {
                                            popUpTo("welcome_screen") { inclusive = true }
                                        }
                                    }
                                })
                            }
                            composable("main_screen") {
                                MainScreen(
                                    onTileClick = { tile ->
                                        when (tile) {
                                            "Essentials" -> navController.navigate("essentials_screen")
                                            "Wishlist" -> navController.navigate("wishlist_screen")
                                            "Add spending" -> navController.navigate("add_spending_screen")
                                            "Add income" -> navController.navigate("add_income_screen")
                                        }
                                    }

                                )
                            }
                            composable("essentials_screen") {
                                EssentialsScreen(onBackPress = { navController.popBackStack() })
                            }
                            composable("wishlist_screen") {
                                WishlistScreen(onBackPress = { navController.popBackStack() })
                            }
                            composable("add_spending_screen") {
                                AddSpendingScreen(onBackPress = { navController.popBackStack() })
                            }
                            composable("add_income_screen") {
                                AddIncomeScreen(onBackPress = { navController.popBackStack() })
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun MainScreen(modifier: Modifier = Modifier, onTileClick: (String) -> Unit) {
    val items = listOf(
        "Essentials",
        "Wishlist",
        "Add spending",
        "Add income",
        "Transfer to savings",
        "Overview",
        "Spending history",
        "Income history"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 24.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items) { title ->
            TileItem(title = title, onClick = { onTileClick(title) })
        }
    }
}

@Composable
fun TileItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
