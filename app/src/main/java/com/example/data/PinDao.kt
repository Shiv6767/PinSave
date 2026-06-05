package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinDao {
    @Query("SELECT * FROM pins WHERE status = :status ORDER BY timestamp DESC")
    fun getPinsByStatus(status: String): Flow<List<PinItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPin(pin: PinItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPins(pins: List<PinItem>)

    @Query("DELETE FROM pins WHERE id = :id")
    suspend fun deletePinById(id: String)

    @Query("DELETE FROM pins WHERE status = :status")
    suspend fun deletePinsByStatus(status: String)
}
