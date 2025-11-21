package com.example.bins_app

import android.app.Application
import com.example.bins_app.data.local.AppDatabase
import com.example.bins_app.data.repository.AppRepository

class VinceApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.appDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: VinceApplication
            private set
    }
}

