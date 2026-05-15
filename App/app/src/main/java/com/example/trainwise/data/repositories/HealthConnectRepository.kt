package com.example.trainwise.data.repositories

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.trainwise.data.models.HeartRateSample
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectRepository(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        return try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            granted.containsAll(permissions)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getWorkoutSummary(startTime: Instant, endTime: Instant): Pair<Int, Int> {
        return try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(
                        HeartRateRecord.BPM_AVG,
                        ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL
                    ),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )

            val avgBpm = response[HeartRateRecord.BPM_AVG]?.toInt() ?: 0
            val totalCals = response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toInt() ?: 0
            Pair(avgBpm, totalCals)
        } catch (e: Exception) {
            Log.e("HealthConnectRepo", "Error fetching summary", e)
            Pair(0, 0)
        }
    }

    suspend fun getHeartRateSamples(startTime: Instant, endTime: Instant): List<HeartRateSample> {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            
            response.records.flatMap { record ->
                record.samples.map { sample ->
                    HeartRateSample(bpm = sample.beatsPerMinute.toInt(), timestamp = sample.time.toEpochMilli())
                }
            }.sortedBy { it.timestamp }
        } catch (e: Exception) {
            Log.e("HealthConnectRepo", "Error fetching HR samples", e)
            emptyList()
        }
    }

    suspend fun getLatestHeartRate(): HeartRateRecord? {
        return try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.after(Instant.now().minus(15, ChronoUnit.MINUTES)),
                    ascendingOrder = false,
                    pageSize = 1
                )
            )
            response.records.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getCalories(startTime: Instant): Int {
        return try {
            val response = healthConnectClient.aggregate(
                AggregateRequest(
                    metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, Instant.now())
                )
            )
            response[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories?.toInt() ?: 0
        } catch (_: Exception) {
            0
        }
    }
}
