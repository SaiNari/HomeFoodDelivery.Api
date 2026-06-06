package com.homefood.delivery.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.homefood.delivery.data.cart.CartManager
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.components.VegBadge
import com.homefood.delivery.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(session: SessionManager, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { navController.navigate(Routes.PAYMENT) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Proceed to payment") }
                }
            }
        }
    ) { padding ->
        if (CartManager.lines.isEmpty()) {
            MessageView("Your cart is empty.\nAdd dishes from a kitchen to get started.", Modifier.padding(padding), emoji = "🛒")
        } else {
            LazyColumn(Modifier.padding(padding).padding(16.dp)) {
                items(CartManager.lines, key = { it.item.menuId }) { line ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                VegBadge(line.item.isVegetarian)
                                Spacer(Modifier.size(8.dp))
                                Column {
                                    Text(line.item.dishName, fontWeight = FontWeight.Bold)
                                    Text(
                                        "₹%.0f each".format(line.item.pricePerPortion),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { CartManager.decrement(line.item.menuId) },
                                    modifier = Modifier.size(36.dp)
                                ) { Icon(Icons.Default.Remove, contentDescription = "Remove one") }
                                Text("${line.quantity}", Modifier.padding(horizontal = 10.dp))
                                FilledTonalIconButton(
                                    onClick = { CartManager.add(line.item) },
                                    enabled = line.quantity < line.item.availablePortions,
                                    modifier = Modifier.size(36.dp)
                                ) { Icon(Icons.Default.Add, contentDescription = "Add one") }
                            }
                        }
                    }
                }
            }
        }
    }
}
