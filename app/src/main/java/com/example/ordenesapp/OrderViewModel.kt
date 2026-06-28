package com.example.ordenesapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {

    // Lista de productos hardcodeada
    private val _items = MutableStateFlow(
        listOf(
            Item("PROD001", "Laptop Gamer", 0),
            Item("PROD002", "Mouse Óptico", 0),
            Item("PROD003", "Teclado Mecánico", 0),
            Item("PROD004", "Monitor 24'' FHD", 0)
        )
    )
    val items: StateFlow<List<Item>> = _items

    // Estado para controlar el diálogo de respuesta de la API
    private val _apiResponse = MutableStateFlow<ApiResponse?>(null)
    val apiResponse: StateFlow<ApiResponse?> = _apiResponse

    // Incrementa la cantidad de un ítem
    fun incrementQuantity(codigo: String) {
        _items.value = _items.value.map {
            if (it.codigo == codigo) it.copy(cantidad = it.cantidad + 1) else it
        }
    }

    // Decrementa la cantidad de un ítem (mínimo 0)
    fun decrementQuantity(codigo: String) {
        _items.value = _items.value.map {
            if (it.codigo == codigo && it.cantidad > 0) it.copy(cantidad = it.cantidad - 1) else it
        }
    }

    // Limpia el estado de la respuesta para cerrar el diálogo
    fun dismissDialog() {
        _apiResponse.value = null
    }

    // Simulación del "empaquetado" y envío a la API placeholder
    fun enviarOrden() {
        viewModelScope.launch {
            // Filtramos solo los ítems que tienen cantidad mayor a 0
            val itemsSeleccionados = _items.value.filter { it.cantidad > 0 }

            // Validación local antes de simular el envío
            if (itemsSeleccionados.isEmpty()) {
                _apiResponse.value = ApiResponse(
                    status = "INVALID",
                    mensaje = "La orden está vacía. Debes seleccionar al menos un ítem."
                )
                return@launch
            }

            // --- PLACEHOLDER DE API ---
            // Aquí se empaquetarían los datos (ej: JSON) y se haría un POST HTTP
            delay(1500) // Simula la latencia de red de 1.5 segundos

            // Simulación de respuesta exitosa
            val totalItems = itemsSeleccionados.sumOf { it.cantidad }
            _apiResponse.value = ApiResponse(
                status = "SUCCESS",
                mensaje = "Orden recibida correctamente. Procesados $totalItems ítems en total."
            )
        }
    }
}