package com.example.ordenesapp

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApiService {
    @POST("/orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<ApiResponse>

    companion object {
        // NOTA: '10.0.2.2' es la IP puente para acceder al localhost de tu PC desde el emulador de Android.
        // Si usas un dispositivo físico, cambia esto por la IP local de tu computadora (ej. 192.168.X.X)
        private const val BASE_URL = "http://192.168.1.49:8000/"

        val instance: OrderApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OrderApiService::class.java)
        }
    }
}