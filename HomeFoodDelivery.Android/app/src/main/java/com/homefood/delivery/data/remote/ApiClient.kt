package com.homefood.delivery.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Single shared Retrofit instance.
 *
 * BASE_URL options:
 *  - Android emulator -> host machine: "http://10.0.2.2:5287/"
 *  - Physical phone   -> use your PC's LAN IP, e.g. "http://192.168.1.20:5287/"
 *
 * The port 5287 is the HTTP profile of HomeFoodDelivery.Api (see launchSettings.json).
 * Keep the trailing slash.
 */
object ApiClient {

    const val BASE_URL = "http://10.0.2.2:5287/"

    val service: ApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
