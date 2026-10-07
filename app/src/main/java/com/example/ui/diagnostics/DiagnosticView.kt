package com.example.ui.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NetworkEventLog
import com.example.data.NetworkLogRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.wifi.PasswordRiskLevel
import com.example.wifi.WifiSecurityAuditor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticView(
    repository: NetworkLogRepository,
    onSimulateTransition: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logs by repository.allLogs.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showWifiSecurityDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Logs?", color = Color.White) },
            text = { Text("Remove all recorded network transitions?", color = Color(0x99FFFFFF)) },
            containerColor = Color(0xFF131D31),
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repository.clearHistory() }
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = StatusRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    if (showWifiSecurityDialog) {
        WifiSecurityAuditModal(
            onDismiss = { showWifiSecurityDialog = false },
            onLogSecurityEvent = { desc, isWeak ->
                scope.launch {
                    repository.logEvent(
                        NetworkEventLog(
                            eventType = "WIFI_SECURITY_AUDIT",
                            previousNetwork = "Wi-Fi",
                            newNetwork = if (isWeak) "WEAK_WIFI" else "SECURE_WIFI",
                            operatorName = "Home Wi-Fi Router",
                            description = desc
                        )
                    )
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EVENT LOGS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
                Text(
                    text = "${logs.size} Recorded Transitions",
                    fontSize = 11.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
            }

            // Top Right Action Buttons (Includes small Wi-Fi security auditor button)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Wi-Fi Security & Password Audit Button
                IconButton(
                    onClick = { showWifiSecurityDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5FF))
                        .border(1.dp, CyanNeon, CircleShape)
                        .testTag("wifi_audit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiLock,
                        contentDescription = "Wi-Fi Security Auditor",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onSimulateTransition,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .testTag("simulate_transition_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = "Simulate",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AEF4444))
                            .border(1.dp, Color(0x33EF4444), CircleShape)
                            .testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = StatusRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events logged • Transitions show here",
                    fontSize = 12.sp,
                    color = Color(0x66FFFFFF)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(logs, key = { it.id }) { log ->
                    CleanGlassEventRow(log)
                }
            }
        }
    }
}

@Composable
private fun WifiSecurityAuditModal(
    onDismiss: () -> Unit,
    onLogSecurityEvent: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val wifiData = remember { WifiSecurityAuditor.auditConnectedWifi(context) }
    var testPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val auditResult = remember(testPassword) {
        if (testPassword.isNotBlank()) {
            WifiSecurityAuditor.evaluatePasswordStrength(testPassword)
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0x3300E5FF))
                        .border(1.dp, CyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiLock,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Wi-Fi Security Auditor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Vulnerability & Weak Password Check",
                        fontSize = 11.sp,
                        color = Color(0x99FFFFFF)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Connected Wi-Fi Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1AFFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = wifiData.ssid,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            GlassPill(
                                backgroundColor = if (wifiData.isConnected) Color(0x2210B981) else Color(0x22EF4444),
                                borderColor = if (wifiData.isConnected) StatusGreen else StatusRed
                            ) {
                                Text(
                                    text = if (wifiData.isConnected) "CONNECTED" else "OFFLINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (wifiData.isConnected) StatusGreen else StatusRed
                                )
                            }
                        }

                        if (wifiData.isConnected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${wifiData.bandType} • ${wifiData.linkSpeedMbps} Mbps • ${wifiData.securityProtocol}",
                                fontSize = 11.sp,
                                color = CyanNeon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Password Test Section
                Text(
                    text = "ROUTER PASSWORD SCANNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = Color(0x80FFFFFF)
                )
                Text(
                    text = "Checks against 100,000+ basic weak password combinations",
                    fontSize = 10.sp,
                    color = Color(0x66FFFFFF)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = testPassword,
                    onValueChange = { testPassword = it },
                    placeholder = { Text("Enter Wi-Fi password to test...", fontSize = 12.sp, color = Color(0x66FFFFFF)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = Color(0x99FFFFFF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = Color(0x0DFFFFFF),
                        unfocusedContainerColor = Color(0x0DFFFFFF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wifi_password_input")
                )

                // Audit Results Card
                if (auditResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))

                    val (riskBg, riskFg) = when (auditResult.riskLevel) {
                        PasswordRiskLevel.CRITICAL_WEAK -> Pair(Color(0x33EF4444), StatusRed)
                        PasswordRiskLevel.WEAK -> Pair(Color(0x33F59E0B), StatusAmber)
                        PasswordRiskLevel.MODERATE -> Pair(Color(0x3300E5FF), CyanNeon)
                        PasswordRiskLevel.STRONG, PasswordRiskLevel.VERY_STRONG -> Pair(Color(0x3310B981), StatusGreen)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(riskBg)
                            .border(1.dp, riskFg, RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (auditResult.isCommonWeakPassword) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = riskFg,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (auditResult.isCommonWeakPassword)
                                        "ALERT: WEAK PASSWORD! NEED TO CHANGE"
                                    else
                                        "STRENGTH: ${auditResult.riskLevel.label}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = riskFg
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Brute-force crack time: ${auditResult.crackTimeEstimate}",
                                fontSize = 10.sp,
                                color = Color.White
                            )

                            if (auditResult.issuesFound.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• ${auditResult.issuesFound.first()}",
                                    fontSize = 10.sp,
                                    color = Color(0xCCFFFFFF)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (auditResult != null) {
                Button(
                    onClick = {
                        onLogSecurityEvent(
                            "Wi-Fi Audit for '${wifiData.ssid}': ${auditResult.riskLevel.label} (${auditResult.crackTimeEstimate})",
                            auditResult.isCommonWeakPassword
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Audit Log", fontSize = 12.sp, color = Color(0xFF00363D), fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (auditResult != null) {
                TextButton(onClick = onDismiss) {
                    Text("Close", color = Color(0x99FFFFFF))
                }
            }
        }
    )
}

@Composable
private fun CleanGlassEventRow(log: NetworkEventLog) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    val (badgeText, badgeColor) = when (log.eventType) {
        "BAND_TRANSITION" -> Pair("HANDOVER", MintNeon)
        "NETWORK_CHANGE" -> Pair("NETWORK", Color(0xFF8DA4FF))
        "FORCE_5G_TOGGLED" -> Pair("FORCE 5G", CyanNeon)
        "WIFI_SECURITY_AUDIT" -> Pair("WIFI AUDIT", StatusAmber)
        else -> Pair("EVENT", Color(0xFFCBD5E1))
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GlassPill(
                        backgroundColor = badgeColor.copy(alpha = 0.15f),
                        borderColor = badgeColor.copy(alpha = 0.4f)
                    ) {
                        Text(text = badgeText, fontSize = 9.sp, fontWeight = FontWeight.Black, color = badgeColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Color(0x66FFFFFF)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (log.previousBand != null || log.newBand != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = log.previousBand ?: "Prev", fontSize = 12.sp, color = Color(0x80FFFFFF))
                        Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                        Text(text = log.newBand ?: "Next", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
                    }
                } else {
                    Text(
                        text = log.description,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            if (log.rsrpDbm != null) {
                Text(
                    text = "${log.rsrpDbm} dBm",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintNeon
                )
            }
        }
    }
}
