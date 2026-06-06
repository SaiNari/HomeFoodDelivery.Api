package com.homefood.delivery.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class PaymentViewModel : ViewModel() {
    var placing by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun pay(customerId: Int, method: String, onSuccess: () -> Unit) {
        error = null
        if (CartManager.lines.isEmpty()) { error = "Your cart is empty."; return }
        placing = true
        viewModelScope.launch {
            try {
                val req = CheckoutRequest(
                    customerId = customerId,
                    paymentMethod = method,
                    items = CartManager.lines.map { CartItemDto(it.item.menuId, it.quantity, it.lineTotal) }
                )
                val res = ApiClient.service.checkout(req)
                if (res.isSuccessful) { CartManager.clear(); onSuccess() }
                else error = "Order failed (a dish may be sold out). Review your cart."
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
fun PaymentScreen(session: SessionManager, navController: NavController) {
    val vm: PaymentViewModel = viewModel()
    val context = LocalContext.current
    val methods = listOf("UPI", "Cash on Delivery")
    var selected by remember { mutableStateOf(methods.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Order summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    CartManager.lines.forEach { line ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${line.quantity} × ${line.item.dishName}", style = MaterialTheme.typography.bodyMedium)
                            Text("₹%.0f".format(line.lineTotal), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("₹%.0f".format(CartManager.totalAmount), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Choose payment method", fontWeight = FontWeight.Bold)
            methods.forEach { method ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected == method, onClick = { selected = method })
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == method, onClick = { selected = method })
                    Text(method, style = MaterialTheme.typography.bodyLarge)
                }
            }

            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.pay(session.userId, selected) {
                        Toast.makeText(context, "Order placed! 🎉", Toast.LENGTH_LONG).show()
                        navController.navigate(Routes.CUSTOMER_HOME) {
                            popUpTo(Routes.CUSTOMER_HOME) { inclusive = true }
                        }
                    }
                },
                enabled = !vm.placing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (vm.placing) CircularProgressIndicator(Modifier.height(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                else Text(if (selected == "UPI") "Pay ₹%.0f & place order".format(CartManager.totalAmount) else "Place order (Pay on delivery)")
            }
        }
    }
}
