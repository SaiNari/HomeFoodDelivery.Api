package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/** Orders tab content with a per-order status timeline. */
@Composable
fun CustomerOrdersContent(session: SessionManager, modifier: Modifier = Modifier) {
    val vm: OrdersViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load(session.userId) }

    when {
        vm.loading -> LoadingView(modifier)
        vm.error != null -> MessageView(vm.error!!, modifier, emoji = "📡")
        vm.orders.isEmpty() -> MessageView("You have no orders yet.\nBrowse kitchens and place your first!", modifier, emoji = "🧾")
        else -> LazyColumn(modifier.padding(16.dp)) {
            items(vm.orders) { order -> OrderCard(order) }
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
                StatusChip(order.orderStatus)
            }
            Text(
                "Qty ${order.quantity}  ·  ₹%.0f  ·  ${order.paymentStatus}".format(order.totalPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            StatusTimeline(order.orderStatus)
        }
    }
}

/** Horizontal stepper showing where the order is in its lifecycle. */
@Composable
private fun StatusTimeline(current: String) {
    if (current == "Cancelled") {
        Text("Order cancelled", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        return
    }
    val steps = OrderStatus.flow
    val currentIndex = steps.indexOf(current).coerceAtLeast(0)

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        steps.forEachIndexed { i, _ ->
            val done = i <= currentIndex
            val color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            Box(
                Modifier.size(14.dp).clip(RoundedCornerShape(50)).background(color)
            )
            if (i < steps.lastIndex) {
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .padding(horizontal = 2.dp)
                        .background(if (i < currentIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
    Spacer(Modifier.height(4.dp))
    Text(
        steps.getOrElse(currentIndex) { current },
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}
