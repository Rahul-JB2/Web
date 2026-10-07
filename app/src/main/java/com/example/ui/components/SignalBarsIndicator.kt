package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.SignalQuality
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun SignalBarsIndicator(
    level: Int, // 0 to 4
    quality: SignalQuality,
    modifier: Modifier = Modifier,
    barWidth: Dp = 4.dp,
    spacing: Dp = 3.dp,
    maxHeight: Dp = 20.dp
) {
    val activeColor = when (quality) {
        SignalQuality.EXCELLENT -> StatusGreen
        SignalQuality.GOOD -> CyanNeon
        SignalQuality.FAIR -> StatusAmber
        SignalQuality.POOR, SignalQuality.NO_SIGNAL -> StatusRed
    }

    val heights = listOf(0.35f, 0.55f, 0.78f, 1.0f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Bottom
    ) {
        heights.forEachIndexed { index, fraction ->
            val isFilled = (index + 1) <= level
            val barColor by animateColorAsState(
                targetValue = if (isFilled) activeColor else Color(0x33FFFFFF),
                animationSpec = tween(durationMillis = 300),
                label = "bar_color_$index"
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(maxHeight * fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
