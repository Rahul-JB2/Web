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
    var selectedCategory by remember { mutableStateOf<BandCategory?>(null) }

    val filteredBands = remember(searchQuery, selectedCategory) {
        BandDatabase.global5gBands.filter { band ->
            val matchesCategory = selectedCategory == null || band.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                    band.band.lowercase().contains(q) ||
                    band.frequency.lowercase().contains(q) ||
                    band.commonCarriers.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "5G BANDS",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = Color.White
        )
        Text(
            text = "Frequency Spectrum Directory",
            fontSize = 11.sp,
            color = Color(0x99FFFFFF),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar with Glass Styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search n78, carrier, freq...", fontSize = 13.sp, color = Color(0x66FFFFFF)) },
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

        // Filter Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon,
                        selectedLabelColor = Color(0xFF00363D)
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == BandCategory.SUB_6_MID,
                    onClick = { selectedCategory = BandCategory.SUB_6_MID },
                    label = { Text("Mid-Band", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon,
                        selectedLabelColor = Color(0xFF00363D)
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == BandCategory.MMWAVE,
                    onClick = { selectedCategory = BandCategory.MMWAVE },
                    label = { Text("mmWave", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MintNeon,
                        selectedLabelColor = Color(0xFF00382E)
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCategory == BandCategory.SUB_6_LOW,
                    onClick = { selectedCategory = BandCategory.SUB_6_LOW },
                    label = { Text("Low-Band", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4361EE),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredBands, key = { it.band }) { band ->
                CleanGlassBandCard(band)
            }
        }
    }
}

@Composable
private fun CleanGlassBandCard(band: FiveGBand) {
    val accent = when (band.category) {
        BandCategory.SUB_6_MID -> CyanNeon
        BandCategory.MMWAVE -> MintNeon
        BandCategory.SUB_6_LOW -> Color(0xFFD6E0FF)
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f))
                        .border(1.dp, accent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = band.band, fontWeight = FontWeight.Black, fontSize = 13.sp, color = accent)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Band ${band.band} • ${band.duplex}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = band.frequency,
                        fontSize = 11.sp,
                        color = Color(0x80FFFFFF)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                GlassPill(
                    backgroundColor = accent.copy(alpha = 0.15f),
                    borderColor = accent.copy(alpha = 0.4f)
                ) {
                    Text(text = band.typicalSpeeds, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accent)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = band.commonCarriers.take(18) + "...",
                    fontSize = 9.sp,
                    color = Color(0x66FFFFFF)
                )
            }
        }
    }
}
