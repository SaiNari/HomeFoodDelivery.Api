package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AssistChip
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
import com.homefood.delivery.data.model.Order
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class OrdersViewModel : ViewModel() {
    var orders by mutableStateOf<List<Order>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load(customerId: Int) {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getCustomerOrders(customerId)
                if (res.isSuccessful) orders = res.body().orEmpty()
                else error = "Could not load your orders."
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
fun OrdersScreen(session: SessionManager, navController: NavController) {
    val vm: OrdersViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load(session.userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My orders") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        session.clear()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        when {
            vm.loading -> LoadingView(Modifier.padding(padding))
            vm.error != null -> MessageView(vm.error!!, Modifier.padding(padding))
            vm.orders.isEmpty() -> MessageView("You have no orders yet.", Modifier.padding(padding))
            else -> LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(vm.orders) { order -> OrderCard(order) }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.dailyMenu?.dishName ?: "Order #${order.orderId}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                AssistChip(onClick = {}, label = { Text(order.orderStatus) })
            }
            Text(
                "Qty ${order.quantity}  ·  ₹%.0f".format(order.totalPrice),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Payment: ${order.paymentStatus}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
