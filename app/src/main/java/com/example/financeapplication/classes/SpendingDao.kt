import androidx.room.*

@Dao
interface SpendingDao {
    @Query("SELECT * FROM spendings")
    suspend fun getAll(): List<Spending>

    @Insert
    suspend fun insert(spending: Spending)

    @Update
    suspend fun update(spending: Spending)

    @Delete
    suspend fun delete(spending: Spending)
}
