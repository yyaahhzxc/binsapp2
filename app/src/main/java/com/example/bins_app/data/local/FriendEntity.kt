package com.example.bins_app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "total_balance")
    val totalBalance: Double = 0.0,

    @ColumnInfo(name = "last_payment_date")
    val lastPaymentDate: Long? = null,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)

