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
import com.homefood.delivery.ui.screens.CookRegisterScreen
import com.homefood.delivery.ui.screens.LoginScreen
import com.homefood.delivery.ui.screens.MenuScreen
import com.homefood.delivery.ui.screens.PaymentScreen
import com.homefood.delivery.ui.screens.RegisterScreen
import com.homefood.delivery.ui.screens.TechParkScreen
import com.homefood.delivery.ui.screens.cook.CookHome
import com.homefood.delivery.ui.screens.customer.CustomerHome

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val COOK_REGISTER = "cookRegister"
    const val TECH_PARKS = "techparks"
    const val CUSTOMER_HOME = "customerHome"
    const val COOK_HOME = "cookHome"
    const val MENU = "menu/{cookId}/{kitchenName}"
    const val CART = "cart"
    const val PAYMENT = "payment"

    fun menu(cookId: Int, kitchenName: String) =
        "menu/$cookId/${android.net.Uri.encode(kitchenName)}"
}

/** Where a logged-in user belongs based on role + whether a tech park is set. */
fun homeRouteFor(session: SessionManager): String = when {
    session.isCook -> Routes.COOK_HOME
    session.zoneId != 0 -> Routes.CUSTOMER_HOME
    else -> Routes.TECH_PARKS
}

@Composable
fun AppNav() {
    val context = LocalContext.current
    val session = remember { SessionManager(context) }
    val navController = rememberNavController()

    val start = if (session.isLoggedIn) homeRouteFor(session) else Routes.LOGIN

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.LOGIN) { LoginScreen(session, navController) }
        composable(Routes.REGISTER) { RegisterScreen(session, navController) }
        composable(Routes.COOK_REGISTER) { CookRegisterScreen(session, navController) }
        composable(Routes.TECH_PARKS) { TechParkScreen(session, navController) }
        composable(Routes.CUSTOMER_HOME) { CustomerHome(session, navController) }
        composable(Routes.COOK_HOME) { CookHome(session, navController) }

        composable(
            route = Routes.MENU,
            arguments = listOf(
                navArgument("cookId") { type = NavType.IntType },
                navArgument("kitchenName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cookId = backStackEntry.arguments?.getInt("cookId") ?: 0
            val kitchenName = backStackEntry.arguments?.getString("kitchenName") ?: "Kitchen"
            MenuScreen(cookId = cookId, kitchenName = kitchenName, navController = navController)
        }

        composable(Routes.CART) { CartScreen(session, navController) }
        composable(Routes.PAYMENT) { PaymentScreen(session, navController) }
    }
}
