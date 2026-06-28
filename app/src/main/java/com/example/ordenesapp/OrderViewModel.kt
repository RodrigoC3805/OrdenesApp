package com.example.ordenesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    private val _items = MutableStateFlow(
        listOf(
            Item("PROD001", "Laptop Gamer", 0),
            Item("PROD002", "Mouse Óptico", 0),
            Item("PROD003", "Teclado Mecánico", 0),
            Item("PROD004", "Monitor 24'' FHD", 0)
        )
    )
    val items: StateFlow<List<Item>> = _items

    private val _apiResponse = MutableStateFlow<ApiResponse?>(null)
    val apiResponse: StateFlow<ApiResponse?> = _apiResponse

    fun incrementQuantity(codigo: String) {
        _items.value = _items.value.map {
            if (it.codigo == codigo) it.copy(cantidad = it.cantidad + 1) else it
        }
    }

    fun decrementQuantity(codigo: String) {
        _items.value = _items.value.map {
            if (it.codigo == codigo && it.cantidad > 0) it.copy(cantidad = it.cantidad - 1) else it
        }
    }

    fun dismissDialog() {
        _apiResponse.value = null
    }

    fun enviarOrden() {
        viewModelScope.launch {
            // FILTRADO CLAVE: Solo enviamos a la API los productos que el usuario incrementó (> 0)
            val itemsSeleccionados = _items.value.filter { it.cantidad > 0 }

            // Mapeamos solo los seleccionados al formato de la API
            val apiItems = itemsSeleccionados.map {
                ApiOrderItem(code = it.codigo, name = it.nombre, quantity = it.cantidad)
            }
            val requestPayload = OrderRequest(items = apiItems)

            try {
                val response = OrderApiService.instance.createOrder(requestPayload)
                if (response.isSuccessful && response.body() != null) {
                    _apiResponse.value = response.body()
                } else {
                    val errorBodyString = response.errorBody()?.string()
                    val errorData = parseFastApiError(errorBodyString)
                    _apiResponse.value = ApiResponse(
                        status = errorData?.status ?: "ERROR",
                        mensaje = errorData?.message ?: "Hubo un error al procesar la orden."
                    )
                }
            } catch (e: Exception) {
                _apiResponse.value = ApiResponse(
                    status = "CONNECTION_ERROR",
                    mensaje = "No se pudo conectar con el servidor."
                )
            }
        }
    }

    // Función auxiliar para procesar el JSON del 'detail' devuelto por FastAPI
    private fun parseFastApiError(errorBody: String?): FastApiErrorDetail? {
        return try {
            val gson = Gson()
            val parsed = gson.fromJson(errorBody, FastApiErrorResponse::class.java)
            parsed.detail
        } catch (e: Exception) {
            null
        }
    }
}