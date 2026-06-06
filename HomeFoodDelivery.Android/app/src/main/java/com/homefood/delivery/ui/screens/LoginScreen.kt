package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.model.LoginRequest
import com.homefood.delivery.data.model.LoginResponse
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun login(phone: String, onSuccess: (LoginResponse) -> Unit) {
        error = null
        if (phone.isBlank()) { error = "Enter your phone number"; return }
        loading = true
        viewModelScope.launch {
            try {
                val res = ApiClient.service.login(LoginRequest(phoneNumber = phone.trim()))
                val body = res.body()
                if (res.isSuccessful && body != null) {
                    onSuccess(body)
                } else {
                    error = "Account not found. Please register."
                }
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally {
                loading = false
            }
        }
    }
}

@Composable
fun LoginScreen(session: SessionManager, navController: NavController) {
    val vm: LoginViewModel = viewModel()
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HomeFood", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Home-cooked meals, delivered to your tech park",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        vm.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                vm.login(phone) { body ->
                    session.userId = body.userId
                    session.fullName = body.fullName
                    session.zoneId = body.zoneId ?: 0
                    val dest = if (session.zoneId != 0) Routes.KITCHENS else Routes.TECH_PARKS
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            },
            enabled = !vm.loading
        ) {
            if (vm.loading) CircularProgressIndicator(Modifier.height(20.dp))
            else Text("Log in")
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate(Routes.REGISTER) }) {
            Text("New here? Create an account")
        }
    }
}
