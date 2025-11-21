package com.example.bins_app.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val monthDayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val fullDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val fullDateTimeFormat = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return monthDayFormat.format(Date(timestamp))
    }

    fun formatFullDate(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return fullDateTimeFormat.format(Date(timestamp))
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        return format.format(amount).replace("PHP", "₱")
    }

    fun isToday(timestamp: Long): Boolean {
        val date = Date(timestamp)
        val today = Date()

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return dateFormat.format(date) == dateFormat.format(today)
    }
}

