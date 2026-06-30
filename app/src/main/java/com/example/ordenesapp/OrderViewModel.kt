package com.example.ordenesapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ❌ AQUÍ BORRAMOS TODO: Ya no declaramos Item, OrderRequest, ApiResponse ni OrderUiState aquí,
// ya que Kotlin las lee automáticamente desde ItemModel.kt y OrderApiService.kt

class OrderViewModel : ViewModel() {

    // 1. Catálogo inicial usando tu clase 'Item' de ItemModel.kt
    // Nota: Agregamos el precio de forma implícita o mapeada si lo necesitas,
    // pero respetando tus atributos obligatorios (codigo, nombre, cantidad)
    private val _items = MutableStateFlow(
        listOf(
            Item("PROD001", "Laptop Gamer", 0),
            Item("PROD002", "Mouse Óptico", 0),
            Item("PROD003", "Teclado Mecánico", 0),
            Item("PROD004", "Monitor 24 FHD", 0)
        )
    )
    val items: StateFlow<List<Item>> = _items.asStateFlow()

    // 2. Estados de control de envío usando tu clase 'ApiResponse' de ItemModel.kt
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _apiResponse = MutableStateFlow<ApiResponse?>(null)
    val apiResponse: StateFlow<ApiResponse?> = _apiResponse.asStateFlow()

    // 3. Estado del historial (Usa la interfaz sellada de OrderApiService.kt o paquete global)
    var uiState: OrderUiState by mutableStateOf(OrderUiState.Loading)
        private set

    // Incrementar cantidad (+) modificando tu propiedad mutable 'var cantidad'
    fun incrementQuantity(codigo: String) {
        _items.value = _items.value.map { item ->
            if (item.codigo == codigo) item.copy(cantidad = item.cantidad + 1) else item
        }
    }

    // Decrementar cantidad (-) asegurando no bajar de 0
    fun decrementQuantity(codigo: String) {
        _items.value = _items.value.map { item ->
            if (item.codigo == codigo && item.cantidad > 0) item.copy(cantidad = item.cantidad - 1) else item
        }
    }

    fun dismissDialog() {
        _apiResponse.value = null
    }

    // Cargar historial desde tu endpoint GET
    fun fetchOrders() {
        viewModelScope.launch {
            uiState = OrderUiState.Loading
            try {
                val list = OrderApiService.instance.getOrders()
                uiState = OrderUiState.Success(list)
            } catch (e: Exception) {
                uiState = OrderUiState.Error("Error al conectar con el servidor: ${e.localizedMessage}")
            }
        }
    }

    // 🚀 ENVIAR ORDEN AL BACKEND (Envia solo seleccionados o lista vacía si todos están en 0)
    fun enviarOrden() {
        viewModelScope.launch {
            _isLoading.value = true // Activa la animación de carga en el botón
            try {
                // 1. Filtramos el catálogo: Solo incluimos los productos con cantidad > 0
                val productosSeleccionados = _items.value.filter { it.cantidad > 0 }

                // 2. Mapeamos al payload de tu API (Si la lista está vacía, enviará un JSON con "items": [])
                val requestBody = OrderRequest(
                    items = productosSeleccionados.map {
                        ApiOrderItem(
                            code = it.codigo,
                            name = it.nombre,
                            quantity = it.cantidad
                        )
                    }
                )

                // 3. Disparamos la petición HTTP hacia Python via Retrofit
                val response = OrderApiService.instance.createOrder(requestBody)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _apiResponse.value = body

                    // Si la API acepta la orden ("RECEIVED" o "SUCCESS"), limpiamos la UI reseteando a 0
                    if (body.status == "RECEIVED" || body.status == "SUCCESS") {
                        _items.value = _items.value.map { it.copy(cantidad = 0) }
                    }
                } else {
                    // 💥 ESCENARIO DEL PAYLOAD VACÍO:
                    // Al enviar la lista vacía [], tu Python ejecutará: raise HTTPException(status_code=400)
                    // Retrofit detectará que el código no es exitoso (2xx) y saltará inmediatamente aquí.
                    _apiResponse.value = ApiResponse("ERROR", "El servidor rechazó la orden (Código: ${response.code()})")
                }
            } catch (e: Exception) {
                // Captura si el backend está apagado o no hay red
                _apiResponse.value = ApiResponse("ERROR", "No se pudo conectar con el backend: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false // Apaga la animación de carga en la UI
            }
        }
    }
}