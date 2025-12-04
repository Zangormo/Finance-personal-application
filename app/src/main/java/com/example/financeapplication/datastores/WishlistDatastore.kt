package com.example.financeapplication.datastores

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStoreWishlist by preferencesDataStore("wishlist_prefs")

object WishlistDatastore {

    private val WISHLIST_KEY = stringSetPreferencesKey("wishlist_list")

    fun getWishlist(context: Context): Flow<List<String>> {
        return context.dataStoreWishlist.data.map { prefs ->
            prefs[WISHLIST_KEY]?.toList() ?: emptyList()
        }
    }

    suspend fun addWishlistItem(context: Context, item: String) {
        context.dataStoreWishlist.edit { prefs ->
            val current = prefs[WISHLIST_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add(item)
            prefs[WISHLIST_KEY] = current
        }
    }

    suspend fun removeWishlistItem(context: Context, item: String) {
        context.dataStoreWishlist.edit { prefs ->
            val current = prefs[WISHLIST_KEY]?.toMutableSet() ?: mutableSetOf()
            current.remove(item)
            prefs[WISHLIST_KEY] = current
        }
    }

    suspend fun updateWishlistItem(context: Context, oldItem: String, newItem: String) {
        context.dataStoreWishlist.edit { prefs ->
            val current = prefs[WISHLIST_KEY]?.toMutableSet() ?: mutableSetOf()
            if (current.remove(oldItem)) {
                current.add(newItem)
            }
            prefs[WISHLIST_KEY] = current
        }
    }
}
