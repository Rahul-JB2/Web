package com.example.ui.diagnostics

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NetworkEventLog
import com.example.data.NetworkLogRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon
import com.example.ui.theme.StatusRed
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
    val logs by repository.allLogs.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

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

            Row {
                IconButton(
                    onClick = onSimulateTransition,
                    modifier = Modifier.testTag("simulate_transition_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = "Simulate",
                        tint = CyanNeon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = StatusRed,
                            modifier = Modifier.size(20.dp)
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
private fun CleanGlassEventRow(log: NetworkEventLog) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    val (badgeText, badgeColor) = when (log.eventType) {
        "BAND_TRANSITION" -> Pair("HANDOVER", MintNeon)
        "NETWORK_CHANGE" -> Pair("NETWORK", Color(0xFF8DA4FF))
        "FORCE_5G_TOGGLED" -> Pair("FORCE 5G", CyanNeon)
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
                        text = "${log.previousNetwork} → ${log.newNetwork}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
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
