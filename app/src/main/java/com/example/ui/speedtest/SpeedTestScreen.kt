package com.example.ui.speedtest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpeedTestResult
import com.example.model.TelephonyStatus
import com.example.speedtest.SpeedTestManager
import com.example.speedtest.TestStage
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
fun SpeedTestScreen(
    speedTestManager: SpeedTestManager,
    telephonyStatus: TelephonyStatus,
    modifier: Modifier = Modifier
) {
    val state by speedTestManager.state.collectAsState()
    val history by speedTestManager.history.collectAsState()
    val scope = rememberCoroutineScope()
    val isRunning = state.stage == TestStage.PINGING || state.stage == TestStage.DOWNLOADING

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
                    text = "SPEED BENCHMARK",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
                Text(
                    text = "${telephonyStatus.operatorName} • 5G Throughput",
                    fontSize = 11.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
            }

            GlassPill(
                backgroundColor = if (telephonyStatus.networkGeneration.is5G) Color(0x3300E5FF) else Color(0x334361EE),
                borderColor = if (telephonyStatus.networkGeneration.is5G) Color(0x6600E5FF) else Color(0x664361EE)
            ) {
                Text(
                    text = if (telephonyStatus.networkGeneration.is5G) "5G NR" else "CELLULAR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (telephonyStatus.networkGeneration.is5G) CyanNeon else Color(0xFFD6E0FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Glass Speedometer Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val fraction = (state.currentMbps.toFloat() / 1000f).coerceIn(0f, 1f)
                val animatedAngle by animateFloatAsState(
                    targetValue = fraction * 240f,
                    animationSpec = tween(150, easing = FastOutSlowInEasing),
                    label = "speed_arc"
                )

                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 10.dp.toPx()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = (size.width / 2f) - 16.dp.toPx()
                        val startAngle = 150f
                        val sweepTotal = 240f

                        // Background Track
                        drawArc(
                            color = Color(0x1FFFFFFF),
                            startAngle = startAngle,
                            sweepAngle = sweepTotal,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )

                        // Glowing Active Arc
                        if (animatedAngle > 0f) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0.4f to Color(0xFF4361EE),
                                    0.7f to CyanNeon,
                                    1.0f to MintNeon
                                ),
                                startAngle = startAngle,
                                sweepAngle = animatedAngle,
                                useCenter = false,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2),
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (state.stage) {
                                TestStage.PINGING -> "PING"
                                TestStage.DOWNLOADING -> "DOWNLOADING"
                                TestStage.COMPLETED -> "FINISHED"
                                TestStage.ERROR -> "FAILED"
                                TestStage.IDLE -> "READY"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = CyanNeon
                        )

                        val displayMbps = if (state.stage == TestStage.COMPLETED) state.finalDownloadMbps else state.currentMbps
                        Text(
                            text = String.format(Locale.US, "%.1f", displayMbps),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Mbps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0x80FFFFFF)
                        )
                    }
                }

                // 3 Clean Glass Metric Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniGlassStat("PING", if (state.pingMs > 0) "${state.pingMs} ms" else "--", CyanNeon)
                    MiniGlassStat("JITTER", if (state.jitterMs > 0) "${state.jitterMs} ms" else "--", MintNeon)
                    MiniGlassStat("PEAK", if (state.peakMbps > 0) String.format(Locale.US, "%.0f M", state.peakMbps) else "--", Color(0xFFD6E0FF))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Button
                if (isRunning) {
                    Button(
                        onClick = { speedTestManager.cancelTest() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("cancel_speed_test_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CANCEL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                speedTestManager.runSpeedTest(
                                    carrierName = telephonyStatus.operatorName,
                                    networkType = telephonyStatus.networkGeneration.displayName
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_speed_test_button")
                    ) {
                        Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF00363D), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.stage == TestStage.COMPLETED) "TEST AGAIN" else "START 5G TEST",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF00363D)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History
        Text(
            text = "PREVIOUS RUNS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0x80FFFFFF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tests yet • Tap Start",
                    fontSize = 12.sp,
                    color = Color(0x66FFFFFF)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    GlassHistoryRow(item)
                }
            }
        }
    }
}

@Composable
private fun MiniGlassStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0x80FFFFFF), letterSpacing = 0.5.sp)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun GlassHistoryRow(result: SpeedTestResult) {
    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(result.timestamp))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.US, "%.1f Mbps", result.downloadSpeedMbps),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.width(8.dp))
                GlassPill(
                    backgroundColor = Color(0x1AFFFFFF),
                    borderColor = Color(0x33FFFFFF)
                ) {
                    Text(text = result.networkType, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD6E0FF))
                }
            }
            Text(
                text = "${result.pingMs}ms • $dateStr",
                fontSize = 11.sp,
                color = Color(0x80FFFFFF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
