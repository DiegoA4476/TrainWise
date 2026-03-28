package com.example.trainwise.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.ui.theme.*
import com.google.android.gms.maps.model.LatLng
import com.example.trainwise.data.models.Gym
import com.example.trainwise.ui.viwemodels.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: MapViewModel = viewModel(),
    onNavigateToWorkouts: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val apiKey = "AIzaSyBL8O8dVwzdkqhNTT6kU3xQmKiGzVlyq-M"

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
    }

    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location ->
                        location?.let {
                            val userLatLng = LatLng(it.latitude, it.longitude)
                            viewModel.fetchNearbyGyms(userLatLng, apiKey)
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                        } ?: run {
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                lastLoc?.let {
                                    val userLatLng = LatLng(it.latitude, it.longitude)
                                    viewModel.fetchNearbyGyms(userLatLng, apiKey)
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 15f)
                                }
                            }
                        }
                    }
            } catch (e: SecurityException) {

            }
        } else {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigationBar(
                onWorkoutsClick = onNavigateToWorkouts,
                onGuideClick = onNavigateToGuide,
                onProfileClick = onNavigateToProfile
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = locationPermissionState.allPermissionsGranted),
                        uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                    ) {
                        viewModel.gyms.forEach { gym ->
                            Marker(
                                state = MarkerState(position = gym.location),
                                title = gym.name,
                                snippet = "${gym.rating} ★",
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                            )
                        }
                    }
                    if (viewModel.isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Orange)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gyms near  you",
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Orange)
                    }
                }
            }

            if (viewModel.errorMessage != null) {
                item {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }

            if (!viewModel.isLoading && viewModel.gyms.isEmpty() && viewModel.errorMessage == null) {
                item {
                    Text(
                        "No gyms found near you.",
                        color = LightGray,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                }
            }

            items(viewModel.gyms) { gym ->
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    GymCard(gym) {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${gym.name} ${gym.address}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HomeBottomNavigationBar(
    onWorkoutsClick: () -> Unit,
    onGuideClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                selectedTextColor = White,
                indicatorColor = Orange,
                unselectedIconColor = White,
                unselectedTextColor = White
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onGuideClick,
            icon = { Icon(Icons.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onProfileClick,
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = White,
                unselectedTextColor = White,
                indicatorColor = Orange
            )
        )
    }
}

@Composable
fun GymCard(gym: Gym, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Orange.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FitnessCenter, null, tint = Orange)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = gym.name, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = Orange, modifier = Modifier.size(14.dp))
                            val distanceText = if (gym.distance != null) {
                                if (gym.distance < 1000) "${gym.distance.toInt()}m" else String.format("%.1fkm", gym.distance / 1000)
                            } else ""
                            Text(
                                text = " ${gym.rating} • $distanceText",
                                color = LightGray,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }w
                    }
                }
                if (gym.isOpenNow) {
                    Surface(
                        color = Color(0xFF2C1C14),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Open",
                            color = Orange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
