package com.homefood.delivery.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.location.LatLng
import com.homefood.delivery.data.location.LocationProvider
import com.homefood.delivery.data.model.DeliveryZone
import com.homefood.delivery.data.model.RegisterRequest
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class CookRegisterViewModel : ViewModel() {
    var zones by mutableStateOf<List<DeliveryZone>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun loadZones() {
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getTechParks()
                if (res.isSuccessful) zones = res.body().orEmpty()
            } catch (_: Exception) {}
        }
    }

    fun register(req: RegisterRequest, onSuccess: (Int) -> Unit) {
        error = null
        if (req.fullName.isBlank() || req.phoneNumber.isBlank()) { error = "Name and phone are required"; return }
        if (req.kitchenName.isNullOrBlank()) { error = "Kitchen name is required"; return }
        if (req.fssaiLicense.isNullOrBlank()) { error = "FSSAI license number is required"; return }
        if (req.zoneId == 0) { error = "Select the tech park you serve"; return }
        loading = true
        viewModelScope.launch {
            try {
                val res = ApiClient.service.register(req)
                val body = res.body()
                if (res.isSuccessful && body != null) onSuccess(body.userId)
                else error = "Could not register. Phone may already be in use."
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally {
                loading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookRegisterScreen(session: SessionManager, navController: NavController) {
    val vm: CookRegisterViewModel = viewModel()
    LaunchedEffect(Unit) { vm.loadZones() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var kitchenName by remember { mutableStateOf("") }
    var kitchenAddress by remember { mutableStateOf("") }
    var fssai by remember { mutableStateOf("") }
    var fssaiExpiry by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf<DeliveryZone?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf<LatLng?>(null) }
    var locating by remember { mutableStateOf(false) }

    fun capture() {
        locating = true
        scope.launch {
            captured = LocationProvider.current(context)
            locating = false
            if (captured == null)
                Toast.makeText(context, "Couldn't get location. Set one in the emulator's location controls.", Toast.LENGTH_LONG).show()
        }
    }
    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) capture() else Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register your kitchen") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Become a HomeFood partner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "IT employees near your tech park will see your kitchen and order your meals.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(name, { name = it }, label = { Text("Your full name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                phone, { phone = it }, label = { Text("Phone number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(kitchenName, { kitchenName = it }, label = { Text("Kitchen name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(kitchenAddress, { kitchenAddress = it }, label = { Text("Kitchen address") }, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                fssai, { fssai = it }, label = { Text("FSSAI license number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                fssaiExpiry, { fssaiExpiry = it },
                label = { Text("FSSAI expiry (YYYY-MM-DD, optional)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedZone?.techParkName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tech park you serve") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    vm.zones.forEach { zone ->
                        DropdownMenuItem(text = { Text(zone.techParkName) }, onClick = { selectedZone = zone; expanded = false })
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    if (LocationProvider.hasPermission(context)) capture()
                    else permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Text(
                    when {
                        locating -> "  Getting location…"
                        captured != null -> "  Location captured ✓"
                        else -> "  Capture kitchen location (GPS)"
                    }
                )
            }

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val req = RegisterRequest(
                        fullName = name.trim(),
                        phoneNumber = phone.trim(),
                        userRole = "Cook",
                        addressText = kitchenAddress.trim(),
                        zoneId = selectedZone?.zoneId ?: 0,
                        kitchenName = kitchenName.trim(),
                        kitchenAddress = kitchenAddress.trim(),
                        fssaiLicense = fssai.trim(),
                        fssaiExpiry = fssaiExpiry.trim().ifBlank { null }?.let { "${it}T00:00:00Z" },
                        latitude = captured?.latitude,
                        longitude = captured?.longitude
                    )
                    vm.register(req) { userId ->
                        session.userId = userId
                        session.fullName = name.trim()
                        session.role = "Cook"
                        session.zoneId = selectedZone?.zoneId ?: 0
                        session.kitchenName = kitchenName.trim()
                        navController.navigate(Routes.COOK_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                enabled = !vm.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vm.loading) CircularProgressIndicator(Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text("Register kitchen")
            }
        }
    }
}
