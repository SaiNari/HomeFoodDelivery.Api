package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.homefood.delivery.data.cart.CartManager
import com.homefood.delivery.data.model.CartItemDto
import com.homefood.delivery.data.model.CheckoutRequest
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {
    var placing by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun placeOrder(customerId: Int, onSuccess: () -> Unit) {
        error = null
        if (customerId == 0) { error = "Please log in again."; return }
        if (CartManager.lines.isEmpty()) { error = "Your cart is empty."; return }
        placing = true
        viewModelScope.launch {
            try {
                val req = CheckoutRequest(
                    customerId = customerId,
                    paymentMethod = "UPI/COD",
                    items = CartManager.lines.map {
                        CartItemDto(it.item.menuId, it.quantity, it.lineTotal)
                    }
                )
                val res = ApiClient.service.checkout(req)
                if (res.isSuccessful) {
                    CartManager.clear()
                    onSuccess()
                } else {
                    error = "Order failed (a dish may be sold out). Please review your cart."
                }
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally {
                placing = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(session: SessionManager, navController: NavController) {
    val vm: CartViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (CartManager.lines.isNotEmpty()) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleMedium)
                        Text("₹%.0f".format(CartManager.totalAmount), fontWeight = FontWeight.Bold)
                    }
                    vm.error?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            vm.placeOrder(session.userId) {
                                navController.navigate(Routes.ORDERS) {
                                    popUpTo(Routes.KITCHENS)
                                }
                            }
                        },
                        enabled = !vm.placing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (vm.placing) CircularProgressIndicator(Modifier.height(20.dp))
                        else Text("Place order (Pay on delivery / UPI)")
                    }
                }
            }
        }
    ) { padding ->
        if (CartManager.lines.isEmpty()) {
            MessageView("Your cart is empty.\nAdd dishes from a kitchen to get started.", Modifier.padding(padding))
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(CartManager.lines, key = { it.item.menuId }) { line ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(line.item.dishName, fontWeight = FontWeight.Bold)
                                Text(
                                    "₹%.0f each".format(line.item.pricePerPortion),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { CartManager.decrement(line.item.menuId) },
                                    modifier = Modifier.height(36.dp)
                                ) { Icon(Icons.Default.Remove, contentDescription = "Remove one") }
                                Text("${line.quantity}", Modifier.padding(horizontal = 10.dp))
                                FilledTonalIconButton(
                                    onClick = { CartManager.add(line.item) },
                                    enabled = line.quantity < line.item.availablePortions,
                                    modifier = Modifier.height(36.dp)
                                ) { Icon(Icons.Default.Add, contentDescription = "Add one") }
                            }
                            Spacer(Modifier.height(0.dp))
                            Text("₹%.0f".format(line.lineTotal), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            }
        }
    }
}
