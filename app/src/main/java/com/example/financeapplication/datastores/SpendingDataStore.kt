package com.example.financeapplication.datastores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStoreSpending by preferencesDataStore("spending_prefs")

data class SpendingRecord(
    val amount: Float,
    val items: List<String>,
    val timestamp: Long
)

object SpendingDataStore {

    private val SPENDINGS_KEY = stringPreferencesKey("spendings_list")
    private val gson = Gson()

    fun getSpendings(context: Context): Flow<List<SpendingRecord>> {
        return context.dataStoreSpending.data.map { prefs ->
            val json = prefs[SPENDINGS_KEY] ?: return@map emptyList()
            try {
                gson.fromJson(json, Array<SpendingRecord>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addSpending(context: Context, amount: Float, items: List<String>) {
        context.dataStoreSpending.edit { prefs ->
            val json = prefs[SPENDINGS_KEY] ?: ""
            val current = try {
                if (json.isNotEmpty()) {
                    gson.fromJson(json, Array<SpendingRecord>::class.java).toMutableList()
                } else {
                    mutableListOf()
                }
            } catch (e: Exception) {
                mutableListOf()
            }
            
            current.add(SpendingRecord(
                amount = amount,
                items = items,
                timestamp = System.currentTimeMillis()
            ))
            
            prefs[SPENDINGS_KEY] = gson.toJson(current)
        }
    }

    suspend fun clearAllSpendings(context: Context) {
        context.dataStoreSpending.edit { prefs ->
            prefs.remove(SPENDINGS_KEY)
        }
    }
}
