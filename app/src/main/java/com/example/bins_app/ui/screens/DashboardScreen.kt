package com.example.bins_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bins_app.ui.components.DashboardStatsCard
import com.example.bins_app.ui.components.FriendListItem
import com.example.bins_app.ui.components.VinceInputField
import com.example.bins_app.util.DateUtils
import com.example.bins_app.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToPerson: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var selectedFriendId by remember { mutableStateOf<Int?>(null) }
    var selectedFriendName by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTransactionSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Today's collection stats
            item {
                DashboardStatsCard(
                    title = "Total Collected Today",
                    amount = uiState.collectedToday,
                    subtitle = DateUtils.formatFullDate(System.currentTimeMillis())
                )
            }

            // This week's stats
            item {
                DashboardStatsCard(
                    title = "Total Collected This Week",
                    amount = uiState.collectedThisWeek,
                    subtitle = "Monday - ${DateUtils.formatDate(System.currentTimeMillis())}"
                )
            }

            // Unpaid today header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unpaid Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Unpaid friends list
            if (uiState.unpaidFriendsToday.isEmpty()) {
                item {
                    Text(
                        text = "All friends have paid today! 🎉",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(uiState.unpaidFriendsToday, key = { it.id }) { friend ->
                    FriendListItem(
                        friend = friend,
                        onClick = {
                            selectedFriendId = friend.id
                            selectedFriendName = friend.name
                            showAddTransactionSheet = true
                        }
                    )
                }
            }
        }
    }

    // Add Transaction Bottom Sheet
    if (showAddTransactionSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddTransactionSheet = false
                selectedFriendId = null
                selectedFriendName = ""
            },
            sheetState = sheetState
        ) {
            AddTransactionSheet(
                viewModel = viewModel,
                friends = uiState.friends,
                preselectedFriendId = selectedFriendId,
                preselectedFriendName = selectedFriendName,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showAddTransactionSheet = false
                        selectedFriendId = null
                        selectedFriendName = ""
                    }
                }
            )
        }
    }
}

@Composable
fun AddTransactionSheet(
    viewModel: AppViewModel,
    friends: List<com.example.bins_app.data.local.FriendEntity>,
    preselectedFriendId: Int?,
    preselectedFriendName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var personName by remember { mutableStateOf(preselectedFriendName) }
    var amount by remember { mutableStateOf("") }
    var claimedBy by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add New Payment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        VinceInputField(
            value = personName,
            onValueChange = { personName = it },
            label = "Person Name",
            placeholder = preselectedFriendName.ifBlank { "Vince" }
        )

        VinceInputField(
            value = amount,
            onValueChange = { amount = it },
            label = "Payment",
            placeholder = "₱ 500.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        VinceInputField(
            value = claimedBy,
            onValueChange = { claimedBy = it },
            label = "Claimed by",
            placeholder = "Yah"
        )

        VinceInputField(
            value = notes,
            onValueChange = { notes = it },
            label = "Notes",
            placeholder = "Notes",
            maxLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (personName.isNotBlank() && amountValue > 0) {
                        // Find or create friend
                        val existingFriend = friends.find {
                            it.name.equals(personName, ignoreCase = true)
                        }

                        if (existingFriend != null) {
                            viewModel.addTransaction(
                                friendId = existingFriend.id,
                                amount = amountValue,
                                type = "PAYMENT",
                                notes = notes,
                                claimedBy = claimedBy
                            )
                        } else {
                            // Create new friend first, then add transaction
                            viewModel.addFriend(personName, 0.0)
                            // Note: In production, we'd need to wait for the friend ID
                            // For now, we'll just add the friend
                        }

                        onDismiss()
                    }
                }
            ) {
                Text("Add Payment")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

