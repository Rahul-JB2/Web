package com.example.ui.force5g

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon

@Composable
fun Force5gScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun directOpen() {
        val res = Force5gLauncher.openRadioInfo(context)
        when (res) {
            is LaunchResult.Success -> {
                Toast.makeText(context, "Direct Opened Phone Info! Choose 'NR only'", Toast.LENGTH_SHORT).show()
            }
            is LaunchResult.Failure -> {
                Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "BAND LOCK",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = Color.White
        )
        Text(
            text = "Direct Phone Info Setting (No Code Needed)",
            fontSize = 11.sp,
            color = Color(0x99FFFFFF),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Direct Action Glass Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderBrush = Brush.linearGradient(listOf(CyanNeon, MintNeon))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, CyanNeon, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Direct Open 'Phone info'",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Click to open Phone Info directly without dialing any code",
                    fontSize = 12.sp,
                    color = Color(0x99FFFFFF)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { directOpen() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("launch_radio_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = Color(0xFF00363D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DIRECT OPEN PHONE INFO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = Color(0xFF00363D)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Info Visual Guide Card (Matching user's screenshot exactly!)
        Text(
            text = "INSIDE 'PHONE INFO' SCREEN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0x80FFFFFF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "1. Locate 'Set preferred network type':",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B14))
                        .border(1.dp, CyanNeon, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NR only (Pure 5G SA Lock)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF070B14))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "NR/LTE (5G Preferred with VoLTE)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD6E0FF)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "2. Make sure 'Mobile radio power' switch is ON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Mode Badges
        Text(
            text = "RECOMMENDED MODES",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0x80FFFFFF)
        )

        Spacer(modifier = Modifier.height(8.dp))

        ModeGlassRow("NR only", "100% 5G Standalone", CyanNeon)
        Spacer(modifier = Modifier.height(6.dp))
        ModeGlassRow("NR / LTE", "5G Data + VoLTE Calls", MintNeon)
        Spacer(modifier = Modifier.height(6.dp))
        ModeGlassRow("NR / LTE / GSM", "Default Auto Switch", Color(0xFFCBD5E1))

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ModeGlassRow(mode: String, desc: String, accentColor: Color) {
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
            Text(text = mode, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accentColor)
            Text(text = desc, fontSize = 11.sp, color = Color(0x99FFFFFF))
        }
    }
}
