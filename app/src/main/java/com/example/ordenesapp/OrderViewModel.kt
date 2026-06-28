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
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
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
            // 2. ACTIVAR CARGANDO: Al iniciar la función pasamos el estado a true
            _isLoading.value = true

            val itemsSeleccionados = _items.value.filter { it.cantidad > 0 }

            // Mapeamos los ítems para la API
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
            } finally {
                // 3. DESACTIVAR CARGANDO: El bloque 'finally' garantiza que, pase lo que pase
                // (éxito, error de la API o excepción de red), el estado regrese a false.
                _isLoading.value = false
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