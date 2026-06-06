package com.homefood.delivery.data.model

/**
 * Data classes mirror the JSON returned by HomeFoodDelivery.Api.
 * ASP.NET Core serializes property names as camelCase by default, which
 * matches these field names — so no @SerializedName annotations are needed.
 */

// ---------- Auth ----------

data class LoginRequest(
    val phoneNumber: String? = null,
    val googleId: String? = null
)

data class LoginResponse(
    val message: String? = null,
    val userId: Int = 0,
    val fullName: String? = null,
    val role: String? = null,
    val zoneId: Int? = null,
    val zoneName: String? = null,
    val kitchenName: String? = null,
    val addressText: String? = null
)

data class RegisterRequest(
    val fullName: String,
    val phoneNumber: String,
    val userRole: String,        // "Customer" or "Cook"
    val addressText: String = "",
    val zoneId: Int,
    val pincode: String? = null,
    val googleId: String? = null,
    // Cook-only fields:
    val kitchenName: String? = null,
    val kitchenAddress: String? = null,
    val fssaiLicense: String? = null,
    val fssaiExpiry: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class RegisterResponse(
    val message: String? = null,
    val userId: Int = 0
)

data class UpdateProfileRequest(
    val fullName: String? = null,
    val addressText: String? = null,
    val pincode: String? = null,
    val zoneId: Int? = null,
    val kitchenName: String? = null,
    val kitchenAddress: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

// ---------- Tech parks (delivery zones) ----------

data class DeliveryZone(
    val zoneId: Int = 0,
    val techParkName: String = "",
    val servicingNeighborhoods: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true
)

// ---------- Nearby kitchens (cooks) ----------

data class Kitchen(
    val cookId: Int = 0,
    val kitchenName: String = "",
    val kitchenAddress: String? = null,
    val rating: Double = 0.0,
    val distanceInKm: Double = 0.0
)

// ---------- Menu ----------

data class MenuItem(
    val menuId: Int = 0,
    val cookId: Int = 0,
    val shiftId: Int = 0,
    val menuDate: String = "",
    val dishName: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val isVegetarian: Boolean = true,
    val preparationTimeMinutes: Int = 30,
    val availablePortions: Int = 0,
    val pricePerPortion: Double = 0.0
)

/** Body for a cook creating a new dish (POST /api/DailyMenus). */
data class NewMenuRequest(
    val cookId: Int,
    val shiftId: Int,
    val menuDate: String,
    val dishName: String,
    val description: String? = null,
    val imageUrl: String? = null,
    val isVegetarian: Boolean = true,
    val preparationTimeMinutes: Int = 30,
    val availablePortions: Int,
    val pricePerPortion: Double
)

// ---------- Orders / checkout ----------

data class CartItemDto(
    val menuId: Int,
    val quantity: Int,
    val totalPrice: Double
)

data class CheckoutRequest(
    val customerId: Int,
    val paymentMethod: String = "UPI/COD",
    val items: List<CartItemDto>
)

data class Order(
    val orderId: Int = 0,
    val customerId: Int = 0,
    val menuId: Int = 0,
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val orderStatus: String = "Pending",
    val paymentStatus: String = "Pending",
    val orderTime: String = "",
    val dailyMenu: MenuItem? = null
)

/** Meal shifts seeded in the backend (ShiftId -> name). */
object MealShifts {
    val all = listOf(1 to "Breakfast", 2 to "Lunch", 3 to "Dinner")
    fun name(id: Int): String = all.firstOrNull { it.first == id }?.second ?: "Meal"
}

/** Canonical order lifecycle, used for the status timeline + cook actions. */
object OrderStatus {
    val flow = listOf("Pending", "Accepted", "Preparing", "Out for delivery", "Delivered")

    /** The next status a cook can advance an order to, or null if terminal/cancelled. */
    fun next(current: String): String? {
        val i = flow.indexOf(current)
        return if (i in 0 until flow.lastIndex) flow[i + 1] else null
    }
}
