package com.example.bins_app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = FriendEntity::class,
            parentColumns = ["id"],
            childColumns = ["friend_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["friend_id"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "friend_id")
    val friendId: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "type")
    val type: String, // "PAYMENT" or "LOAN"

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "claimed_by")
    val claimedBy: String = ""
)

