package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkLogDao {

    @Query("SELECT * FROM network_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NetworkEventLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: NetworkEventLog)

    @Query("DELETE FROM network_logs")
    suspend fun clearAllLogs()

    @Query("SELECT * FROM network_logs WHERE eventType = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): Flow<List<NetworkEventLog>>

    @Query("SELECT COUNT(*) FROM network_logs")
    fun getLogCount(): Flow<Int>
}
