package com.homefood.delivery.data.remote

import com.homefood.delivery.data.model.CheckoutRequest
import com.homefood.delivery.data.model.DeliveryZone
import com.homefood.delivery.data.model.Kitchen
import com.homefood.delivery.data.model.LoginRequest
import com.homefood.delivery.data.model.LoginResponse
import com.homefood.delivery.data.model.MenuItem
import com.homefood.delivery.data.model.NewMenuRequest
import com.homefood.delivery.data.model.Order
import com.homefood.delivery.data.model.RegisterRequest
import com.homefood.delivery.data.model.RegisterResponse
import com.homefood.delivery.data.model.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Maps 1:1 to the controllers in HomeFoodDelivery.Api. */
interface ApiService {

    // ----- Auth -----
    @POST("api/Auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/Auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    // ----- Tech parks -----
    @GET("api/DeliveryZones")
    suspend fun getTechParks(): Response<List<DeliveryZone>>

    // ----- Customer browse -----
    @GET("api/users/kitchens/zone/{zoneId}")
    suspend fun getKitchens(@Path("zoneId") zoneId: Int): Response<List<Kitchen>>

    @GET("api/DailyMenus/kitchen/{cookId}")
    suspend fun getMenu(@Path("cookId") cookId: Int): Response<List<MenuItem>>

    // ----- Orders (customer) -----
    @POST("api/Orders/checkout")
    suspend fun checkout(@Body body: CheckoutRequest): Response<Unit>

    @GET("api/Orders/customer/{customerId}")
    suspend fun getCustomerOrders(@Path("customerId") customerId: Int): Response<List<Order>>

    // ----- Profile -----
    @PUT("api/users/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body body: UpdateProfileRequest): Response<Unit>

    // ----- Cook: menu management -----
    @GET("api/DailyMenus/mine/{cookId}")
    suspend fun getMyMenus(@Path("cookId") cookId: Int): Response<List<MenuItem>>

    @POST("api/DailyMenus")
    suspend fun createMenu(@Body body: NewMenuRequest): Response<MenuItem>

    @DELETE("api/DailyMenus/{id}")
    suspend fun deleteMenu(@Path("id") id: Int): Response<Unit>

    // ----- Cook: orders -----
    @GET("api/Orders/cook/{cookId}")
    suspend fun getCookOrders(@Path("cookId") cookId: Int): Response<List<Order>>

    @PATCH("api/Orders/{id}/status")
    suspend fun updateOrderStatus(@Path("id") id: Int, @Body status: String): Response<Unit>
}
