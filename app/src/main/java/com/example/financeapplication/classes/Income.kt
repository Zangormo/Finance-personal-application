package com.example.financeapplication.classes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sum: Float,
    val description: String
)
