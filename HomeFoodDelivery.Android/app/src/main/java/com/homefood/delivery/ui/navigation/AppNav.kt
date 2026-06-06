package com.homefood.delivery.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homefood.delivery.data.session.SessionManager
import com.homefood.delivery.ui.screens.CartScreen
import com.homefood.delivery.ui.screens.KitchensScreen
import com.homefood.delivery.ui.screens.LoginScreen
import com.homefood.delivery.ui.screens.MenuScreen
import com.homefood.delivery.ui.screens.OrdersScreen
import com.homefood.delivery.ui.screens.RegisterScreen
import com.homefood.delivery.ui.screens.TechParkScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TECH_PARKS = "techparks"
    const val KITCHENS = "kitchens"
    const val MENU = "menu/{cookId}/{kitchenName}"
    const val CART = "cart"
    const val ORDERS = "orders"

    fun menu(cookId: Int, kitchenName: String) =
        "menu/$cookId/${android.net.Uri.encode(kitchenName)}"
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val navController = rememberNavController()

    val start = if (session.isLoggedIn) Routes.KITCHENS else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.LOGIN) {
            LoginScreen(session = session, navController = navController)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(session = session, navController = navController)
        }

        composable(Routes.TECH_PARKS) {
            TechParkScreen(session = session, navController = navController)
        }

        composable(Routes.KITCHENS) {
            KitchensScreen(session = session, navController = navController)
        }

        composable(
            route = Routes.MENU,
            arguments = listOf(
                navArgument("cookId") { type = NavType.IntType },
                navArgument("kitchenName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cookId = backStackEntry.arguments?.getInt("cookId") ?: 0
            val kitchenName = backStackEntry.arguments?.getString("kitchenName") ?: "Kitchen"
            MenuScreen(
                cookId = cookId,
                kitchenName = kitchenName,
                navController = navController
            )
        }

        composable(Routes.CART) {
            CartScreen(session = session, navController = navController)
        }

        composable(Routes.ORDERS) {
            OrdersScreen(session = session, navController = navController)
        }
    }
}
