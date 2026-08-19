package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.location.LocationTracker
import com.example.wage.DdiWageEngine
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
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: SarthiDatabase
  private lateinit var hazardDao: HazardDao
  private lateinit var tripDao: TripDao
  private lateinit var incidentDao: IncidentDao
  private lateinit var context: Context

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    database = Room.inMemoryDatabaseBuilder(context, SarthiDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    hazardDao = database.hazardDao()
    tripDao = database.tripDao()
    incidentDao = database.incidentDao()
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Sarthi Shield", appName)
  }

  @Test
  fun `location distance calculation verifies proximity`() {
    val tracker = LocationTracker(context)
    val distance = tracker.calculateDistanceMeters(28.6139, 77.2090, 28.6143, 77.2093)
    assertTrue("Distance should be approximately 50 meters", distance in 40.0..70.0)
  }

  @Test
  fun `ddi wage engine calculates fair compensation and wait fine`() {
    val engine = DdiWageEngine()
    
    // Level 5 severe storm + high IMU roughness
    val ddi = engine.computeDdi(
      imuRms = 18f,
      imuKurtosis = 4.5f,
      visualHazardsCount = 3,
      weather = WeatherCondition.WATERLOGGED_STORM,
      speedKmh = 20f
    )
    assertEquals(5, ddi.level)
    assertEquals(0.50f, ddi.bonusMultiplier, 0.01f)
    assertTrue(ddi.isRatingImmunityEligible)

    // 4.0 km distance, 9 minutes customer wait (4 minutes billable * ₹2 = ₹8)
    engine.updateTripProgress(distanceKm = 4.0f, waitMinutes = 9)
    val wage = engine.liveWage.value

    // Base (25) + Dist (4 * 6.5 = 26) = 51. DDI +50% = 25.5. Wait = 8. Total = 84.5
    assertEquals(25.0, wage.basePay, 0.01)
    assertEquals(26.0, wage.distanceFee, 0.01)
    assertEquals(25.5, wage.ddiBonus, 0.01)
    assertEquals(8.0, wage.waitCharge, 0.01)
    assertEquals(84.5, wage.totalFairPayout, 0.01)
    assertTrue("Extra money earned is positive", wage.extraMoneyEarned > 50.0)
  }

  @Test
  fun `room database insert and retrieve fused hazard`() = runBlocking {
    val hazard = HazardEntity(
      type = HazardType.POTHOLE,
      fusionSource = FusionSource.FUSED_VISION_AND_IMU,
      confidence = 0.98f,
      latitude = 28.6139,
      longitude = 77.2090,
      peakGForce = 2.4f,
      zAxisDisplacement = 16.5f,
      notes = "Fused test pothole"
    )

    val id = hazardDao.insertHazard(hazard)
    assertTrue(id > 0)

    val retrieved = hazardDao.getHazardById(id)
    assertNotNull(retrieved)
    assertEquals(HazardType.POTHOLE, retrieved?.type)
    assertEquals(FusionSource.FUSED_VISION_AND_IMU, retrieved?.fusionSource)
    assertEquals(0.98f, retrieved?.confidence ?: 0f, 0.01f)
  }
}
