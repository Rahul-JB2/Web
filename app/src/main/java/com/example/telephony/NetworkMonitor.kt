package com.example.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthNr
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.data.NetworkEventLog
import com.example.data.NetworkLogRepository
import com.example.model.CellTowerInfo
import com.example.model.NetworkGeneration
import com.example.model.SignalMetrics
import com.example.model.SignalQuality
import com.example.model.TelephonyStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NetworkMonitor(
    private val context: Context,
    private val logRepository: NetworkLogRepository? = null
) {

    private val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _status = MutableStateFlow(TelephonyStatus())
    val status: StateFlow<TelephonyStatus> = _status.asStateFlow()

    private val _isForce5gEnabled = MutableStateFlow(false)
    val isForce5gEnabled: StateFlow<Boolean> = _isForce5gEnabled.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    private var telephonyCallback: Any? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var lastRecordedGen: NetworkGeneration = NetworkGeneration.UNKNOWN
    private var lastRecordedBand: String? = null

    init {
        refreshBasicInfo()
        registerNetworkCallback()
        registerTelephonyListener()
        // Log initial state
        scope.launch {
            logRepository?.logEvent(
                NetworkEventLog(
                    eventType = "INITIAL_STATE",
                    previousNetwork = "None",
                    newNetwork = _status.value.networkGeneration.displayName,
                    operatorName = _status.value.operatorName,
                    description = "Initial cellular diagnostic session established on ${_status.value.operatorName}"
                )
            )
        }
    }

    fun hasPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val phoneState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation && phoneState
    }

    fun setForce5gMode(enabled: Boolean) {
        _isForce5gEnabled.value = enabled
        scope.launch {
            logRepository?.logEvent(
                NetworkEventLog(
                    eventType = "FORCE_5G_TOGGLED",
                    previousNetwork = if (enabled) "Auto Mode" else "5G Force Locked",
                    newNetwork = if (enabled) "5G Force (NR Locked)" else "Auto (NR/LTE/GSM)",
                    operatorName = _status.value.operatorName,
                    rsrpDbm = _status.value.signalMetrics.rsrp,
                    sinrDb = _status.value.signalMetrics.sinr,
                    description = if (enabled)
                        "User enabled 5G Force Mode toggle: Locking to NR High-Speed bands."
                    else
                        "User disabled 5G Force Mode toggle: Restoring standard network switching."
                )
            )
        }
    }

    fun refreshAll() {
        refreshBasicInfo()
        if (hasPermissions()) {
            queryCellInfo()
        }
    }

    private fun refreshBasicInfo() {
        val tm = telephonyManager ?: return
        val current = _status.value

        val networkOperator = tm.networkOperatorName.ifBlank {
            tm.simOperatorName.ifBlank { "Unknown Carrier" }
        }
        val simOperator = tm.simOperatorName.ifBlank { "SIM" }

        val dataStateStr = when (tm.dataState) {
            TelephonyManager.DATA_CONNECTED -> "Connected"
            TelephonyManager.DATA_CONNECTING -> "Connecting..."
            TelephonyManager.DATA_SUSPENDED -> "Suspended"
            TelephonyManager.DATA_DISCONNECTED -> "Disconnected"
            else -> "Unknown"
        }

        val simStateStr = when (tm.simState) {
            TelephonyManager.SIM_STATE_READY -> "SIM Ready"
            TelephonyManager.SIM_STATE_ABSENT -> "No SIM Card"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            else -> "Not Ready"
        }

        val generation = determineNetworkGeneration()

        if (lastRecordedGen != NetworkGeneration.UNKNOWN && lastRecordedGen != generation) {
            val prev = lastRecordedGen
            lastRecordedGen = generation
            scope.launch {
                logRepository?.logEvent(
                    NetworkEventLog(
                        eventType = "NETWORK_CHANGE",
                        previousNetwork = prev.displayName,
                        newNetwork = generation.displayName,
                        operatorName = networkOperator,
                        rsrpDbm = current.signalMetrics.rsrp,
                        sinrDb = current.signalMetrics.sinr,
                        description = "Network transition: Shifted from ${prev.displayName} to ${generation.displayName}"
                    )
                )
            }
        } else if (lastRecordedGen == NetworkGeneration.UNKNOWN) {
            lastRecordedGen = generation
        }

        _status.value = current.copy(
            operatorName = networkOperator,
            simOperator = simOperator,
            dataState = dataStateStr,
            isDataConnected = tm.dataState == TelephonyManager.DATA_CONNECTED,
            networkGeneration = generation,
            simState = simStateStr,
            roaming = tm.isNetworkRoaming
        )
    }

    @SuppressLint("MissingPermission")
    private fun determineNetworkGeneration(): NetworkGeneration {
        val cm = connectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = activeNetwork?.let { cm.getNetworkCapabilities(it) }

        val tm = telephonyManager ?: return NetworkGeneration.UNKNOWN

        val networkType = try {
            if (hasPermissions()) tm.dataNetworkType else TelephonyManager.NETWORK_TYPE_UNKNOWN
        } catch (_: SecurityException) {
            TelephonyManager.NETWORK_TYPE_UNKNOWN
        }

        return when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> NetworkGeneration.FIVE_G_SA
            TelephonyManager.NETWORK_TYPE_LTE -> {
                if (_status.value.isNrAvailable || _status.value.overrideNetworkType.contains("NR")) {
                    NetworkGeneration.FIVE_G_NSA
                } else {
                    NetworkGeneration.FOUR_G
                }
            }
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> NetworkGeneration.THREE_G
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS -> NetworkGeneration.TWO_G
            else -> {
                if (_status.value.isNrAvailable) NetworkGeneration.FIVE_G_NSA
                else if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) NetworkGeneration.WIFI
                else NetworkGeneration.UNKNOWN
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun queryCellInfo() {
        val tm = telephonyManager ?: return
        if (!hasPermissions()) return

        scope.launch(Dispatchers.IO) {
            try {
                val cellList: List<CellInfo>? = tm.allCellInfo
                if (cellList.isNullOrEmpty()) {
                    return@launch
                }

                var primaryCell: CellTowerInfo? = null
                val towers = mutableListOf<CellTowerInfo>()
                var bestSignal = _status.value.signalMetrics

                for (cell in cellList) {
                    when (cell) {
                        is CellInfoNr -> {
                            val id = cell.cellIdentity as? CellIdentityNr
                            val ss = cell.cellSignalStrength as? CellSignalStrengthNr

                            val arfcn = id?.nrarfcn?.takeIf { it != CellInfo.UNAVAILABLE }
                            val (bandName, bandFreq) = if (arfcn != null) {
                                BandCalculator.getNrBandFromArfcn(arfcn)
                            } else Pair("5G NR Band", "Unknown Freq")

                            val tower = CellTowerInfo(
                                cellId = id?.nci?.takeIf { it != CellInfo.UNAVAILABLE_LONG },
                                pci = id?.pci?.takeIf { it != CellInfo.UNAVAILABLE },
                                tac = id?.tac?.takeIf { it != CellInfo.UNAVAILABLE },
                                arfcn = arfcn,
                                bandName = bandName,
                                bandFrequency = bandFreq,
                                mcc = id?.mccString,
                                mnc = id?.mncString,
                                isRegistered = cell.isRegistered
                            )
                            towers.add(tower)

                            if (cell.isRegistered || primaryCell == null) {
                                primaryCell = tower
                                if (ss != null) {
                                    val rsrp = ss.ssRsrp.takeIf { it != CellInfo.UNAVAILABLE }
                                    val rsrq = ss.ssRsrq.takeIf { it != CellInfo.UNAVAILABLE }
                                    val sinr = ss.ssSinr.takeIf { it != CellInfo.UNAVAILABLE }
                                    val csiRsrp = ss.csiRsrp.takeIf { it != CellInfo.UNAVAILABLE }
                                    val csiRsrq = ss.csiRsrq.takeIf { it != CellInfo.UNAVAILABLE }
                                    val csiSinr = ss.csiSinr.takeIf { it != CellInfo.UNAVAILABLE }

                                    val level = ss.level
                                    val dbm = ss.dbm.takeIf { it != CellInfo.UNAVAILABLE } ?: (rsrp ?: -100)

                                    bestSignal = SignalMetrics(
                                        dbm = dbm,
                                        level = level,
                                        quality = mapLevelToQuality(level),
                                        rsrp = rsrp,
                                        rsrq = rsrq,
                                        sinr = sinr,
                                        csiRsrp = csiRsrp,
                                        csiRsrq = csiRsrq,
                                        csiSinr = csiSinr,
                                        asu = ss.asuLevel
                                    )
                                }
                            }
                        }

                        is CellInfoLte -> {
                            val id = cell.cellIdentity as? CellIdentityLte
                            val ss = cell.cellSignalStrength as? CellSignalStrengthLte

                            val earfcn = id?.earfcn?.takeIf { it != CellInfo.UNAVAILABLE }
                            val (bandName, bandFreq) = if (earfcn != null) {
                                BandCalculator.getLteBandFromEarfcn(earfcn)
                            } else Pair("4G LTE", "Standard")

                            val tower = CellTowerInfo(
                                cellId = id?.ci?.takeIf { it != CellInfo.UNAVAILABLE }?.toLong(),
                                pci = id?.pci?.takeIf { it != CellInfo.UNAVAILABLE },
                                tac = id?.tac?.takeIf { it != CellInfo.UNAVAILABLE },
                                arfcn = earfcn,
                                bandName = bandName,
                                bandFrequency = bandFreq,
                                mcc = id?.mccString,
                                mnc = id?.mncString,
                                isRegistered = cell.isRegistered
                            )
                            towers.add(tower)

                            if ((cell.isRegistered && primaryCell == null) || primaryCell == null) {
                                primaryCell = tower
                                if (ss != null) {
                                    val rsrp = ss.rsrp.takeIf { it != CellInfo.UNAVAILABLE }
                                    val rsrq = ss.rsrq.takeIf { it != CellInfo.UNAVAILABLE }
                                    val sinr = ss.rssnr.takeIf { it != CellInfo.UNAVAILABLE }
                                    val level = ss.level

                                    bestSignal = SignalMetrics(
                                        dbm = ss.dbm.takeIf { it != CellInfo.UNAVAILABLE } ?: (rsrp ?: -100),
                                        level = level,
                                        quality = mapLevelToQuality(level),
                                        rsrp = rsrp,
                                        rsrq = rsrq,
                                        sinr = sinr,
                                        asu = ss.asuLevel
                                    )
                                }
                            }
                        }
                    }
                }

                // Check for band transition
                primaryCell?.bandName?.let { currentBand ->
                    if (lastRecordedBand != null && lastRecordedBand != currentBand) {
                        val oldBand = lastRecordedBand
                        lastRecordedBand = currentBand
                        scope.launch {
                            logRepository?.logEvent(
                                NetworkEventLog(
                                    eventType = "BAND_TRANSITION",
                                    previousNetwork = _status.value.networkGeneration.displayName,
                                    newNetwork = _status.value.networkGeneration.displayName,
                                    previousBand = oldBand,
                                    newBand = currentBand,
                                    operatorName = _status.value.operatorName,
                                    rsrpDbm = bestSignal.rsrp,
                                    sinrDb = bestSignal.sinr,
                                    description = "Cell handoff: Band transitioned from $oldBand to $currentBand"
                                )
                            )
                        }
                    } else if (lastRecordedBand == null) {
                        lastRecordedBand = currentBand
                    }
                }

                _status.value = _status.value.copy(
                    cellTower = primaryCell ?: _status.value.cellTower,
                    allCells = towers,
                    signalMetrics = bestSignal
                )
            } catch (_: Exception) {}
        }
    }

    /**
     * Helper to add a diagnostic test event into the Room database.
     */
    fun addManualDiagnosticLog(type: String, desc: String, oldBand: String? = null, newBand: String? = null) {
        scope.launch {
            logRepository?.logEvent(
                NetworkEventLog(
                    eventType = type,
                    previousNetwork = _status.value.networkGeneration.displayName,
                    newNetwork = if (type == "BAND_TRANSITION") "5G NR" else _status.value.networkGeneration.displayName,
                    previousBand = oldBand ?: _status.value.cellTower.bandName ?: "LTE B3",
                    newBand = newBand ?: "5G NR n78 (3500 MHz)",
                    operatorName = _status.value.operatorName,
                    rsrpDbm = _status.value.signalMetrics.rsrp ?: -88,
                    sinrDb = _status.value.signalMetrics.sinr ?: 18,
                    description = desc
                )
            )
        }
    }

    private fun mapLevelToQuality(level: Int): SignalQuality {
        return when (level) {
            4 -> SignalQuality.EXCELLENT
            3 -> SignalQuality.GOOD
            2 -> SignalQuality.FAIR
            1 -> SignalQuality.POOR
            else -> SignalQuality.NO_SIGNAL
        }
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch { refreshBasicInfo() }
            }

            override fun onLost(network: Network) {
                scope.launch { refreshBasicInfo() }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                scope.launch { refreshBasicInfo() }
            }
        }

        try {
            cm.registerNetworkCallback(request, networkCallback!!)
        } catch (_: Exception) {}
    }

    private fun registerTelephonyListener() {
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val callback = object : TelephonyCallback(),
                TelephonyCallback.DisplayInfoListener,
                TelephonyCallback.SignalStrengthsListener,
                TelephonyCallback.DataConnectionStateListener {

                override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyDisplayInfo) {
                    val overrideType = telephonyDisplayInfo.overrideNetworkType
                    val overrideDesc = when (overrideType) {
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> "5G NSA"
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> "5G+ Ultra Wideband (mmWave/C-Band)"
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_ADVANCED_PRO -> "4G LTE Advanced Pro"
                        TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_LTE_CA -> "4G LTE Carrier Aggregation"
                        else -> "Standard"
                    }

                    val is5G = overrideType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA ||
                            overrideType == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED

                    _status.value = _status.value.copy(
                        overrideNetworkType = overrideDesc,
                        isNrAvailable = is5G,
                        networkGeneration = if (is5G) NetworkGeneration.FIVE_G_NSA else _status.value.networkGeneration
                    )
                }

                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    val level = signalStrength.level
                    _status.value = _status.value.copy(
                        signalMetrics = _status.value.signalMetrics.copy(
                            level = level,
                            quality = mapLevelToQuality(level)
                        )
                    )
                }

                override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
                    refreshBasicInfo()
                }
            }

            try {
                tm.registerTelephonyCallback(context.mainExecutor, callback)
                telephonyCallback = callback
            } catch (_: Exception) {}
        }
    }

    fun unregister() {
        try {
            networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (telephonyCallback as? TelephonyCallback)?.let {
                    telephonyManager?.unregisterTelephonyCallback(it)
                }
            }
        } catch (_: Exception) {}
    }
}
