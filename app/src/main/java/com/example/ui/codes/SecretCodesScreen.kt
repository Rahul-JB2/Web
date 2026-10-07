package com.example.ui.codes

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.launcher.Force5gLauncher
import com.example.model.SecretCodesDatabase
import com.example.model.SecretDialCode
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassPill
import com.example.ui.theme.CyanNeon

@Composable
fun SecretCodesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedBrand by remember { mutableStateOf<String?>(null) }
    val brands = listOf("Universal", "Samsung", "Xiaomi", "OnePlus", "Vivo")

    val filteredCodes = remember(selectedBrand) {
        if (selectedBrand == null) {
            SecretCodesDatabase.codes
        } else {
            SecretCodesDatabase.codes.filter {
                it.oemBrand.contains(selectedBrand!!, ignoreCase = true) ||
                        it.title.contains(selectedBrand!!, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "SECRET CODES",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            color = Color.White
        )
        Text(
            text = "Hardware Keypad Codes",
            fontSize = 11.sp,
            color = Color(0x99FFFFFF),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedBrand == null,
                    onClick = { selectedBrand = null },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon,
                        selectedLabelColor = Color(0xFF00363D)
                    )
                )
            }
            items(brands) { brand ->
                FilterChip(
                    selected = selectedBrand == brand,
                    onClick = { selectedBrand = brand },
                    label = { Text(brand, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyanNeon,
                        selectedLabelColor = Color(0xFF00363D)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCodes, key = { it.code + it.title }) { item ->
                CleanGlassCodeRow(
                    codeItem = item,
                    onDial = { Force5gLauncher.openDialerWithCode(context, item.code) },
                    onCopy = { Force5gLauncher.copyToClipboard(context, item.code) }
                )
            }
        }
    }
}

@Composable
private fun CleanGlassCodeRow(
    codeItem: SecretDialCode,
    onDial: () -> Unit,
    onCopy: () -> Unit
) {
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
                    Text(
                        text = codeItem.code,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyanNeon
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GlassPill(
                        backgroundColor = Color(0x1AFFFFFF),
                        borderColor = Color(0x33FFFFFF)
                    ) {
                        Text(
                            text = codeItem.oemBrand.take(12),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD6E0FF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = codeItem.title,
                    fontSize = 11.sp,
                    color = Color(0x99FFFFFF)
                )
            }

            Row {
                IconButton(onClick = onDial, modifier = Modifier.size(36.dp).testTag("dial_code_${codeItem.code}")) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Dial", tint = CyanNeon, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(36.dp).testTag("copy_code_${codeItem.code}")) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0x99FFFFFF), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
