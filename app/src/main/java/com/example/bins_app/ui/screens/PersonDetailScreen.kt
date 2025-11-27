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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bins_app.ui.components.DashboardStatsCard
import com.example.bins_app.ui.components.TransactionListItem
import com.example.bins_app.ui.components.VinceFAB
import com.example.bins_app.ui.components.VinceInputField
import com.example.bins_app.util.DateUtils
import com.example.bins_app.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    friendId: Int,
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val friend = uiState.friends.find { it.id == friendId }
    val transactions = uiState.transactions.filter { it.friendId == friendId }

    val scope = rememberCoroutineScope()
    var showEditSheet by remember { mutableStateOf(false) }
    var showAddPaymentSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friend?.name ?: "Person") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            VinceFAB(
                mainText = "Add Payment",
                mainIcon = Icons.Default.Add,
                onMainClick = { showAddPaymentSheet = true }
            )
        }
    ) { paddingValues ->
        if (friend == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Person not found")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance card
                item {
                    DashboardStatsCard(
                        title = "Total Balance",
                        amount = friend.totalBalance,
                        subtitle = if (friend.lastPaymentDate != null) {
                            "Last paid: ${DateUtils.formatDate(friend.lastPaymentDate)}"
                        } else {
                            "No payments yet"
                        }
                    )
                }

                // Filter tabs
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = true,
                            onClick = { },
                            label = { Text("Today") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("This Week") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("This Month") }
                        )
                        FilterChip(
                            selected = false,
                            onClick = { },
                            label = { Text("All") }
                        )
                    }
                }

                // Payment summary header
                item {
                    Text(
                        text = "Payment Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Transactions list
                if (transactions.isEmpty()) {
                    item {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionListItem(
                            friendName = friend.name,
                            amount = transaction.amount,
                            type = transaction.type,
                            timestamp = transaction.timestamp,
                            notes = transaction.notes,
                            claimedBy = transaction.claimedBy,
                            onClick = { }
                        )
                    }
                }
            }
        }
    }

    // Edit Person Sheet
    if (showEditSheet && friend != null) {
        ModalBottomSheet(
            onDismissRequest = { showEditSheet = false },
            sheetState = sheetState
        ) {
            EditPersonSheet(
                friend = friend,
                viewModel = viewModel,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showEditSheet = false
                    }
                }
            )
        }
    }

    // Add Payment Sheet
    if (showAddPaymentSheet && friend != null) {
        ModalBottomSheet(
            onDismissRequest = { showAddPaymentSheet = false },
            sheetState = sheetState
        ) {
            AddPaymentForPersonSheet(
                friend = friend,
                viewModel = viewModel,
                onDismiss = {
                    scope.launch {
                        sheetState.hide()
                        showAddPaymentSheet = false
                    }
                }
            )
        }
    }
}

@Composable
fun EditPersonSheet(
    friend: com.example.bins_app.data.local.FriendEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var personName by remember { mutableStateOf(friend.name) }
    var notes by remember { mutableStateOf("") }
    var showPerson by remember { mutableStateOf(!friend.isArchived) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Person Info",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        VinceInputField(
            value = personName,
            onValueChange = { personName = it },
            label = "Person Name",
            placeholder = friend.name
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show Person",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = showPerson,
                onCheckedChange = { showPerson = it }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }

            TextButton(
                onClick = {
                    if (personName.isNotBlank()) {
                        viewModel.updateFriend(
                            friend.copy(
                                name = personName,
                                isArchived = !showPerson
                            )
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Confirm")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AddPaymentForPersonSheet(
    friend: com.example.bins_app.data.local.FriendEntity,
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = "Add Payment",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Person: ${friend.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    if (amountValue > 0) {
                        viewModel.addTransaction(
                            friendId = friend.id,
                            amount = amountValue,
                            type = "PAYMENT",
                            notes = notes,
                            claimedBy = claimedBy
                        )
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
