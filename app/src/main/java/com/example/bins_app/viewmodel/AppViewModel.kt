package com.example.bins_app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bins_app.data.local.FriendEntity
import com.example.bins_app.data.local.TransactionEntity
import com.example.bins_app.data.repository.AppRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

data class AppUiState(
    val friends: List<FriendEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val unpaidFriendsToday: List<FriendEntity> = emptyList(),
    val collectedToday: Double = 0.0,
    val collectedThisWeek: Double = 0.0,
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedTransactionFilter: TransactionFilter = TransactionFilter.ALL
)

enum class TransactionFilter {
    ALL, PAYMENTS, LOANS
}

data class BackupData(
    val friends: List<FriendEntity>,
    val transactions: List<TransactionEntity>,
    val backupTimestamp: Long
)

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _transactionFilter = MutableStateFlow(TransactionFilter.ALL)
    val transactionFilter: StateFlow<TransactionFilter> = _transactionFilter.asStateFlow()

    val uiState: StateFlow<AppUiState> = combine(
        combine(
            repository.getAllFriends(),
            repository.getAllTransactions(),
            repository.getUnpaidFriendsToday()
        ) { friends, transactions, unpaidFriends ->
            Triple(friends, transactions, unpaidFriends)
        },
        combine(
            repository.getCollectedToday(),
            repository.getCollectedThisWeek(),
            repository.getTotalBalance()
        ) { collectedToday, collectedWeek, totalBalance ->
            Triple(collectedToday, collectedWeek, totalBalance)
        },
        _searchQuery,
        _transactionFilter
    ) { friendsData, statsData, query, filter ->
        val (friends, transactions, unpaidFriends) = friendsData
        val (collectedToday, collectedWeek, totalBalance) = statsData

        val filteredFriends = if (query.isBlank()) {
            friends
        } else {
            friends.filter { it.name.contains(query, ignoreCase = true) }
        }

        val filteredTransactions = when (filter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.PAYMENTS -> transactions.filter { it.type == "PAYMENT" }
            TransactionFilter.LOANS -> transactions.filter { it.type == "LOAN" }
        }

        AppUiState(
            friends = filteredFriends,
            transactions = filteredTransactions,
            unpaidFriendsToday = unpaidFriends,
            collectedToday = collectedToday,
            collectedThisWeek = collectedWeek,
            totalBalance = totalBalance,
            isLoading = false,
            searchQuery = query,
            selectedTransactionFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppUiState()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTransactionFilter(filter: TransactionFilter) {
        _transactionFilter.value = filter
    }

    fun addFriend(name: String, initialBalance: Double = 0.0) {
        viewModelScope.launch {
            val friend = FriendEntity(
                name = name,
                totalBalance = initialBalance,
                lastPaymentDate = null,
                isArchived = false
            )
            repository.insertFriend(friend)
        }
    }

    fun updateFriend(friend: FriendEntity) {
        viewModelScope.launch {
            repository.updateFriend(friend)
        }
    }

    fun deleteFriend(friend: FriendEntity) {
        viewModelScope.launch {
            repository.deleteFriend(friend)
        }
    }

    fun addTransaction(
        friendId: Int,
        amount: Double,
        type: String,
        notes: String = "",
        claimedBy: String = ""
    ) {
        viewModelScope.launch {
            repository.addTransactionAndUpdateBalance(
                friendId = friendId,
                amount = amount,
                type = type,
                notes = notes,
                claimedBy = claimedBy
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetAllData()
        }
    }

    // Backup data to JSON
    suspend fun exportDataToJson(): String {
        val friends = repository.getAllFriendsSnapshot()
        val transactions = repository.getAllTransactionsSnapshot()

        val backupData = BackupData(
            friends = friends,
            transactions = transactions,
            backupTimestamp = System.currentTimeMillis()
        )

        return Gson().toJson(backupData)
    }

    // Restore data from JSON
    fun importDataFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.readText()
                reader.close()

                val backupData = Gson().fromJson<BackupData>(
                    jsonString,
                    object : TypeToken<BackupData>() {}.type
                )

                // Clear existing data
                repository.resetAllData()

                // Insert friends first (to get proper IDs)
                val friendIdMap = mutableMapOf<Int, Int>()
                backupData.friends.forEach { friend ->
                    val newId = repository.insertFriend(friend.copy(id = 0)).toInt()
                    friendIdMap[friend.id] = newId
                }

                // Insert transactions with updated friend IDs
                backupData.transactions.forEach { transaction ->
                    val newFriendId = friendIdMap[transaction.friendId] ?: transaction.friendId
                    repository.insertTransaction(transaction.copy(id = 0, friendId = newFriendId))
                }

                Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun createExportIntent(jsonData: String): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "vince_app_backup_${System.currentTimeMillis()}.json")
        }
        return intent
    }
}
