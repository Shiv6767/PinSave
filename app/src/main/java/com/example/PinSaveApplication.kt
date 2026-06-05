package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.PinRepository

class PinSaveApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { PinRepository(database.pinDao()) }
}
