package com.example.financeapplication.datastores

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financeapplication.classes.NecessityLevel
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStoreSpending by preferencesDataStore("spending_prefs")

data class SpendingRecord(
    val amount: Float,
    val items: List<String>,
    val timestamp: Long,
    val necessity: NecessityLevel = NecessityLevel.NECESSARY
)

object SpendingDataStore {

    private val SPENDINGS_KEY = stringPreferencesKey("spendings_list")
    private val gson = Gson()

    fun getSpendings(context: Context): Flow<List<SpendingRecord>> {
        return context.dataStoreSpending.data.map { prefs ->
            val json = prefs[SPENDINGS_KEY] ?: return@map emptyList()
            try {
                gson.fromJson(json, Array<SpendingRecord>::class.java).toList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun addSpending(context: Context, amount: Float, items: List<String>, necessity: NecessityLevel = NecessityLevel.NECESSARY) {
        context.dataStoreSpending.edit { prefs ->
            val json = prefs[SPENDINGS_KEY] ?: ""
            val current = try {
                if (json.isNotEmpty()) {
                    gson.fromJson(json, Array<SpendingRecord>::class.java).toMutableList()
                } else {
                    mutableListOf()
                }
            } catch (_: Exception) {
                mutableListOf()
            }
            
            current.add(SpendingRecord(
                amount = amount,
                items = items,
                timestamp = System.currentTimeMillis(),
                necessity = necessity
            ))
            
            prefs[SPENDINGS_KEY] = gson.toJson(current)
        }
    }
}
