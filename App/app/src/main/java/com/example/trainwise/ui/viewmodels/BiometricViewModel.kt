package com.example.trainwise.ui.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.data.models.Device
import com.example.trainwise.data.models.HeartRateSample
import com.example.trainwise.data.repositories.HealthConnectRepository
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.*
import java.time.Instant
import java.util.Collections

class BiometricViewModel : ViewModel() {

    enum class HealthConnectStatus {
        AVAILABLE,
        NOT_INSTALLED,
        UPDATE_REQUIRED,
        UNAVAILABLE
    }

    private val _sdkStatus = mutableStateOf(HealthConnectStatus.UNAVAILABLE)
    val sdkStatus: State<HealthConnectStatus> = _sdkStatus

    private val _heartRate = mutableStateOf(0)
    val heartRate: State<Int> = _heartRate

    private val _calories = mutableStateOf(0)
    val calories: State<Int> = _calories

    private val _isRefreshing = mutableStateOf(false)
    val isRefreshing: State<Boolean> = _isRefreshing

    private val _lastUpdate = mutableStateOf<Long>(0)
    val lastUpdate: State<Long> = _lastUpdate

    private val heartRateSamples = Collections.synchronizedList(mutableListOf<Int>())
    private var lastHealthConnectSampleTime: Instant? = null

    private val _permissionsGranted = mutableStateOf(false)
    val permissionsGranted: State<Boolean> = _permissionsGranted

    private val _isSamsungHealthInstalled = mutableStateOf(false)
    val isSamsungHealthInstalled: State<Boolean> = _isSamsungHealthInstalled

    private val _devices = mutableStateOf<List<Device>>(emptyList())
    val devices: State<List<Device>> = _devices

    private val _isScanning = mutableStateOf(false)
    val isScanning: State<Boolean> = _isScanning

    private var healthConnectRepository: HealthConnectRepository? = null

    val healthConnectAvailable = derivedStateOf {
        _sdkStatus.value == HealthConnectStatus.AVAILABLE
    }

    val isAnyDeviceConnected = derivedStateOf {
        _devices.value.any { it.isConnected } && _permissionsGranted.value
    }

    private fun getRepository(context: Context): HealthConnectRepository {
        return healthConnectRepository ?: HealthConnectRepository(context).also { healthConnectRepository = it }
    }

    fun checkHealthConnectStatus(context: Context) {
        try {
            val status = HealthConnectClient.getSdkStatus(context)
            _sdkStatus.value = when (status) {
                HealthConnectClient.SDK_AVAILABLE -> HealthConnectStatus.AVAILABLE
                HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectStatus.NOT_INSTALLED
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectStatus.UPDATE_REQUIRED
                else -> HealthConnectStatus.UNAVAILABLE
            }
            
            val packageManager = context.packageManager
            _isSamsungHealthInstalled.value = try {
                packageManager.getPackageInfo("com.sec.android.app.shealth", 0)
                true
            } catch (e: Exception) {
                false
            }

            if (_sdkStatus.value == HealthConnectStatus.AVAILABLE) {
                val repo = getRepository(context)
                viewModelScope.launch {
                    _permissionsGranted.value = repo.hasAllPermissions()
                    refreshDevicesList(context)
                }
            } else {
                refreshDevicesList(context)
            }
        } catch (e: Exception) {
            _sdkStatus.value = HealthConnectStatus.UNAVAILABLE
            refreshDevicesList(context)
        }
    }

    private fun refreshDevicesList(context: Context) {
        val updatedDevices = mutableListOf<Device>()
        
        if (_isSamsungHealthInstalled.value) {
            updatedDevices.add(
                Device(
                    id = "samsung_health",
                    name = "Samsung Health",
                    type = "Companion App",
                    isConnected = true,
                    icon = Icons.Outlined.Watch
                )
            )
        }

        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                updatedDevices.add(
                    Device(
                        id = node.id,
                        name = node.displayName,
                        type = "Wear OS Watch",
                        isConnected = true,
                        icon = Icons.Outlined.Watch
                    )
                )
            }
            _devices.value = updatedDevices.distinctBy { it.id }
        }.addOnFailureListener {
            _devices.value = updatedDevices.distinctBy { it.id }
        }
    }

    fun openHealthConnectStore(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
        }
        context.startActivity(intent)
    }

    suspend fun fetchWorkoutSummary(context: Context, startTime: Instant, endTime: Instant): Pair<Int, Int> {
        if (!healthConnectAvailable.value || !_permissionsGranted.value) {
            return Pair(0, 0)
        }
        return getRepository(context).getWorkoutSummary(startTime, endTime)
    }

    suspend fun fetchHeartRateSamples(context: Context, startTime: Instant, endTime: Instant): List<HeartRateSample> {
        if (!healthConnectAvailable.value || !_permissionsGranted.value) return emptyList()
        return getRepository(context).getHeartRateSamples(startTime, endTime)
    }

    private suspend fun fetchHeartRateInternal(context: Context) {
        if (!healthConnectAvailable.value || !_permissionsGranted.value) return
        val repo = getRepository(context)
        try {
            val latestRecord = repo.getLatestHeartRate()
            val latestSample = latestRecord?.samples?.maxByOrNull { it.time }
            if (latestSample != null) {
                val bpm = latestSample.beatsPerMinute.toInt()
                if (bpm > 0) {
                    withContext(Dispatchers.Main) {
                        _heartRate.value = bpm
                        if (latestSample.time != lastHealthConnectSampleTime) {
                            heartRateSamples.add(bpm)
                            lastHealthConnectSampleTime = latestSample.time
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BiometricViewModel", "Error fetching heart rate", e)
        }
    }

    private suspend fun fetchCaloriesInternal(context: Context, startTime: Instant) {
        if (!healthConnectAvailable.value || !_permissionsGranted.value) return
        val repo = getRepository(context)
        try {
            val totalCals = repo.getCalories(startTime)
            if (totalCals > 0) {
                withContext(Dispatchers.Main) {
                    _calories.value = totalCals
                }
            }
        } catch (e: Exception) {
            Log.e("BiometricViewModel", "Error fetching calories", e)
        }
    }

    fun refreshData(context: Context, startTime: Instant?) {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchHeartRateInternal(context)
            startTime?.let { fetchCaloriesInternal(context, it) }
            _lastUpdate.value = System.currentTimeMillis()
            delay(500)
            _isRefreshing.value = false
        }
    }

    fun getAverageBpm(): Int {
        synchronized(heartRateSamples) {
            return if (heartRateSamples.isNotEmpty()) heartRateSamples.average().toInt() else 0
        }
    }

    fun resetSessionData() {
        heartRateSamples.clear()
        _heartRate.value = 0
        _calories.value = 0
        _lastUpdate.value = 0
        lastHealthConnectSampleTime = null
    }

    fun updateDeviceConnectionStatus(context: Context) {
        checkHealthConnectStatus(context)
    }

    fun scanForBiometricDevices(context: Context) {
        viewModelScope.launch {
            _isScanning.value = true
            checkHealthConnectStatus(context)
            delay(1000)
            _isScanning.value = false
        }
    }
}
