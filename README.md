# Finance Personal Application

A personal finance tracker for Android, built entirely with Jetpack Compose. The app lets you record daily income and spending, manage essential expenses and a wishlist, track savings, and review your financial activity over daily, monthly, and yearly periods — all stored locally on the device with no internet connection required.

---

## Features

- **Onboarding** — First-launch screen where the user sets their initial overall balance
- **Dashboard (Main Screen)** — 8-tile grid hub linking to every section of the app
- **Add Spending** — Record a spending transaction; select items from Essentials or Wishlist, assign a necessity level (`NECESSARY`, `MEDIUM`, `NOT_NEEDED`), and enter an amount; balance is updated automatically
- **Add Income** — Record an income entry with an amount and free-text description; balance is updated automatically
- **Essentials** — Manage a named list of recurring essential spending items (add, edit, delete)
- **Wishlist** — Manage a list of desired items with associated prices (add, edit, delete)
- **Savings** — Transfer money between the main balance and a separate savings balance via Save / Withdraw tabs
- **Spending History** — Chronological list of all spending records with inline detail view, edit, and delete
- **Income History** — Chronological list of all income records with inline detail view, edit, and delete
- **Overview** — Financial dashboard showing overall balance, current balance, and savings balance; breaks down spending and income totals and averages across daily, monthly, and yearly periods
- **Splash Screen** — Animated fade-in logo screen that routes to Welcome or Main depending on first-run status

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI framework | Jetpack Compose (BOM 2024.09.00) |
| UI components | Material 3 (`androidx.compose.material3`) |
| Navigation | Jetpack Navigation Compose 2.7.3 |
| Persistence | AndroidX DataStore Preferences 1.1.0 |
| Database (configured) | Room 2.6.0 (entities and DAOs present, not actively used) |
| JSON serialization | Gson 2.10.1 |
| Async / reactive | Kotlin Coroutines, `Flow`, `collectAsState()` |
| Build system | Gradle 8.12.0-alpha03 with Kotlin DSL |
| Annotation processing | KAPT 1.9.0 |

---

## Architecture

The app follows a **single-Activity, Compose-first** design with no explicit ViewModel layer. State management and data access are handled through the following conventions:

- **Single Activity** — `MainActivity` hosts the `NavHost` and all route definitions
- **Compose state** — Each screen manages its own UI state via `mutableStateOf()` and `rememberCoroutineScope()`
- **DataStore as single source of truth** — All persistent data is read and written through dedicated DataStore wrapper objects; the UI collects `Flow`s from these stores and recomposes reactively
- **No Repository layer** — DataStore wrappers are called directly from composable screens

**Key classes:**

| Class | Role |
|---|---|
| `MainActivity` | Entry point; hosts `NavHost` with all 10 routes |
| `SplashScreen` | Animated entry screen; determines first-run routing |
| `WelcomeScreen` | Onboarding balance input |
| `MainScreen` | Navigation hub (grid of tiles) |
| `AddSpendingScreen` / `AddIncomeScreen` | Transaction entry screens |
| `SpendingHistoryScreen` / `IncomeHistoryScreen` | History views with edit/delete |
| `EssentialsScreen` / `WishlistScreen` | Item list management |
| `SavingsScreen` | Savings transfer interface |
| `OverviewScreen` | Period-based financial summary |
| `UserPreferencesDataStore` | Stores overall balance, savings balance, first-run flag |
| `SpendingDataStore` | Stores and queries spending records (JSON) |
| `IncomeDataStore` | Stores and queries income records (JSON) |
| `EssentialsDataStore` | Stores essential item names (StringSet) |
| `WishlistDatastore` | Stores wishlist items with prices (StringSet) |
| `FinanceDatabase` / `SpendingDao` / `IncomeDao` | Room setup (configured, unused) |
| `NecessityLevel` | Enum: `NECESSARY`, `MEDIUM`, `NOT_NEEDED` |

---

## Data Storage

All data is stored **locally on the device** using **AndroidX DataStore Preferences**. There is no network layer and no remote database.

| DataStore file | Key | Type | Description |
|---|---|---|---|
| `user_prefs` | `overall_balance` | `Float` | User's total available balance |
| `user_prefs` | `savings_balance` | `Float` | Dedicated savings balance |
| `user_prefs` | `is_first_run` | `Boolean` | First-launch flag |
| `spending_prefs` | spending list | `String` (JSON) | All spending records serialized with Gson |
| `income_prefs` | income list | `String` (JSON) | All income records serialized with Gson |
| `essentials_prefs` | essentials set | `StringSet` | Names of essential items |
| `wishlist_prefs` | wishlist set | `StringSet` | Wishlist items serialized as `"name\|price"` |

A **Room database** (`FinanceDatabase`) with `Spending` and `Income` entities and corresponding DAOs is also present in the codebase but is not currently wired into the application flow.

---

## Getting Started

### Requirements

- **Android Studio** — Ladybug or newer recommended
- **Min SDK** — 34 (Android 14)
- **Compile / Target SDK** — 36
- **JDK** — 11

### Clone and run

```bash
git clone <repository-url>
cd Finance-personal-application
```

1. Open the `Financeapplication/` folder in Android Studio as the project root
2. Wait for Gradle sync to complete
3. Connect a physical device running Android 14+ or create an emulator with API 34+
4. Run the `app` configuration (`Shift+F10` / green ▶ button)

> **Note:** `local.properties` is not tracked by Git. Android Studio generates it automatically with your local SDK path.

---

## Project Structure

```
Financeapplication/
├── app/
│   ├── build.gradle.kts              # App-level dependencies and SDK config
│   └── src/main/java/com/example/financeapplication/
│       ├── MainActivity.kt           # Single activity + NavHost (all routes)
│       ├── classes/
│       │   ├── Income.kt             # Room entity + IncomeRecord data class
│       │   ├── Spending.kt           # Room entity + SpendingRecord data class
│       │   ├── WishlistItem.kt       # WishlistItem data class
│       │   ├── NecessityLevel.kt     # Enum: NECESSARY, MEDIUM, NOT_NEEDED
│       │   └── StringListConverter.kt# Room TypeConverter (List<String> ↔ JSON)
│       ├── database/
│       │   ├── FinanceDatabase.kt    # Room database definition
│       │   ├── SpendingDao.kt        # Room DAO for spendings
│       │   └── IncomeDao.kt          # Room DAO for incomes
│       ├── datastores/
│       │   ├── UserPreferencesDataStore.kt
│       │   ├── SpendingDataStore.kt
│       │   ├── IncomeDataStore.kt
│       │   ├── EssentialsDataStore.kt
│       │   └── WishlistDatastore.kt
│       ├── screens/
│       │   ├── SplashScreen.kt
│       │   ├── WelcomeScreen.kt
│       │   ├── MainScreen.kt
│       │   ├── AddSpendingScreen.kt
│       │   ├── AddIncomeScreen.kt
│       │   ├── SpendingHistoryScreen.kt
│       │   ├── IncomeHistoryScreen.kt
│       │   ├── EssentialsScreen.kt
│       │   ├── WishlistScreen.kt
│       │   ├── SavingsScreen.kt
│       │   └── OverviewScreen.kt
│       └── ui/theme/
│           ├── Theme.kt              # Material 3 light/dark theme + dynamic color
│           ├── AppColors.kt          # Custom color helper
│           ├── Color.kt
│           └── Type.kt
├── gradle/
│   ├── libs.versions.toml            # Centralized version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts                  # Top-level build config
└── settings.gradle.kts
```

---

## Version

**Alpha build 1.2** — Version code 1
