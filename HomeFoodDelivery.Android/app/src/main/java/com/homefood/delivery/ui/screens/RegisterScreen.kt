package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.model.DeliveryZone
import com.homefood.delivery.data.model.RegisterRequest
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    var zones by mutableStateOf<List<DeliveryZone>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun loadZones() {
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getTechParks()
                if (res.isSuccessful) zones = res.body().orEmpty()
            } catch (_: Exception) { /* zones stay empty; user sees no options */ }
        }
    }

    fun register(req: RegisterRequest, onSuccess: (userId: Int) -> Unit) {
        error = null
        if (req.fullName.isBlank() || req.phoneNumber.isBlank()) {
            error = "Name and phone are required"; return
        }
        if (req.zoneId == 0) { error = "Please select your tech park"; return }
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
fun RegisterScreen(session: SessionManager, navController: NavController) {
    val vm: RegisterViewModel = viewModel()
    LaunchedEffect(Unit) { vm.loadZones() }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf<DeliveryZone?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text("Create account") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Full name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Phone number") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedZone?.techParkName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Your tech park") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    vm.zones.forEach { zone ->
                        DropdownMenuItem(
                            text = { Text(zone.techParkName) },
                            onClick = { selectedZone = zone; expanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = address, onValueChange = { address = it },
                label = { Text("Office / desk address (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = pincode, onValueChange = { if (it.length <= 6) pincode = it },
                label = { Text("Pincode (optional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val req = RegisterRequest(
                        fullName = name.trim(),
                        phoneNumber = phone.trim(),
                        userRole = "Customer",
                        addressText = address.trim(),
                        zoneId = selectedZone?.zoneId ?: 0,
                        pincode = pincode.ifBlank { null }
                    )
                    vm.register(req) { userId ->
                        session.userId = userId
                        session.fullName = name.trim()
                        session.zoneId = selectedZone?.zoneId ?: 0
                        navController.navigate(Routes.KITCHENS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                enabled = !vm.loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vm.loading) CircularProgressIndicator(Modifier.height(20.dp))
                else Text("Create account")
            }
        }
    }
}
