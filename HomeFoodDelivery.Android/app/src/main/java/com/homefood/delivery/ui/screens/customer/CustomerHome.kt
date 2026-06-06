package com.homefood.delivery.ui.screens.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.homefood.delivery.data.cart.CartManager
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.navigation.Routes
import com.homefood.delivery.ui.screens.BrowseTab
import com.homefood.delivery.ui.screens.CustomerOrdersContent
import com.homefood.delivery.ui.screens.ProfileContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHome(session: SessionManager, navController: NavController) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Hi, ${session.fullName ?: "there"}", "My orders", "Profile")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab]) },
                actions = {
                    if (tab == 0) {
                        IconButton(onClick = { navController.navigate(Routes.CART) }) {
                            BadgedBox(badge = {
                                if (CartManager.totalItems > 0) Badge { Text("${CartManager.totalItems}") }
                            }) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = null) },
                    label = { Text("Browse") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                0 -> BrowseTab(session, navController)
                1 -> CustomerOrdersContent(session)
                else -> ProfileContent(session, navController)
            }
        }
    }
}
