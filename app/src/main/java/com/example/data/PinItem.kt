package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pins")
data class PinItem(
    @PrimaryKey
    val id: String,
    val src: String,
    val title: String,
    val description: String,
    val domain: String,
    val type: String, // "image" or "video"
    val status: String, // "history", "active", "downloaded"
    val timestamp: Long = System.currentTimeMillis()
)
