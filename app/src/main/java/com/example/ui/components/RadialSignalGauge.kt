package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkGeneration
import com.example.model.SignalQuality
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadialSignalGauge(
    level: Int,
    dbm: Int,
    rsrp: Int?,
    sinr: Int?,
    networkGen: NetworkGeneration,
    quality: SignalQuality,
    modifier: Modifier = Modifier
) {
    val normalizedScore: Float = if (rsrp != null && rsrp != 0) {
        ((rsrp + 125f) / 60f).coerceIn(0.05f, 1.0f)
    } else if (dbm != 0) {
        ((dbm + 125f) / 60f).coerceIn(0.05f, 1.0f)
    } else {
        (level / 4f).coerceIn(0.05f, 1.0f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = normalizedScore,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "radial_progress"
    )

    val qualityColor = when (quality) {
        SignalQuality.EXCELLENT -> StatusGreen
        SignalQuality.GOOD -> CyanNeon
        SignalQuality.FAIR -> StatusAmber
        SignalQuality.POOR, SignalQuality.NO_SIGNAL -> StatusRed
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 22.dp.toPx()
            val startAngle = 145f
            val totalSweep = 250f

            // 1. Subtle Glass Inner Shadow Ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x1400E5FF), Color(0x00000000)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 2. Minimalist Glass Ticks
            val tickCount = 24
            val angleStep = totalSweep / (tickCount - 1)
            for (i in 0 until tickCount) {
                val tickAngleDeg = startAngle + (i * angleStep)
                val tickAngleRad = Math.toRadians(tickAngleDeg.toDouble())
                val isMajor = i % 4 == 0

                val innerR = if (isMajor) radius - 12.dp.toPx() else radius - 7.dp.toPx()
                val outerR = radius - 1.dp.toPx()

                val startX = center.x + (innerR * cos(tickAngleRad)).toFloat()
                val startY = center.y + (innerR * sin(tickAngleRad)).toFloat()
                val endX = center.x + (outerR * cos(tickAngleRad)).toFloat()
                val endY = center.y + (outerR * sin(tickAngleRad)).toFloat()

                val tickProgress = i.toFloat() / (tickCount - 1)
                val isLit = tickProgress <= animatedProgress

                val tickColor = if (isLit) {
                    if (tickProgress > 0.7f) MintNeon else CyanNeon
                } else {
                    Color(0x22FFFFFF)
                }

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3. Frosted Track Arc
            val arcStroke = 10.dp.toPx()
            drawArc(
                color = Color(0x1FFFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = arcStroke, cap = StrokeCap.Round)
            )

            // 4. Glowing Neon Arc
            val activeSweep = totalSweep * animatedProgress
            if (activeSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.4f to Color(0xFF4361EE),
                        0.7f to CyanNeon,
                        1.0f to MintNeon
                    ),
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                )

                // Glowing Arc Tip
                val tipRad = Math.toRadians((startAngle + activeSweep).toDouble())
                val tipX = center.x + (radius * cos(tipRad)).toFloat()
                val tipY = center.y + (radius * sin(tipRad)).toFloat()

                drawCircle(
                    color = CyanNeon.copy(alpha = 0.5f),
                    radius = 9.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(tipX, tipY)
                )
            }
        }

        // Center Minimalist Stats
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GlassPill(
                backgroundColor = if (networkGen.is5G) Color(0x2B00E5FF) else Color(0x2B4361EE),
                borderColor = if (networkGen.is5G) Color(0x6600E5FF) else Color(0x664361EE)
            ) {
                Text(
                    text = if (networkGen.is5G) "5G NR" else networkGen.displayName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = if (networkGen.is5G) CyanNeon else Color(0xFFD6E0FF)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val displayDbm = rsrp ?: (if (dbm != 0) dbm else -95)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$displayDbm",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = " dBm",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0x99FFFFFF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Minimalist status line (e.g. "92% • EXCELLENT")
            val percent = (normalizedScore * 100).toInt()
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(qualityColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$percent% • ${quality.label.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = qualityColor
                )
            }
        }
    }
}
