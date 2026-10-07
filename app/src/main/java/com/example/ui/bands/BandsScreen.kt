package com.example.ui.bands

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BandCategory
import com.example.model.BandDatabase
import com.example.model.FiveGBand
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.MintNeon

@Composable
fun BandsScreen(
    onNavigateToForce5g: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCarrier by remember { mutableStateOf<String?>("ALL") }

    val filteredBands = remember(searchQuery, selectedCarrier) {
        BandDatabase.global5gBands.filter { band ->
            val matchesCarrier = selectedCarrier == "ALL" || band.indianCarriers.contains(selectedCarrier)
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    band.band.lowercase().contains(q) ||
                    band.frequency.lowercase().contains(q) ||
                    band.commonCarriers.lowercase().contains(q) ||
                    band.indianAllocation.lowercase().contains(q)
            matchesCarrier && matchesQuery
        }
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
                    text = "INDIAN 5G BANDS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp,
                    color = Color.White
                )
                Text(
                    text = "Jio True 5G • Airtel 5G Plus • Vi 5G Spectrum",
                    fontSize = 11.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
            }

            GlassPill(
                backgroundColor = Color(0x3300E5FF),
                borderColor = Color(0x6600E5FF)
            ) {
                Text(
                    text = "INDIA (DoT)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = CyanNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with Glass Styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search n78, n28, Jio, Airtel, freq...", fontSize = 13.sp, color = Color(0x66FFFFFF)) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanNeon,
                unfocusedBorderColor = Color(0x33FFFFFF),
                focusedContainerColor = Color(0x1AFFFFFF),
                unfocusedContainerColor = Color(0x0DFFFFFF)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_bands_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Indian Carrier Filter Chips
        val carriers = listOf("ALL" to "All Indian Bands", "Jio" to "Jio True 5G", "Airtel" to "Airtel 5G Plus", "Vi" to "Vi 5G")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(carriers) { (carrierKey, label) ->
                val isSelected = selectedCarrier == carrierKey
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCarrier = carrierKey },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (carrierKey) {
                            "Jio" -> Color(0xFF0055FF)
                            "Airtel" -> Color(0xFFEF4444)
                            "Vi" -> Color(0xFFF59E0B)
                            else -> CyanNeon
                        },
                        selectedLabelColor = Color.White,
                        containerColor = Color(0x1AFFFFFF),
                        labelColor = Color(0x99FFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Band Cards List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBands, key = { it.band }) { band ->
                IndianGlassBandCard(band)
            }
        }
    }
}

@Composable
private fun IndianGlassBandCard(band: FiveGBand) {
    val accent = when (band.category) {
        BandCategory.SUB_6_MID -> CyanNeon
        BandCategory.MMWAVE -> MintNeon
        BandCategory.SUB_6_LOW -> Color(0xFF90B4FF)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Band Name + Frequency + Speed Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.15f))
                            .border(1.2.dp, accent.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = band.band,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = accent
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Band ${band.band}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            GlassPill(
                                backgroundColor = Color(0x1AFFFFFF),
                                borderColor = Color(0x33FFFFFF)
                            ) {
                                Text(
                                    text = band.duplex,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD6E0FF)
                                )
                            }
                        }
                        Text(
                            text = band.frequency,
                            fontSize = 11.sp,
                            color = Color(0x80FFFFFF)
                        )
                    }
                }

                GlassPill(
                    backgroundColor = accent.copy(alpha = 0.15f),
                    borderColor = accent.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = band.typicalSpeeds,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indian Carrier Pills Row (Jio, Airtel, Vi)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                band.indianCarriers.forEach { carrier ->
                    val (tagBg, tagFg) = when (carrier) {
                        "Jio" -> Pair(Color(0xFF0044CC), Color.White)
                        "Airtel" -> Pair(Color(0xFFCC1111), Color.White)
                        "Vi" -> Pair(Color(0xFFD97706), Color.White)
                        else -> Pair(Color(0x33FFFFFF), Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagBg)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = carrier.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = tagFg
                        )
                    }
                }

                if (band.indianAllocation.isNotEmpty()) {
                    Text(
                        text = "• ${band.indianAllocation}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0x99FFFFFF),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = band.description,
                fontSize = 11.sp,
                color = Color(0x80FFFFFF),
                lineHeight = 15.sp
            )
        }
    }
}
