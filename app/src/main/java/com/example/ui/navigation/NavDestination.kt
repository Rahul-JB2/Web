package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.WifiTethering),
    FORCE_5G("force_5g", "Force 5G", Icons.Default.Lock),
    DIAGNOSTICS("diagnostics", "Logs", Icons.Default.History),
    SPEED_TEST("speed_test", "Speed", Icons.Default.Speed),
    BANDS("bands", "Bands", Icons.Default.CellTower),
    CODES("codes", "Codes", Icons.Default.Key)
}
