package com.homefood.delivery.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.homefood.delivery.ui.components.RatingPill
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

/** Browse tab content: search + list of nearby kitchens (sorted by distance). */
@Composable
fun BrowseTab(session: SessionManager, navController: NavController, modifier: Modifier = Modifier) {
    val vm: KitchensViewModel = viewModel()
    LaunchedEffect(session.zoneId) { vm.load(session.zoneId) }
    var query by remember { mutableStateOf("") }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search kitchens") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )

        when {
            vm.loading -> LoadingView()
            vm.error != null -> MessageView(vm.error!!, emoji = "📡")
            vm.kitchens.isEmpty() -> MessageView(
                "No cooks registered at your tech park yet. Check back soon!",
                emoji = "🍳"
            )
            else -> {
                val filtered = vm.kitchens.filter {
                    it.kitchenName.contains(query, ignoreCase = true)
                }
                if (filtered.isEmpty()) {
                    MessageView("No kitchens match \"$query\".", emoji = "🔍")
                } else {
                    LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                        items(filtered) { kitchen ->
                            KitchenCard(kitchen) {
                                navController.navigate(Routes.menu(kitchen.cookId, kitchen.kitchenName))
                            }
                        }
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
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    kitchen.kitchenName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
                RatingPill(kitchen.rating)
            }
            kitchen.kitchenAddress?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "${kitchen.distanceInKm} km from your tech park",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
