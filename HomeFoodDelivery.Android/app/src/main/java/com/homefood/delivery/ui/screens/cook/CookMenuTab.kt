package com.homefood.delivery.ui.screens.cook

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homefood.delivery.data.model.MealShifts
import com.homefood.delivery.data.model.MenuItem
import com.homefood.delivery.data.model.NewMenuRequest
import com.homefood.delivery.data.remote.ApiClient
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.components.LoadingView
import com.homefood.delivery.ui.components.MessageView
import com.homefood.delivery.ui.components.VegBadge
import java.time.LocalDate
import kotlinx.coroutines.launch

class CookMenuViewModel : ViewModel() {
    var menus by mutableStateOf<List<MenuItem>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    fun load(cookId: Int) {
        loading = true; error = null
        viewModelScope.launch {
            try {
                val res = ApiClient.service.getMyMenus(cookId)
                if (res.isSuccessful) menus = res.body().orEmpty()
                else error = "Could not load your menu."
            } catch (e: Exception) {
                error = "Cannot reach server. Is the API running? (${e.message})"
            } finally { loading = false }
        }
    }

    fun add(req: NewMenuRequest, cookId: Int) {
        viewModelScope.launch {
            try { ApiClient.service.createMenu(req); load(cookId) } catch (_: Exception) {}
        }
    }

    fun delete(menuId: Int, cookId: Int) {
        viewModelScope.launch {
            try { ApiClient.service.deleteMenu(menuId); load(cookId) } catch (_: Exception) {}
        }
    }
}

@Composable
fun CookMenuContent(session: SessionManager, modifier: Modifier = Modifier) {
    val vm: CookMenuViewModel = viewModel()
    LaunchedEffect(Unit) { vm.load(session.userId) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add dish") }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                vm.loading -> LoadingView()
                vm.error != null -> MessageView(vm.error!!, emoji = "📡")
                vm.menus.isEmpty() -> MessageView("No dishes yet.\nTap “Add dish” to post today's menu.", emoji = "🍲")
                else -> LazyColumn(Modifier.padding(16.dp)) {
                    items(vm.menus) { dish ->
                        DishRow(dish, onDelete = { vm.delete(dish.menuId, session.userId) })
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddDishDialog(
            cookId = session.userId,
            onDismiss = { showAdd = false },
            onAdd = { req -> vm.add(req, session.userId); showAdd = false }
        )
    }
}

@Composable
private fun DishRow(dish: MenuItem, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            VegBadge(dish.isVegetarian)
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(dish.dishName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${MealShifts.name(dish.shiftId)} · ₹%.0f · ${dish.availablePortions} left".format(dish.pricePerPortion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(dish.menuDate.take(10), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddDishDialog(cookId: Int, onDismiss: () -> Unit, onAdd: (NewMenuRequest) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }
    var shiftId by remember { mutableStateOf(2) } // Lunch by default

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a dish") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Dish name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(desc, { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        price, { price = it }, label = { Text("Price ₹") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        portions, { portions = it }, label = { Text("Portions") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(imageUrl, { imageUrl = it }, label = { Text("Image URL (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text("Meal", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealShifts.all.forEach { (id, label) ->
                        FilterChip(selected = shiftId == id, onClick = { shiftId = id }, label = { Text(label) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vegetarian", Modifier.weight(1f))
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = price.toDoubleOrNull() ?: return@TextButton
                    val q = portions.toIntOrNull() ?: return@TextButton
                    if (name.isBlank()) return@TextButton
                    onAdd(
                        NewMenuRequest(
                            cookId = cookId,
                            shiftId = shiftId,
                            menuDate = "${LocalDate.now()}T00:00:00Z",
                            dishName = name.trim(),
                            description = desc.trim().ifBlank { null },
                            imageUrl = imageUrl.trim().ifBlank { null },
                            isVegetarian = isVeg,
                            availablePortions = q,
                            pricePerPortion = p
                        )
                    )
                }
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
