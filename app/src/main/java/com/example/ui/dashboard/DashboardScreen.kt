package com.example.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.Force5gLauncher
import com.example.launcher.LaunchResult
import com.example.model.NetworkGeneration
import com.example.model.TelephonyStatus
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.components.RadialSignalGauge
import com.example.ui.components.SignalBarsIndicator
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun DashboardScreen(
    status: TelephonyStatus,
    isForce5gEnabled: Boolean,
    onToggleForce5g: (Boolean) -> Unit,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    onNavigateToForce5g: () -> Unit,
    onNavigateToSpeedTest: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_beacon")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_alpha"
    )

    fun directOpenPhoneInfo() {
        val result = Force5gLauncher.openRadioInfo(context)
        when (result) {
            is LaunchResult.Success -> {
                Toast.makeText(context, "Opening Phone Info! Select 'NR only'", Toast.LENGTH_SHORT).show()
            }
            is LaunchResult.Failure -> {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "NETWORK CONTROLLER",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    color = Color.White
                )
                Text(
                    text = "Real-time Telemetry & Signal Quality",
                    fontSize = 11.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0x1AFFFFFF))
                    .border(1.dp, Color(0x2EFFFFFF), CircleShape)
                    .testTag("refresh_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Telemetry",
                    tint = CyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Current Network Technology Display Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderBrush = if (status.networkGeneration.is5G) {
                Brush.linearGradient(listOf(CyanNeon.copy(alpha = 0.8f), MintNeon.copy(alpha = 0.4f)))
            } else {
                Brush.linearGradient(listOf(Color(0x66FFFFFF), Color(0x1AFFFFFF)))
            }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Top Row: Operator + Live Connection Beacon + Bars Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (status.isDataConnected) StatusGreen.copy(alpha = beaconAlpha)
                                    else StatusRed.copy(alpha = beaconAlpha)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = status.operatorName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Cellular Signal Bars Indicator (0-4 Bars)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SignalBarsIndicator(
                            level = status.signalMetrics.level,
                            quality = status.signalMetrics.quality,
                            barWidth = 4.dp,
                            spacing = 3.dp,
                            maxHeight = 18.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${status.signalMetrics.level}/4",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0x99FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Center Highlight: Technology Badge + Subtype
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (status.networkGeneration) {
                                    NetworkGeneration.FIVE_G_SA -> "5G NR STANDALONE"
                                    NetworkGeneration.FIVE_G_NSA -> "5G NR NON-STANDALONE"
                                    NetworkGeneration.FIVE_G_UNKNOWN -> "5G NEW RADIO"
                                    NetworkGeneration.FOUR_G -> "4G LTE-ADVANCED"
                                    NetworkGeneration.THREE_G -> "3G HSPA+"
                                    NetworkGeneration.TWO_G -> "2G EDGE/GSM"
                                    NetworkGeneration.WIFI -> "WI-FI CONNECTED"
                                    NetworkGeneration.DISCONNECTED -> "DISCONNECTED"
                                    NetworkGeneration.UNKNOWN -> "SCANNING..."
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = if (status.networkGeneration.is5G) CyanNeon else Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val bandText = status.cellTower.bandName ?: (if (status.networkGeneration.is5G) "n78" else "B3")
                        val freqText = status.cellTower.bandFrequency ?: "Sub-6 GHz"
                        Text(
                            text = "Band: $bandText • $freqText",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFD6E0FF)
                        )
                    }

                    // Technology Pill
                    GlassPill(
                        backgroundColor = if (status.networkGeneration.is5G) Color(0x3300E5FF) else Color(0x334361EE),
                        borderColor = if (status.networkGeneration.is5G) Color(0x8000E5FF) else Color(0x664361EE)
                    ) {
                        Text(
                            text = if (status.networkGeneration.is5G) "5G ACTIVE" else status.networkGeneration.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (status.networkGeneration.is5G) CyanNeon else Color(0xFFD6E0FF)
                        )
                    }
                }

                if (status.overrideNetworkType.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Layer: ${status.overrideNetworkType}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MintNeon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Direct 1-Tap Button to Open Phone Info (RadioInfo) Setting
        Button(
            onClick = { directOpenPhoneInfo() },
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("direct_open_phone_info_button")
        ) {
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = null,
                tint = Color(0xFF00363D),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "OPEN PHONE INFO SETTING (DIRECT)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color(0xFF00363D)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5G Force Mode Toggle Switch
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            borderBrush = if (isForce5gEnabled) Brush.linearGradient(listOf(CyanNeon, MintNeon)) else Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF)))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isForce5gEnabled) Color(0x3300E5FF) else Color(0x1AFFFFFF))
                                .border(1.dp, if (isForce5gEnabled) CyanNeon else Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isForce5gEnabled) CyanNeon else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "5G Force Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isForce5gEnabled) "NR ONLY LOCKED" else "AUTO SWITCHING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isForce5gEnabled) CyanNeon else Color(0x99FFFFFF)
                            )
                        }
                    }

                    Switch(
                        checked = isForce5gEnabled,
                        onCheckedChange = { checked ->
                            onToggleForce5g(checked)
                            if (checked) directOpenPhoneInfo()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00272E),
                            checkedTrackColor = CyanNeon,
                            uncheckedThumbColor = Color(0xFFCCCCCC),
                            uncheckedTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier.testTag("force_5g_toggle_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Permission Pill if missing
        if (!hasPermissions) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp,
                onClick = onRequestPermissions
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Grant diagnostics permission",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 2. Real-time Signal Strength Radial Gauge Indicator
        Text(
            text = "SIGNAL STRENGTH INDICATOR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0x80FFFFFF)
        )

        RadialSignalGauge(
            level = status.signalMetrics.level,
            dbm = status.signalMetrics.dbm,
            rsrp = status.signalMetrics.rsrp,
            sinr = status.signalMetrics.sinr,
            networkGen = status.networkGeneration,
            quality = status.signalMetrics.quality
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Physical Layer Signal Parameters (RSRP, SINR, RSRQ, ASU)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CleanGlassStat(
                title = "RSRP (POWER)",
                value = status.signalMetrics.rsrp?.let { "$it dBm" } ?: (if (status.signalMetrics.dbm != 0) "${status.signalMetrics.dbm} dBm" else "N/A"),
                accentColor = CyanNeon,
                modifier = Modifier.weight(1f)
            )
            CleanGlassStat(
                title = "SINR (RATIO)",
                value = status.signalMetrics.sinr?.let { "$it dB" } ?: "N/A",
                accentColor = MintNeon,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CleanGlassStat(
                title = "RSRQ (QUALITY)",
                value = status.signalMetrics.rsrq?.let { "$it dB" } ?: "N/A",
                accentColor = Color(0xFFD6E0FF),
                modifier = Modifier.weight(1f)
            )
            CleanGlassStat(
                title = "ASU LEVEL",
                value = "${status.signalMetrics.asu} ASU",
                accentColor = Color(0xFFFBBF24),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onNavigateToSpeedTest,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(16.dp))
                    .testTag("quick_speed_test_button")
            ) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("SPEED TEST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Button(
                onClick = onNavigateToDiagnostics,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .border(1.dp, Color(0x4DFFFFFF), RoundedCornerShape(16.dp))
                    .testTag("quick_diagnostics_button")
            ) {
                Text("LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CleanGlassStat(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
