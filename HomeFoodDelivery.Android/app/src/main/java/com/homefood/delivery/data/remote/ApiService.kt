package com.homefood.delivery.data.remote

import com.homefood.delivery.data.model.CheckoutRequest
import com.homefood.delivery.data.model.DeliveryZone
import com.homefood.delivery.data.model.Kitchen
import com.homefood.delivery.data.model.LoginRequest
import com.homefood.delivery.data.model.LoginResponse
import com.homefood.delivery.data.model.MenuItem
import com.homefood.delivery.data.model.Order
import com.homefood.delivery.data.model.RegisterRequest
import com.homefood.delivery.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Maps 1:1 to the controllers in HomeFoodDelivery.Api. */
interface ApiService {

    @POST("api/Auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/Auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/DeliveryZones")
    suspend fun getTechParks(): Response<List<DeliveryZone>>

    @GET("api/users/kitchens/zone/{zoneId}")
    suspend fun getKitchens(@Path("zoneId") zoneId: Int): Response<List<Kitchen>>

    @GET("api/DailyMenus/kitchen/{cookId}")
    suspend fun getMenu(@Path("cookId") cookId: Int): Response<List<MenuItem>>

    @POST("api/Orders/checkout")
    suspend fun checkout(@Body body: CheckoutRequest): Response<Unit>

    @GET("api/Orders/customer/{customerId}")
    suspend fun getCustomerOrders(@Path("customerId") customerId: Int): Response<List<Order>>
}
