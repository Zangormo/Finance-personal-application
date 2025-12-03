import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Spending::class, Income::class], version = 1)
@TypeConverters(StringListConverter::class)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun spendingDao(): SpendingDao
    abstract fun incomeDao(): IncomeDao
}
