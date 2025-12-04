package com.example.financeapplication.classes

import androidx.room.*

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes")
    suspend fun getAll(): List<Income>

    @Insert
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)
}
