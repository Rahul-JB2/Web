package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_logs")
data class NetworkEventLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "BAND_TRANSITION", "NETWORK_CHANGE", "FORCE_5G_TOGGLED", "SIGNAL_CHANGE"
    val previousNetwork: String,
    val newNetwork: String,
    val previousBand: String? = null,
    val newBand: String? = null,
    val operatorName: String = "Carrier",
    val rsrpDbm: Int? = null,
    val sinrDb: Int? = null,
    val description: String
)
