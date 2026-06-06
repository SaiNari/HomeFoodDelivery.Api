package com.homefood.delivery.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.location.LatLng
import com.homefood.delivery.data.location.LocationProvider
import com.homefood.delivery.data.model.UpdateProfileRequest
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    var saving by mutableStateOf(false); private set
    var message by mutableStateOf<String?>(null)

    fun save(userId: Int, body: UpdateProfileRequest, onDone: () -> Unit) {
        saving = true; message = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.updateProfile(userId, body)
                message = if (res.isSuccessful) "Profile saved" else "Save failed"
                if (res.isSuccessful) onDone()
            } catch (e: Exception) {
                message = "Cannot reach server (${e.message})"
            } finally {
                saving = false
            }
        }
    }
}

@Composable
fun ProfileContent(
    session: SessionManager,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val vm: ProfileViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCook = session.isCook

    var name by remember { mutableStateOf(session.fullName ?: "") }
    var address by remember { mutableStateOf(session.addressText ?: "") }
    var kitchenName by remember { mutableStateOf(session.kitchenName ?: "") }
    var captured by remember { mutableStateOf<LatLng?>(null) }
    var locating by remember { mutableStateOf(false) }

    fun capture() {
        locating = true
        scope.launch {
            captured = LocationProvider.current(context)
            locating = false
            if (captured == null) {
                Toast.makeText(context, "Couldn't get location. Set one in the emulator's location controls.", Toast.LENGTH_LONG).show()
            }
        }
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) capture() else Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show() }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("My profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (isCook) "Cook account" else "Customer account",
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = name, onValueChange = { name = it },
            label = { Text("Full name") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isCook) {
            OutlinedTextField(
                value = kitchenName, onValueChange = { kitchenName = it },
                label = { Text("Kitchen name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = address, onValueChange = { address = it },
            label = { Text(if (isCook) "Kitchen address" else "Delivery address") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            onClick = {
                if (LocationProvider.hasPermission(context)) capture()
                else permLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null)
            Spacer(Modifier.height(0.dp))
            Text(
                when {
                    locating -> "  Getting location…"
                    captured != null -> "  Location set ✓ (%.4f, %.4f)".format(captured!!.latitude, captured!!.longitude)
                    else -> "  Update my location (GPS)"
                }
            )
        }

        if (!isCook) {
            OutlinedButton(
                onClick = { navController.navigate(Routes.TECH_PARKS) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Change my tech park") }
        }

        vm.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }

        Button(
            onClick = {
                val body = UpdateProfileRequest(
                    fullName = name.trim().ifBlank { null },
                    addressText = address.trim(),
                    kitchenName = if (isCook) kitchenName.trim().ifBlank { null } else null,
                    kitchenAddress = if (isCook) address.trim() else null,
                    latitude = captured?.latitude,
                    longitude = captured?.longitude
                )
                vm.save(session.userId, body) {
                    session.fullName = name.trim()
                    session.addressText = address.trim()
                    if (isCook) session.kitchenName = kitchenName.trim()
                }
            },
            enabled = !vm.saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (vm.saving) CircularProgressIndicator(Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            else Text("Save changes")
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                session.clear()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Text("  Log out", color = Color.White)
        }
    }
}
