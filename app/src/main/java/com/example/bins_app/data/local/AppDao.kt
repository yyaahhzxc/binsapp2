package com.example.bins_app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Friend queries
    @Query("SELECT * FROM friends WHERE is_archived = 0 ORDER BY name ASC")
    fun getAllFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :friendId")
    suspend fun getFriendById(friendId: Int): FriendEntity?

    @Query("SELECT * FROM friends WHERE id = :friendId")
    fun getFriendByIdFlow(friendId: Int): Flow<FriendEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Delete
    suspend fun deleteFriend(friend: FriendEntity)

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()

    // Transaction queries
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE friend_id = :friendId ORDER BY timestamp DESC")
    fun getTransactionsByFriend(friendId: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY timestamp DESC")
    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // Dashboard queries
    @Query("SELECT COALESCE(SUM(total_balance), 0.0) FROM friends WHERE is_archived = 0")
    fun getTotalBalance(): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = 'PAYMENT' 
        AND timestamp >= :startOfDay 
        AND timestamp < :endOfDay
    """)
    fun getCollectedToday(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) 
        FROM transactions 
        WHERE type = 'PAYMENT' 
        AND timestamp >= :startOfWeek
    """)
    fun getCollectedThisWeek(startOfWeek: Long): Flow<Double>

    @Query("""
        SELECT f.* FROM friends f
        WHERE f.is_archived = 0 
        AND (f.last_payment_date IS NULL 
             OR f.last_payment_date < :startOfDay)
        AND f.total_balance > 0
        ORDER BY f.total_balance DESC
    """)
    fun getUnpaidFriendsToday(startOfDay: Long): Flow<List<FriendEntity>>

    // Atomic transaction with balance update
    @Transaction
    suspend fun addTransactionAndUpdateBalance(
        transaction: TransactionEntity,
        friendId: Int,
        newBalance: Double,
        newLastPaymentDate: Long?
    ) {
        insertTransaction(transaction)
        val friend = getFriendById(friendId)
        if (friend != null) {
            updateFriend(
                friend.copy(
                    totalBalance = newBalance,
                    lastPaymentDate = newLastPaymentDate
                )
            )
        }
    }
}

