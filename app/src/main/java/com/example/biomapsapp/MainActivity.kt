@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.biomapsapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import com.example.biomapsapp.repository.AuthRepository
import com.example.biomapsapp.ui.screen.LoginActivity
import com.example.biomapsapp.ui.theme.BioMapsAppTheme
import com.example.biomapsapp.viewmodel.AuthViewModel
import com.example.biomapsapp.utils.AuthViewModelFactory
import com.example.biomapsapp.data.LoginResponse
import com.example.biomapsapp.utils.AuthPreference
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.biomapsapp.data.local.SafeZoneDatabase
import com.example.biomapsapp.dummy.FakeSafeZoneDao
import com.example.biomapsapp.model.SafeZoneEntity
import com.example.biomapsapp.repository.SafeZoneRepository
import com.example.biomapsapp.ui.component.BottomNavigationBar
import com.example.biomapsapp.ui.component.ZoneCardComponent
import com.example.biomapsapp.utils.BottomNavItem
import com.example.biomapsapp.utils.SafeZoneViewModelFactory
import com.example.biomapsapp.viewmodel.SafeZoneViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var safeZoneViewModel: SafeZoneViewModel
    private lateinit var loginResponse: LoginResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepository()
        val authFactory = AuthViewModelFactory(authRepository)
        authViewModel = ViewModelProvider(this, authFactory).get(AuthViewModel::class.java)

        loginResponse = AuthPreference(this).getData()

        val safeZoneDB = SafeZoneDatabase.getDatabase(applicationContext)
        val safeZoneRepository = SafeZoneRepository(safeZoneDB.safeZoneDao())
        val safeZoneFactory = SafeZoneViewModelFactory(safeZoneRepository)
        safeZoneViewModel = ViewModelProvider(this, safeZoneFactory)[SafeZoneViewModel::class.java]

        if (loginResponse.token.isNullOrBlank()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContent {
            val navController = rememberNavController()
            var showDialog by remember { mutableStateOf(false) }

            BioMapsAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "SafeZoneEntity App",
                                    fontSize = MaterialTheme.typography.titleSmall.fontSize
                                )
                            },
                            actions = {
                                IconButton(onClick = { showDialog = true }) {
                                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomNavItem.Home.route) {
                            HomeScreen(name = loginResponse.user.name, safeZoneViewModel = safeZoneViewModel)
                        }
                        composable(BottomNavItem.Maps.route) {
                            MapsScreen(safeZoneViewModel = safeZoneViewModel)
                        }
                        composable(BottomNavItem.SafeZone.route) {
                            ZoneListScreen(safeZoneviewModel = safeZoneViewModel)
                        }
                    }

                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("Konfirmasi Logout") },
                            text = { Text("Apakah Anda yakin ingin logout?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog = false
                                    AuthPreference(this@MainActivity).removeData()
                                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }) {
                                    Text("Ya")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Batal")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    name: String,
    safeZoneViewModel: SafeZoneViewModel,
    modifier: Modifier = Modifier
) {
    val zones by safeZoneViewModel.zones.observeAsState(emptyList())
    var showMapDialog by remember { mutableStateOf(false) }
    var zoneName by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedZoneForDialog by remember { mutableStateOf<SafeZoneEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showMapDialog = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_location_alt_24),
                    contentDescription = "Tambah Zona"
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = modifier.padding(innerPadding).padding(16.dp)) {
            Row {
                Text("Selamat Datang, ", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = name.split(" ").joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text("Zona Anda:", style = MaterialTheme.typography.titleSmall)

            LazyColumn(modifier = modifier.padding(innerPadding)) {
                items(zones) { zone ->
                    ZoneCardComponent(
                        zone = zone,
                        onClick = {
                            selectedZoneForDialog = zone
                        }
                    )
                }
            }

            selectedZoneForDialog?.let { zone ->
                AlertDialog(
                    onDismissRequest = { selectedZoneForDialog = null },
                    title = {
                        Text(
                            text = zone.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    text = {
                        AndroidView(factory = { context ->
                            MapView(context).apply {
                                onCreate(null)
                                getMapAsync { map ->
                                    val location = LatLng(zone.latitude, zone.longitude)
                                    map.addMarker(MarkerOptions().position(location).title(zone.name))
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                                }
                                onResume()
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp))
                    },
                    confirmButton = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = { selectedZoneForDialog = null }) {
                                Text("Tutup")
                            }
                        }
                    }
                )
            }
        }

        if (showMapDialog) {
            AlertDialog(
                onDismissRequest = { showMapDialog = false },
                text = {
                    Column {
                        AndroidView(factory = { context ->
                            MapView(context).apply {
                                onCreate(null)
                                getMapAsync { map ->
                                    val defaultLocation = LatLng(-6.2, 106.8)
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

                                    map.setOnMapClickListener { latLng ->
                                        map.clear()
                                        map.addMarker(MarkerOptions().position(latLng).title("Zona Baru"))
                                        selectedLocation = latLng
                                    }
                                }
                                onResume()
                            }
                        }, modifier = Modifier.height(300.dp))

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = zoneName,
                            onValueChange = { zoneName = it },
                            label = { Text("Nama Zona Aman") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val lokasi = selectedLocation
                        if (lokasi != null && zoneName.isNotBlank()) {
                            val timestamp = System.currentTimeMillis()
                            val safeZone = SafeZoneEntity(
                                name = zoneName,
                                latitude = lokasi.latitude,
                                longitude = lokasi.longitude,
                                timestamp = timestamp,
                                addedBy = name.split(" ").joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } },
                            )

                            safeZoneViewModel.addZone(safeZone)
                            showMapDialog = false
                        } else {
                            Log.d("MainActivity", "Gagal Menambahkan SafeZone")
                        }
                    }) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMapDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}


@Composable
fun MapsScreen(safeZoneViewModel: SafeZoneViewModel) {

    val zones by safeZoneViewModel.zones.observeAsState(emptyList())

    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                if (zones.isNotEmpty()) {
                    val countMap = zones.groupingBy { LatLng(it.latitude, it.longitude) }.eachCount()
                    val mostFrequent = countMap.maxByOrNull { it.value }?.key
                        ?: LatLng(zones[0].latitude, zones[0].longitude)

                    zones.forEach { zone ->
                        val location = LatLng(zone.latitude, zone.longitude)
                        map.addMarker(MarkerOptions().position(location).title(zone.name))
                    }

                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(mostFrequent, 12f))
                } else {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-6.2, 106.8), 10f))
                }
            }
            mapView.onResume()
            mapView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun ZoneListScreen(safeZoneviewModel: SafeZoneViewModel, modifier: Modifier = Modifier) {
    val zones by safeZoneviewModel.zones.observeAsState(emptyList())
    var selectedZoneForDialog by remember { mutableStateOf<SafeZoneEntity?>(null) }

    Scaffold { innerPadding ->
        LazyColumn(modifier = modifier.padding(innerPadding)) {
            items(zones) { zone ->
                ZoneCardComponent(
                    zone = zone,
                    onClick = {
                        selectedZoneForDialog = zone
                    }
                )
            }
        }

        selectedZoneForDialog?.let { zone ->
            AlertDialog(
                onDismissRequest = { selectedZoneForDialog = null },
                title = {
                    Text(
                        text = zone.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    AndroidView(factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            getMapAsync { map ->
                                val location = LatLng(zone.latitude, zone.longitude)
                                map.addMarker(MarkerOptions().position(location).title(zone.name))
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                            }
                            onResume()
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp))
                },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { selectedZoneForDialog = null }) {
                            Text("Tutup")
                        }
                    }
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ZoneListScreenPreview() {
    val fakeDao = FakeSafeZoneDao()
    val fakeRepository = SafeZoneRepository(fakeDao)
    val viewModel = SafeZoneViewModel(fakeRepository)

    BioMapsAppTheme {
        ZoneListScreen(safeZoneviewModel = viewModel)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ClassicGoogleMapWithInfoWindowPreview() {
    BioMapsAppTheme {
        val fakeDao = FakeSafeZoneDao()
        val fakeRepository = SafeZoneRepository(fakeDao)
        val viewModel = SafeZoneViewModel(fakeRepository)
        MapsScreen(safeZoneViewModel = viewModel)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
fun HomeScreenPreview() {
    BioMapsAppTheme {
        val fakeDao = FakeSafeZoneDao()
        val fakeRepository = SafeZoneRepository(fakeDao)
        val viewModel = SafeZoneViewModel(fakeRepository)
        HomeScreen(
            name = "Bagus",
            safeZoneViewModel = viewModel
        )
    }
}