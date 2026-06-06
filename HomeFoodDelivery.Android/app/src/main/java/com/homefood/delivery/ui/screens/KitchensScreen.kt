package com.homefood.delivery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.homefood.delivery.data.model.Kitchen
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class KitchensViewModel : ViewModel() {
    var kitchens by mutableStateOf<List<Kitchen>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load(zoneId: Int) {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getKitchens(zoneId)
                if (res.isSuccessful) kitchens = res.body().orEmpty()
                else error = "Could not load kitchens."
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
fun KitchensScreen(session: SessionManager, navController: NavController) {
    val vm: KitchensViewModel = viewModel()
    LaunchedEffect(session.zoneId) { vm.load(session.zoneId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchens near you") },
                actions = {
                    IconButton(onClick = { navController.navigate(Routes.TECH_PARKS) }) {
                        Icon(Icons.Default.Place, contentDescription = "Change tech park")
                    }
                    IconButton(onClick = { navController.navigate(Routes.ORDERS) }) {
                        Icon(Icons.Default.Receipt, contentDescription = "My orders")
                    }
                }
            )
        }
    ) { padding ->
        when {
            vm.loading -> LoadingView(Modifier.padding(padding))
            vm.error != null -> MessageView(vm.error!!, Modifier.padding(padding))
            vm.kitchens.isEmpty() -> MessageView(
                "No cooks registered at your tech park yet. Check back soon!",
                Modifier.padding(padding)
            )
            else -> LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(vm.kitchens) { kitchen ->
                    KitchenCard(kitchen) {
                        navController.navigate(Routes.menu(kitchen.cookId, kitchen.kitchenName))
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenCard(kitchen: Kitchen, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(kitchen.kitchenName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            kitchen.kitchenAddress?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.padding(end = 2.dp))
                    Text("%.1f".format(kitchen.rating))
                }
                Text("${kitchen.distanceInKm} km away", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
