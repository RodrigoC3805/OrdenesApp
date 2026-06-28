package com.example.ordenesapp

import com.google.gson.annotations.SerializedName

// Estructura interna de la UI
data class Item(
    val codigo: String,
    val nombre: String,
    var cantidad: Int
)

// PAYLOADS PARA LA API

data class OrderRequest(
    @SerializedName("items") val items: List<ApiOrderItem>
)

data class ApiOrderItem(
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int
)

data class ApiResponse(
    @SerializedName("status") val status: String, // "RECEIVED"
    @SerializedName("message") val mensaje: String // Mapea "message" de la API a "mensaje" en Kotlin
)

// Estructura para mapear el HTTPException detail de FastAPI
data class FastApiErrorDetail(
    val status: String,
    val message: String
)

data class FastApiErrorResponse(
    val detail: FastApiErrorDetail
)