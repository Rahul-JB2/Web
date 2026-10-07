package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.AppDatabase
import com.example.data.NetworkLogRepository
import com.example.speedtest.SpeedTestManager
import com.example.telephony.NetworkMonitor
import com.example.ui.bands.BandsScreen
import com.example.ui.codes.SecretCodesScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.diagnostics.DiagnosticView
import com.example.ui.force5g.Force5gScreen
import com.example.ui.navigation.AppDestination
import com.example.ui.speedtest.SpeedTestScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkMeshBackground
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.GlassCardBrush
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var logRepository: NetworkLogRepository
    private lateinit var networkMonitor: NetworkMonitor
    private val speedTestManager = SpeedTestManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getInstance(applicationContext)
        logRepository = NetworkLogRepository(database.networkLogDao())
        networkMonitor = NetworkMonitor(applicationContext, logRepository)

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    networkMonitor = networkMonitor,
                    speedTestManager = speedTestManager,
                    logRepository = logRepository
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        networkMonitor.unregister()
    }
}

@Composable
fun MainAppScreen(
    networkMonitor: NetworkMonitor,
    speedTestManager: SpeedTestManager,
    logRepository: NetworkLogRepository
) {
    val telephonyStatus by networkMonitor.status.collectAsState()
    val isForce5gEnabled by networkMonitor.isForce5gEnabled.collectAsState()
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var hasPermissions by remember { mutableStateOf(networkMonitor.hasPermissions()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.READ_PHONE_STATE] == true
        hasPermissions = granted
        if (granted) {
            networkMonitor.refreshAll()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermissions = networkMonitor.hasPermissions()
                networkMonitor.refreshAll()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (currentDestination != AppDestination.DASHBOARD) {
        BackHandler {
            currentDestination = AppDestination.DASHBOARD
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkMeshBackground)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                // Floating Android 16 Frosted Glass Capsule Navigation Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val navShape = RoundedCornerShape(28.dp)
                    NavigationBar(
                        containerColor = Color(0xCC0D1527),
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .clip(navShape)
                            .border(1.dp, GlassBorderBrush, navShape)
                            .height(68.dp)
                    ) {
                        AppDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF00272E),
                                    selectedTextColor = CyanNeon,
                                    indicatorColor = CyanNeon,
                                    unselectedIconColor = Color(0x80FFFFFF),
                                    unselectedTextColor = Color(0x80FFFFFF)
                                ),
                                modifier = Modifier.testTag("nav_item_${destination.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val contentModifier = Modifier.padding(innerPadding)

            when (currentDestination) {
                AppDestination.DASHBOARD -> {
                    DashboardScreen(
                        status = telephonyStatus,
                        isForce5gEnabled = isForce5gEnabled,
                        onToggleForce5g = { enabled ->
                            networkMonitor.setForce5gMode(enabled)
                        },
                        hasPermissions = hasPermissions,
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE
                                )
                            )
                        },
                        onNavigateToForce5g = { currentDestination = AppDestination.FORCE_5G },
                        onNavigateToSpeedTest = { currentDestination = AppDestination.SPEED_TEST },
                        onNavigateToDiagnostics = { currentDestination = AppDestination.DIAGNOSTICS },
                        onRefresh = { networkMonitor.refreshAll() },
                        modifier = contentModifier
                    )
                }

                AppDestination.FORCE_5G -> {
                    Force5gScreen(modifier = contentModifier)
                }

                AppDestination.DIAGNOSTICS -> {
                    DiagnosticView(
                        repository = logRepository,
                        onSimulateTransition = {
                            val bands = listOf("5G n78", "5G n77", "5G n41", "5G n28", "LTE B3")
                            val currentBand = telephonyStatus.cellTower.bandName ?: "LTE B3"
                            val nextBand = bands.filter { it != currentBand }.random()
                            networkMonitor.addManualDiagnosticLog(
                                type = "BAND_TRANSITION",
                                desc = "Handover: $currentBand → $nextBand",
                                oldBand = currentBand,
                                newBand = nextBand
                            )
                        },
                        modifier = contentModifier
                    )
                }

                AppDestination.SPEED_TEST -> {
                    SpeedTestScreen(
                        speedTestManager = speedTestManager,
                        telephonyStatus = telephonyStatus,
                        modifier = contentModifier
                    )
                }

                AppDestination.BANDS -> {
                    BandsScreen(
                        onNavigateToForce5g = { currentDestination = AppDestination.FORCE_5G },
                        modifier = contentModifier
                    )
                }

                AppDestination.CODES -> {
                    SecretCodesScreen(modifier = contentModifier)
                }
            }
        }
    }
}
