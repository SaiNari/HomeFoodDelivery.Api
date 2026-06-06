package com.homefood.delivery.ui.screens.cook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.homefood.delivery.data.model.Order
import com.homefood.delivery.data.model.OrderStatus
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.components.StatusChip
import kotlinx.coroutines.launch

class CookOrdersViewModel : ViewModel() {
    var orders by mutableStateOf<List<Order>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load(cookId: Int) {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getCookOrders(cookId)
                if (res.isSuccessful) orders = res.body().orEmpty()
                else error = "Could not load incoming orders."
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally { loading = false }
        }
    }

    fun advance(order: Order, cookId: Int) {
        val next = OrderStatus.next(order.orderStatus) ?: return
        viewModelScope.launch {
            try { ApiClient.service.updateOrderStatus(order.orderId, next); load(cookId) } catch (_: Exception) {}
        }
    }
}

@Composable
fun CookOrdersContent(session: SessionManager, modifier: Modifier = Modifier) {
    val vm: CookOrdersViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load(session.userId) }

    when {
        vm.loading -> LoadingView(modifier)
        vm.error != null -> MessageView(vm.error!!, modifier, emoji = "📡")
        vm.orders.isEmpty() -> MessageView("No orders yet today.\nThey'll show up here in real time.", modifier, emoji = "📭")
        else -> LazyColumn(modifier.padding(16.dp)) {
            items(vm.orders) { order ->
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
                            StatusChip(order.orderStatus)
                        }
                        Text(
                            "Qty ${order.quantity}  ·  ₹%.0f  ·  ${order.paymentStatus}".format(order.totalPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val next = OrderStatus.next(order.orderStatus)
                        if (next != null) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { vm.advance(order, session.userId) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Mark as $next") }
                        }
                    }
                }
            }
        }
    }
}
