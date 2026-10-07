package com.example.data

import kotlinx.coroutines.flow.Flow

class NetworkLogRepository(private val dao: NetworkLogDao) {

    val allLogs: Flow<List<NetworkEventLog>> = dao.getAllLogs()
    val logCount: Flow<Int> = dao.getLogCount()

    fun getLogsByType(type: String): Flow<List<NetworkEventLog>> = dao.getLogsByType(type)

    suspend fun logEvent(log: NetworkEventLog) {
        dao.insertLog(log)
    }

    suspend fun clearHistory() {
        dao.clearAllLogs()
    }
}
