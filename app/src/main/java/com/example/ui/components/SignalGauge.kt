package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NetworkGeneration
import com.example.model.SignalQuality
import com.example.ui.theme.SignalExcellent
import com.example.ui.theme.SignalFair
import com.example.ui.theme.SignalGood
import com.example.ui.theme.SignalPoor

@Composable
fun SignalGauge(
    level: Int, // 0..4
    dbm: Int,
    networkGen: NetworkGeneration,
    quality: SignalQuality,
    modifier: Modifier = Modifier
) {
    val targetAngle = (level / 4f) * 240f
    val animatedAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gauge_angle"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val qualityColor = when (quality) {
        SignalQuality.EXCELLENT -> SignalExcellent
        SignalQuality.GOOD -> SignalGood
        SignalQuality.FAIR -> SignalFair
        SignalQuality.POOR -> SignalPoor
        SignalQuality.NO_SIGNAL -> Color.Gray
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arc Canvas
        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 16.dp.toPx()
            val startAngle = 150f
            val sweepTotal = 240f

            // Background track
            drawArc(
                color = Color(0xFF1E293B),
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active Arc
            if (animatedAngle > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.4f to Color(0xFF4361EE),
                        0.7f to Color(0xFF00E5FF),
                        1.0f to Color(0xFF00F5D4)
                    ),
                    startAngle = startAngle,
                    sweepAngle = animatedAngle.coerceAtMost(sweepTotal),
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pulse Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (networkGen.is5G) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF4361EE).copy(alpha = 0.15f),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (networkGen.is5G) Color(0xFF00E5FF).copy(alpha = pulseAlpha) else Color(0xFF4361EE))
                    )
                    Text(
                        text = if (networkGen.is5G) "5G ACTIVE" else networkGen.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (networkGen.is5G) Color(0xFF00E5FF) else Color(0xFFD6E0FF),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // Big Network Level
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (dbm != 0) "$dbm" else "--",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = " dBm",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Quality Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(qualityColor)
                )
                Text(
                    text = " ${quality.label} Signal ($level/4 bars)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = qualityColor
                )
            }
        }
    }
}
