package com.homefood.delivery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.model.DeliveryZone
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class TechParkViewModel : ViewModel() {
    var zones by mutableStateOf<List<DeliveryZone>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load() {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getTechParks()
                if (res.isSuccessful) zones = res.body().orEmpty()
                else error = "Could not load tech parks."
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
fun TechParkScreen(session: SessionManager, navController: NavController) {
    val vm: TechParkViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load() }

    Scaffold(topBar = { TopAppBar(title = { Text("Choose your tech park") }) }) { padding ->
        when {
            vm.loading -> LoadingView(Modifier.padding(padding))
            vm.error != null -> MessageView(vm.error!!, Modifier.padding(padding))
            vm.zones.isEmpty() -> MessageView("No tech parks available yet.", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.padding(padding).padding(16.dp),
            ) {
                items(vm.zones) { zone ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                session.zoneId = zone.zoneId
                                navController.navigate(Routes.CUSTOMER_HOME) {
                                    popUpTo(Routes.TECH_PARKS) { inclusive = true }
                                }
                            }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(zone.techParkName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                zone.servicingNeighborhoods,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
