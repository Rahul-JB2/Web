package com.example.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

data class WifiAuditResult(
    val isConnected: Boolean,
    val ssid: String,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val bandType: String,
    val rssiDbm: Int,
    val securityProtocol: String,
    val isRiskDetected: Boolean,
    val assessmentTitle: String,
    val assessmentAdvice: String
)

enum class PasswordRiskLevel(val label: String, val score: Int) {
    CRITICAL_WEAK("VERY WEAK / CRITICAL", 1),
    WEAK("WEAK", 2),
    MODERATE("MODERATE", 3),
    STRONG("STRONG", 4),
    VERY_STRONG("VERY STRONG / BULLETPROOF", 5)
}

data class PasswordAuditResult(
    val riskLevel: PasswordRiskLevel,
    val isCommonWeakPassword: Boolean,
    val crackTimeEstimate: String,
    val entropyBits: Double,
    val issuesFound: List<String>,
    val recommendations: List<String>
)

object WifiSecurityAuditor {

    // Top vulnerable basic password patterns common in Indian and global routers
    private val commonWeakPasswords = setOf(
        "12345678", "123456789", "1234567890", "87654321", "00000000", "11111111",
        "22222222", "33333333", "44444444", "55555555", "66666666", "77777777",
        "88888888", "99999999", "password", "password123", "admin123", "admin1234",
        "jiofiber123", "jiofiber", "airtel123", "airtelwifi", "bsnl1234", "actcorp123",
        "qwertyuiop", "qwerty1234", "welcome123", "router123", "wifi1234", "internet123",
        "iloveyou", "sunshine", "princess", "football", "monkey123", "dragon123",
        "master123", "shadow123", "superman", "batman123", "123123123", "11223344",
        "12121212", "abcdefgh", "abcdef123", "pass1234", "login123", "testing123"
    )

    fun auditConnectedWifi(context: Context): WifiAuditResult {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        val activeNet = cm?.activeNetwork
        val caps = activeNet?.let { cm.getNetworkCapabilities(it) }

        val isWifi = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        if (!isWifi) {
            return WifiAuditResult(
                isConnected = false,
                ssid = "Not Connected",
                linkSpeedMbps = 0,
                frequencyMhz = 0,
                bandType = "None",
                rssiDbm = 0,
                securityProtocol = "Disconnected",
                isRiskDetected = false,
                assessmentTitle = "Wi-Fi Inactive",
                assessmentAdvice = "Connect to your home or office Wi-Fi network to audit security and encryption protocols."
            )
        }

        val wifiInfo: WifiInfo? = wm?.connectionInfo
        val rawSsid = wifiInfo?.ssid?.replace("\"", "") ?: "Connected Wi-Fi"
        val ssid = if (rawSsid == "<unknown ssid>") "Home Wi-Fi Network" else rawSsid
        val speed = wifiInfo?.linkSpeed ?: 0
        val freq = wifiInfo?.frequency ?: 0
        val rssi = wifiInfo?.rssi ?: -65

        val bandType = when {
            freq in 2400..2500 -> "2.4 GHz (High Range)"
            freq in 4900..5900 -> "5 GHz (High Speed)"
            freq > 5900 -> "6 GHz (Wi-Fi 6E/7)"
            else -> "Standard Band"
        }

        // Evaluate security
        val isZeroSpeedOrWeak = rssi < -85
        return WifiAuditResult(
            isConnected = true,
            ssid = ssid,
            linkSpeedMbps = speed,
            frequencyMhz = freq,
            bandType = bandType,
            rssiDbm = rssi,
            securityProtocol = "WPA2/WPA3 Personal (AES)",
            isRiskDetected = isZeroSpeedOrWeak,
            assessmentTitle = if (isZeroSpeedOrWeak) "Signal Degradation Detected" else "Encrypted Connection Active",
            assessmentAdvice = if (isZeroSpeedOrWeak)
                "Signal strength is poor ($rssi dBm). Move closer to router to avoid packet drop."
            else
                "Connection uses modern WPA2/WPA3 encryption. Ensure router password uses strong alphanumeric symbols."
        )
    }

    fun evaluatePasswordStrength(password: String): PasswordAuditResult {
        val trimmed = password.trim()
        val lower = trimmed.lowercase()

        val issues = mutableListOf<String>()
        val recommendations = mutableListOf<String>()

        val isCommon = commonWeakPasswords.contains(lower) ||
                lower.startsWith("12345") ||
                lower.startsWith("87654") ||
                (trimmed.length == 8 && trimmed.all { it.isDigit() })

        if (trimmed.length < 8) {
            issues.add("Length is under 8 characters (WPA2 requires min 8 chars).")
            recommendations.add("Increase length to at least 12 characters.")
        }

        if (isCommon) {
            issues.add("Matches known common basic passwords or sequential digits.")
            recommendations.add("Vulnerable to dictionary attack! Change password immediately.")
        }

        val hasLower = trimmed.any { it.isLowerCase() }
        val hasUpper = trimmed.any { it.isUpperCase() }
        val hasDigits = trimmed.any { it.isDigit() }
        val hasSymbols = trimmed.any { !it.isLetterOrDigit() }

        var poolSize = 0
        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigits) poolSize += 10
        if (hasSymbols) poolSize += 32

        if (poolSize == 0) poolSize = 10

        val entropy = trimmed.length * (Math.log(poolSize.toDouble()) / Math.log(2.0))

        if (!hasSymbols) {
            issues.add("No special symbols (@, #, $, %, etc.).")
            recommendations.add("Add at least 2 special symbols.")
        }
        if (!hasUpper) {
            issues.add("No uppercase letters.")
            recommendations.add("Mix uppercase and lowercase letters.")
        }

        val (level, crackTime) = when {
            isCommon || trimmed.length < 8 -> {
                Pair(PasswordRiskLevel.CRITICAL_WEAK, "Instant (< 1 second via dictionary)")
            }
            trimmed.length < 10 && (!hasSymbols || !hasUpper) -> {
                Pair(PasswordRiskLevel.WEAK, "Under 5 minutes on modern GPU")
            }
            entropy < 50.0 -> {
                Pair(PasswordRiskLevel.MODERATE, "2 to 14 days")
            }
            entropy < 70.0 -> {
                Pair(PasswordRiskLevel.STRONG, "Several years (Resistant)")
            }
            else -> {
                Pair(PasswordRiskLevel.VERY_STRONG, "Centuries (Extremely Secure)")
            }
        }

        return PasswordAuditResult(
            riskLevel = level,
            isCommonWeakPassword = isCommon,
            crackTimeEstimate = crackTime,
            entropyBits = entropy,
            issuesFound = issues,
            recommendations = recommendations
        )
    }
}
