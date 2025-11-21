package com.example.bins_app.data.repository

import com.example.bins_app.data.local.AppDao
import com.example.bins_app.data.local.FriendEntity
import com.example.bins_app.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AppRepository(private val dao: AppDao) {

    // Friend operations
    fun getAllFriends(): Flow<List<FriendEntity>> = dao.getAllFriends()

    fun getFriendById(friendId: Int): Flow<FriendEntity?> = dao.getFriendByIdFlow(friendId)

    suspend fun insertFriend(friend: FriendEntity): Long = dao.insertFriend(friend)

    suspend fun updateFriend(friend: FriendEntity) = dao.updateFriend(friend)

    suspend fun deleteFriend(friend: FriendEntity) = dao.deleteFriend(friend)

    // Transaction operations
    fun getAllTransactions(): Flow<List<TransactionEntity>> = dao.getAllTransactions()

    fun getTransactionsByFriend(friendId: Int): Flow<List<TransactionEntity>> =
        dao.getTransactionsByFriend(friendId)

    fun getTransactionsByType(type: String): Flow<List<TransactionEntity>> =
        dao.getTransactionsByType(type)

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        dao.insertTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        dao.deleteTransaction(transaction)

    // Dashboard operations
    fun getTotalBalance(): Flow<Double> = dao.getTotalBalance()

    fun getCollectedToday(): Flow<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return dao.getCollectedToday(startOfDay, endOfDay)
    }

    fun getCollectedThisWeek(): Flow<Double> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfWeek = calendar.timeInMillis

        return dao.getCollectedThisWeek(startOfWeek)
    }

    fun getUnpaidFriendsToday(): Flow<List<FriendEntity>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        return dao.getUnpaidFriendsToday(startOfDay)
    }

    // Atomic operation: add transaction and update friend balance
    suspend fun addTransactionAndUpdateBalance(
        friendId: Int,
        amount: Double,
        type: String,
        notes: String,
        claimedBy: String
    ) {
        val friend = dao.getFriendById(friendId) ?: return

        // Calculate new balance based on transaction type
        val newBalance = when (type) {
            "LOAN" -> friend.totalBalance + amount  // LOAN increases balance
            "PAYMENT" -> friend.totalBalance - amount  // PAYMENT decreases balance
            else -> friend.totalBalance
        }

        // Update last payment date only for PAYMENT transactions
        val newLastPaymentDate = if (type == "PAYMENT") {
            System.currentTimeMillis()
        } else {
            friend.lastPaymentDate
        }

        val transaction = TransactionEntity(
            friendId = friendId,
            amount = amount,
            type = type,
            timestamp = System.currentTimeMillis(),
            notes = notes,
            claimedBy = claimedBy
        )

        dao.addTransactionAndUpdateBalance(
            transaction = transaction,
            friendId = friendId,
            newBalance = newBalance,
            newLastPaymentDate = newLastPaymentDate
        )
    }

    // Data management operations
    suspend fun resetAllData() {
        dao.deleteAllTransactions()
        dao.deleteAllFriends()
    }

    suspend fun getAllFriendsSnapshot(): List<FriendEntity> =
        dao.getAllFriends().let { flow ->
            var result: List<FriendEntity> = emptyList()
            flow.collect { result = it }
            result
        }

    suspend fun getAllTransactionsSnapshot(): List<TransactionEntity> =
        dao.getAllTransactions().let { flow ->
            var result: List<TransactionEntity> = emptyList()
            flow.collect { result = it }
            result
        }
}

