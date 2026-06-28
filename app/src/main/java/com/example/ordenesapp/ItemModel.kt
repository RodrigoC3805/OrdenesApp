package com.example.ordenesapp

// Representa un producto en la lista
data class Item(
    val codigo: String,
    val nombre: String,
    var cantidad: Int
)

// Representa la respuesta de la API
data class ApiResponse(
    val status: String,   // "SUCCESS" o "INVALID"
    val mensaje: String
)