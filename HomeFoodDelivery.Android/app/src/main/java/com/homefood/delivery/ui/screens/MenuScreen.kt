package com.homefood.delivery.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.homefood.delivery.data.cart.CartManager
import com.homefood.delivery.data.model.MealShifts
import com.homefood.delivery.data.model.MenuItem
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.components.VegBadge
import com.homefood.delivery.ui.navigation.Routes
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    var menu by mutableStateOf<List<MenuItem>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load(cookId: Int) {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getMenu(cookId)
                if (res.isSuccessful) menu = res.body().orEmpty()
                else error = "Could not load the menu."
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally { loading = false }
        }
    }
}

private enum class VegFilter { All, Veg, NonVeg }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(cookId: Int, kitchenName: String, navController: NavController) {
    val vm: MenuViewModel = viewModel()
    LaunchedEffect(cookId) { vm.load(cookId) }

    var vegFilter by remember { mutableStateOf(VegFilter.All) }
    var shiftFilter by remember { mutableStateOf(0) } // 0 = all

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(kitchenName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (CartManager.totalItems > 0) {
                Button(
                    onClick = { navController.navigate(Routes.CART) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("View cart · ${CartManager.totalItems} item(s) · ₹%.0f".format(CartManager.totalAmount))
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            // Filter chips
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(vegFilter == VegFilter.Veg, { vegFilter = if (vegFilter == VegFilter.Veg) VegFilter.All else VegFilter.Veg }, label = { Text("Veg") })
                FilterChip(vegFilter == VegFilter.NonVeg, { vegFilter = if (vegFilter == VegFilter.NonVeg) VegFilter.All else VegFilter.NonVeg }, label = { Text("Non-veg") })
                MealShifts.all.forEach { (id, label) ->
                    FilterChip(shiftFilter == id, { shiftFilter = if (shiftFilter == id) 0 else id }, label = { Text(label) })
                }
            }

            when {
                vm.loading -> LoadingView()
                vm.error != null -> MessageView(vm.error!!, emoji = "📡")
                vm.menu.isEmpty() -> MessageView("This kitchen has no dishes listed for today.", emoji = "🍽️")
                else -> {
                    val dishes = vm.menu.filter { d ->
                        (vegFilter == VegFilter.All ||
                            (vegFilter == VegFilter.Veg && d.isVegetarian) ||
                            (vegFilter == VegFilter.NonVeg && !d.isVegetarian)) &&
                            (shiftFilter == 0 || d.shiftId == shiftFilter)
                    }
                    if (dishes.isEmpty()) MessageView("No dishes match your filters.", emoji = "🔍")
                    else LazyColumn(Modifier.padding(horizontal = 16.dp)) {
                        items(dishes) { dish -> MenuCard(dish) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(dish: MenuItem) {
    val qty = CartManager.quantityOf(dish.menuId)
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(12.dp)) {
            if (!dish.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = dish.imageUrl,
                    contentDescription = dish.dishName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(84.dp).clip(RoundedCornerShape(8.dp))
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VegBadge(dish.isVegetarian)
                    Spacer(Modifier.width(6.dp))
                    Text(dish.dishName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                dish.description?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹%.0f".format(dish.pricePerPortion), style = MaterialTheme.typography.titleSmall)
                Text(
                    if (dish.availablePortions > 0) "${MealShifts.name(dish.shiftId)} · ${dish.availablePortions} left"
                    else "Sold out",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (dish.availablePortions > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(8.dp))
            QuantityControl(
                qty = qty,
                soldOut = dish.availablePortions <= 0,
                atStockLimit = qty >= dish.availablePortions,
                onAdd = { CartManager.add(dish) },
                onRemove = { CartManager.decrement(dish.menuId) }
            )
        }
    }
}

@Composable
private fun QuantityControl(
    qty: Int,
    soldOut: Boolean,
    atStockLimit: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    if (qty == 0) {
        OutlinedButton(onClick = onAdd, enabled = !soldOut) { Text("Add") }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = onRemove, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Remove one")
            }
            Text("$qty", modifier = Modifier.padding(horizontal = 10.dp))
            FilledTonalIconButton(onClick = onAdd, enabled = !atStockLimit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add one")
            }
        }
    }
}
