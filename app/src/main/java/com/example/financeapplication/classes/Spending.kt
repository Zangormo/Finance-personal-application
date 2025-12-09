package com.example.financeapplication.classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

enum class NecessityLevel {
    NECESSARY,
    MEDIUM,
    NOT_NEEDED
}

@Entity(tableName = "spendings")
data class Spending(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sum: Float,
    @TypeConverters(StringListConverter::class)
    val items: List<String>,
    val necessity: NecessityLevel = NecessityLevel.NECESSARY,
    val date: String
)

