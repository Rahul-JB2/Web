package com.example.model

enum class NetworkGeneration(val displayName: String, val is5G: Boolean) {
    FIVE_G_SA("5G Standalone (NR)", true),
    FIVE_G_NSA("5G Non-Standalone", true),
    FIVE_G_UNKNOWN("5G NR", true),
    FOUR_G("4G LTE", false),
    THREE_G("3G (HSPA/UMTS)", false),
    TWO_G("2G (GSM/EDGE)", false),
    WIFI("Wi-Fi", false),
    DISCONNECTED("No Connection", false),
    UNKNOWN("Unknown", false)
}

enum class SignalQuality(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
    NO_SIGNAL("No Signal")
}

data class SignalMetrics(
    val dbm: Int = 0,
    val level: Int = 0, // 0 to 4
    val quality: SignalQuality = SignalQuality.NO_SIGNAL,
    val rsrp: Int? = null, // dBm
    val rsrq: Int? = null, // dB
    val sinr: Int? = null, // dB
    val csiRsrp: Int? = null,
    val csiRsrq: Int? = null,
    val csiSinr: Int? = null,
    val asu: Int = 0
)

data class CellTowerInfo(
    val cellId: Long? = null,
    val pci: Int? = null, // Physical Cell ID
    val tac: Int? = null, // Tracking Area Code
    val arfcn: Int? = null, // NR-ARFCN or EARFCN
    val bandName: String? = null,
    val bandFrequency: String? = null,
    val mcc: String? = null,
    val mnc: String? = null,
    val isRegistered: Boolean = false
)

data class TelephonyStatus(
    val operatorName: String = "Unknown Carrier",
    val simOperator: String = "Unknown",
    val dataState: String = "Disconnected",
    val isDataConnected: Boolean = false,
    val networkGeneration: NetworkGeneration = NetworkGeneration.UNKNOWN,
    val overrideNetworkType: String = "",
    val isNrAvailable: Boolean = false,
    val isDualConnectivityNr: Boolean = false,
    val simState: String = "Ready",
    val roaming: Boolean = false,
    val signalMetrics: SignalMetrics = SignalMetrics(),
    val cellTower: CellTowerInfo = CellTowerInfo(),
    val allCells: List<CellTowerInfo> = emptyList()
)

data class SpeedTestResult(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val downloadSpeedMbps: Double,
    val pingMs: Long,
    val jitterMs: Long,
    val networkType: String,
    val carrierName: String
)
