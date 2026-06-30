package com.example.ordenesapp

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface OrderApiService {
    @POST("/orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<ApiResponse>

    @GET("/orders")
    suspend fun getOrders(): List<OrderResponse>

    companion object {
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

// ==========================================
// 🌟 ESTADOS PARA EL HISTORIAL (COLOCAR AQUÍ)
// ==========================================

sealed interface OrderUiState {
    object Loading : OrderUiState
    data class Success(val orders: List<OrderResponse>) : OrderUiState
    data class Error(val message: String) : OrderUiState
}

data class OrderResponse(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("items") val items: List<OrderItemHistory>,
    @SerializedName("estado") val estado: String,
    @SerializedName("fecha_registro") val fechaRegistro: String?,
    @SerializedName("num_factura") val numFactura: String?,
    @SerializedName("fecha_entrega") val fechaEntrega: String?
)

data class OrderItemHistory(
    @SerializedName("code") val code: String?,
    @SerializedName("product_id") val productId: String?,
    @SerializedName("quantity") val quantity: Int
)