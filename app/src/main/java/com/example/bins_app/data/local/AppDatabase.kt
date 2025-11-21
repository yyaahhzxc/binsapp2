package com.example.bins_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FriendEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vince_app_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.appDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(dao: AppDao) {
            // Pre-populate with 3 dummy friends
            val currentTime = System.currentTimeMillis()
            val oneDayAgo = currentTime - (24 * 60 * 60 * 1000)
            val twoDaysAgo = currentTime - (2 * 24 * 60 * 60 * 1000)

            val vince = FriendEntity(
                name = "Vince",
                totalBalance = 2000.0,
                lastPaymentDate = oneDayAgo,
                isArchived = false
            )
            val vinceId = dao.insertFriend(vince).toInt()

            val josh = FriendEntity(
                name = "Josh",
                totalBalance = 1500.0,
                lastPaymentDate = twoDaysAgo,
                isArchived = false
            )
            val joshId = dao.insertFriend(josh).toInt()

            val sarah = FriendEntity(
                name = "Sarah",
                totalBalance = 500.0,
                lastPaymentDate = null,
                isArchived = false
            )
            val sarahId = dao.insertFriend(sarah).toInt()

            // Add 2 sample transactions
            dao.insertTransaction(
                TransactionEntity(
                    friendId = vinceId,
                    amount = 500.0,
                    type = "PAYMENT",
                    timestamp = oneDayAgo,
                    notes = "Initial payment",
                    claimedBy = "Vince"
                )
            )

            dao.insertTransaction(
                TransactionEntity(
                    friendId = joshId,
                    amount = 300.0,
                    type = "PAYMENT",
                    timestamp = twoDaysAgo,
                    notes = "Weekly payment",
                    claimedBy = "Josh"
                )
            )
        }
    }
}

