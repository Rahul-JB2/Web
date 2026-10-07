package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.NetworkEventLog
import com.example.model.BandDatabase
import com.example.model.SecretCodesDatabase
import com.example.telephony.BandCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Force 5G", appName)
  }

  @Test
  fun `verify nr band calculation for n78 and n77`() {
    val (band78, freq78) = BandCalculator.getNrBandFromArfcn(640000)
    assertEquals("n78", band78)
    assertTrue(freq78.contains("3.5 GHz"))

    val (band41, freq41) = BandCalculator.getNrBandFromArfcn(510000)
    assertEquals("n41", band41)
  }

  @Test
  fun `verify global 5g band database is populated`() {
    assertTrue(BandDatabase.global5gBands.isNotEmpty())
    val n78 = BandDatabase.global5gBands.find { it.band == "n78" }
    assertNotNull(n78)
    assertEquals("TDD", n78?.duplex)
  }

  @Test
  fun `verify secret dial codes database contains radio info`() {
    val primary = SecretCodesDatabase.codes.find { it.isPrimaryRadioInfo }
    assertNotNull(primary)
    assertEquals("*#*#4636#*#*", primary?.code)
  }

  @Test
  fun `room database inserts and queries network event log`() = runBlocking {
    val dao = db.networkLogDao()
    val log = NetworkEventLog(
      eventType = "BAND_TRANSITION",
      previousNetwork = "4G LTE",
      newNetwork = "5G NR",
      previousBand = "LTE B3",
      newBand = "5G NR n78",
      operatorName = "Test Carrier",
      rsrpDbm = -82,
      sinrDb = 22,
      description = "Handover to 5G n78"
    )

    dao.insertLog(log)

    val logs = dao.getAllLogs().first()
    assertEquals(1, logs.size)
    assertEquals("BAND_TRANSITION", logs[0].eventType)
    assertEquals("5G NR n78", logs[0].newBand)
    assertEquals(-82, logs[0].rsrpDbm)
  }
}
