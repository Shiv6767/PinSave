package com.example.data

import kotlinx.coroutines.flow.Flow

class PinRepository(private val pinDao: PinDao) {
    fun getHistoryPins(): Flow<List<PinItem>> = pinDao.getPinsByStatus("history")
    
    fun getDownloadedPins(): Flow<List<PinItem>> = pinDao.getPinsByStatus("downloaded")
    
    fun getActivePins(): Flow<List<PinItem>> = pinDao.getPinsByStatus("active")

    suspend fun insertPin(pin: PinItem) = pinDao.insertPin(pin)
    
    suspend fun insertPins(pins: List<PinItem>) = pinDao.insertPins(pins)

    suspend fun deletePinById(id: String) = pinDao.deletePinById(id)
    
    suspend fun clearHistory() = pinDao.deletePinsByStatus("history")
    
    suspend fun clearDownloaded() = pinDao.deletePinsByStatus("downloaded")
}
