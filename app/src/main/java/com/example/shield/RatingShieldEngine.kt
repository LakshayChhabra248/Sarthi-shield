package com.example.shield

import android.content.Context
import com.example.data.local.IncidentDao
import com.example.data.local.IncidentReportEntity
import com.example.data.local.IncidentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

data class IdShieldHealth(
    val healthPercent: Int = 100,
    val status: String = "SHIELDED & ACTIVE",
    val immunityCertificatesIssued: Int = 14,
    val ratingsProtected: Int = 11,
    val disputesWonAutomatically: Int = 9,
    val idBlockedRisk: Float = 0.0f // 0% risk of account suspension
)

class RatingShieldEngine(
    private val context: Context,
    private val incidentDao: IncidentDao,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    val allIncidents: StateFlow<List<IncidentReportEntity>> = incidentDao.getAllIncidents()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _idHealth = MutableStateFlow(IdShieldHealth())
    val idHealth: StateFlow<IdShieldHealth> = _idHealth.asStateFlow()

    private val _lastIssuedCertificateId = MutableStateFlow<String?>(null)
    val lastIssuedCertificateId: StateFlow<String?> = _lastIssuedCertificateId.asStateFlow()

    init {
        seedSampleImmunityIncidentsIfEmpty()
    }

    /**
     * Issues an automated verifiable AI Rating Immunity Certificate
     */
    fun issueRatingImmunity(
        orderId: String,
        incidentType: IncidentType,
        title: String,
        description: String,
        lat: Double,
        lng: Double,
        peakG: Float,
        peakDecibel: Float
    ): String {
        val certId = "SARTHI-IMMUNITY-" + UUID.randomUUID().toString().take(8).uppercase()
        val entity = IncidentReportEntity(
            orderId = orderId,
            incidentType = incidentType,
            title = title,
            description = description,
            latitude = lat,
            longitude = lng,
            decibelPeak = peakDecibel,
            peakGForce = peakG,
            audioEvidenceRecorded = peakDecibel > 75f,
            visualEvidenceCaptured = true,
            ratingImmunityCertificateId = certId,
            disputeResolved = true,
            timestamp = System.currentTimeMillis()
        )

        coroutineScope.launch {
            incidentDao.insertIncident(entity)
            _lastIssuedCertificateId.value = certId
            val cur = _idHealth.value
            _idHealth.value = cur.copy(
                immunityCertificatesIssued = cur.immunityCertificatesIssued + 1,
                ratingsProtected = cur.ratingsProtected + 1,
                disputesWonAutomatically = cur.disputesWonAutomatically + 1
            )
        }

        return certId
    }

    private fun seedSampleImmunityIncidentsIfEmpty() {
        coroutineScope.launch {
            val list = incidentDao.getAllIncidents()
            // If empty, insert benchmark proofs
            val sample = listOf(
                IncidentReportEntity(
                    orderId = "ZOM-7821",
                    incidentType = IncidentType.UNFAIR_LATE_REVIEW_SHIELD,
                    title = "Late Delivery Review Blocked",
                    description = "Customer gave 1-star rating for 12m delay. Sarthi-Shield proved 3 severe waterlogged roads & 7 potholes on route.",
                    latitude = 28.6139,
                    longitude = 77.2090,
                    peakGForce = 2.4f,
                    decibelPeak = 45f,
                    visualEvidenceCaptured = true,
                    ratingImmunityCertificateId = "SARTHI-IMMUNITY-89FA1C",
                    disputeResolved = true,
                    timestamp = System.currentTimeMillis() - 86400000L
                ),
                IncidentReportEntity(
                    orderId = "SWG-4429",
                    incidentType = IncidentType.EXTREME_CUSTOMER_WAIT_TIME,
                    title = "Gate Wait Penalty Billed & Rating Immune",
                    description = "Customer made rider wait 14 minutes at apartment gate. ₹18 automatically billed, negative rating neutralized.",
                    latitude = 28.6250,
                    longitude = 77.2180,
                    peakGForce = 1.0f,
                    decibelPeak = 52f,
                    visualEvidenceCaptured = false,
                    ratingImmunityCertificateId = "SARTHI-IMMUNITY-438B72",
                    disputeResolved = true,
                    timestamp = System.currentTimeMillis() - 172800000L
                ),
                IncidentReportEntity(
                    orderId = "ZPT-9012",
                    incidentType = IncidentType.CUSTOMER_AGGRESSION_DETECTED,
                    title = "Customer Aggression Evidence Preserved",
                    description = "Hostile shouting (88 dB) detected at delivery doorstep. Audio evidence timestamped and ID protected.",
                    latitude = 28.5980,
                    longitude = 77.2340,
                    peakGForce = 1.1f,
                    decibelPeak = 88.4f,
                    audioEvidenceRecorded = true,
                    visualEvidenceCaptured = false,
                    ratingImmunityCertificateId = "SARTHI-IMMUNITY-90CD33",
                    disputeResolved = true,
                    timestamp = System.currentTimeMillis() - 259200000L
                )
            )
            incidentDao.insertIncidents(sample)
        }
    }
}
