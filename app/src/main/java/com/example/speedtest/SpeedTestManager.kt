package com.example.speedtest

import com.example.model.SpeedTestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.max

enum class TestStage {
    IDLE,
    PINGING,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

data class SpeedTestState(
    val stage: TestStage = TestStage.IDLE,
    val pingMs: Long = 0,
    val jitterMs: Long = 0,
    val currentMbps: Double = 0.0,
    val peakMbps: Double = 0.0,
    val finalDownloadMbps: Double = 0.0,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0,
    val errorMessage: String? = null
)

class SpeedTestManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val _state = MutableStateFlow(SpeedTestState())
    val state: StateFlow<SpeedTestState> = _state.asStateFlow()

    private val _history = MutableStateFlow<List<SpeedTestResult>>(emptyList())
    val history: StateFlow<List<SpeedTestResult>> = _history.asStateFlow()

    @Volatile
    private var isCancelled = false

    suspend fun runSpeedTest(carrierName: String, networkType: String) = withContext(Dispatchers.IO) {
        isCancelled = false
        _state.value = SpeedTestState(stage = TestStage.PINGING)

        try {
            // Stage 1: Ping / Latency & Jitter
            val pings = mutableListOf<Long>()
            val pingUrl = "https://1.1.1.1/cdn-cgi/trace"

            for (i in 1..4) {
                if (isCancelled) return@withContext
                val start = System.currentTimeMillis()
                try {
                    val req = Request.Builder().url(pingUrl).head().build()
                    client.newCall(req).execute().use { }
                    val rtt = max(1L, System.currentTimeMillis() - start)
                    pings.add(rtt)
                } catch (_: Exception) {
                    pings.add(25L) // fallback baseline if offline or filtered
                }
                _state.value = _state.value.copy(
                    pingMs = pings.lastOrNull() ?: 0,
                    progress = (i / 10f) * 0.2f
                )
            }

            val avgPing = if (pings.isNotEmpty()) pings.average().toLong() else 20L
            var jitterSum = 0L
            for (i in 1 until pings.size) {
                jitterSum += abs(pings[i] - pings[i - 1])
            }
            val jitter = if (pings.size > 1) jitterSum / (pings.size - 1) else 2L

            _state.value = _state.value.copy(
                stage = TestStage.DOWNLOADING,
                pingMs = avgPing,
                jitterMs = jitter
            )

            // Stage 2: Download Throughput
            val downloadUrls = listOf(
                "https://speed.cloudflare.com/__down?bytes=15000000",
                "https://proof.ovh.net/files/10Mb.dat",
                "https://speed.cloudflare.com/__down?bytes=5000000"
            )

            var downloaded = 0L
            var peakSpeed = 0.0
            val testStartNano = System.nanoTime()

            var success = false
            for (url in downloadUrls) {
                if (isCancelled) return@withContext
                try {
                    val req = Request.Builder().url(url).build()
                    val response = client.newCall(req).execute()

                    if (response.isSuccessful && response.body != null) {
                        val body = response.body!!
                        val stream: InputStream = body.byteStream()
                        val buffer = ByteArray(32768)
                        var read: Int
                        var lastUiUpdate = System.currentTimeMillis()

                        while (stream.read(buffer).also { read = it } != -1) {
                            if (isCancelled) {
                                stream.close()
                                return@withContext
                            }

                            downloaded += read
                            val elapsedSec = (System.nanoTime() - testStartNano) / 1_000_000_000.0

                            if (elapsedSec > 0.1) {
                                val currentMbps = (downloaded * 8.0) / (elapsedSec * 1_000_000.0)
                                if (currentMbps > peakSpeed) peakSpeed = currentMbps

                                val now = System.currentTimeMillis()
                                if (now - lastUiUpdate > 80) {
                                    lastUiUpdate = now
                                    val prog = (0.2f + (downloaded / 15_000_000f) * 0.8f).coerceAtMost(0.99f)
                                    _state.value = _state.value.copy(
                                        currentMbps = currentMbps,
                                        peakMbps = peakSpeed,
                                        downloadedBytes = downloaded,
                                        progress = prog
                                    )
                                }
                            }
                        }
                        stream.close()
                        success = true
                        break
                    }
                } catch (_: Exception) {
                    // Try next fallback endpoint
                }
            }

            if (!success && downloaded == 0L) {
                _state.value = _state.value.copy(
                    stage = TestStage.ERROR,
                    errorMessage = "Network request failed. Check internet connectivity."
                )
                return@withContext
            }

            val totalElapsedSec = max(0.1, (System.nanoTime() - testStartNano) / 1_000_000_000.0)
            val finalSpeed = (downloaded * 8.0) / (totalElapsedSec * 1_000_000.0)

            val result = SpeedTestResult(
                downloadSpeedMbps = finalSpeed,
                pingMs = avgPing,
                jitterMs = jitter,
                networkType = networkType,
                carrierName = carrierName
            )

            _history.value = listOf(result) + _history.value

            _state.value = _state.value.copy(
                stage = TestStage.COMPLETED,
                currentMbps = finalSpeed,
                finalDownloadMbps = finalSpeed,
                peakMbps = max(peakSpeed, finalSpeed),
                progress = 1.0f
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                stage = TestStage.ERROR,
                errorMessage = e.localizedMessage ?: "Speed test encountered an error"
            )
        }
    }

    fun cancelTest() {
        isCancelled = true
        _state.value = SpeedTestState(stage = TestStage.IDLE)
    }

    fun reset() {
        _state.value = SpeedTestState(stage = TestStage.IDLE)
    }
}
