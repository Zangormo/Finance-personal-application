package com.example.financeapplication.classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "spendings")
data class Spending(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sum: Float,
    @TypeConverters(StringListConverter::class)
    val items: List<String>
)
