package com.example.ordenesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ordenesapp.ui.theme.OrdenesAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: OrderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrdenesAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenContainer(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContainer(viewModel: OrderViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // Optimización nativa sin boxing

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Sistema de Órdenes", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
                // Pestañas de Navegación Superior
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Nueva Orden", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            viewModel.fetchOrders() // Consulta a Python automáticamente al entrar
                        },
                        text = { Text("Historial", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                OrderScreenContent(viewModel = viewModel)
            } else {
                OrderHistoryScreenContent(viewModel = viewModel)
            }
        }
    }
}

// ==========================================
// PANTALLA 1: CATÁLOGO / NUEVA ORDEN
// ==========================================
@Composable
fun OrderScreenContent(viewModel: OrderViewModel) {
    val items by viewModel.items.collectAsState()
    val apiResponse by viewModel.apiResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.codigo }) { item ->
                ItemRow(
                    item = item,
                    onIncrement = { viewModel.incrementQuantity(item.codigo) },
                    onDecrement = { viewModel.decrementQuantity(item.codigo) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón con barra de progreso mientras envía
        Button(
            onClick = { viewModel.enviarOrden() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Text("Enviando orden...", fontSize = 16.sp)
                }
            } else {
                Text("Enviar Orden", fontSize = 18.sp)
            }
        }
    }

    // Ventana emergente adaptada a tu ApiResponse original (.mensaje)
    apiResponse?.let { response ->
        val isSuccess = response.status == "SUCCESS" || response.status == "RECEIVED"

        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = {
                Text(
                    text = if (isSuccess) "¡Orden Procesada!" else "Error en la Orden",
                    color = if (isSuccess) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = { Text(text = response.mensaje, fontSize = 16.sp) },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissDialog() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun ItemRow(item: Item, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Código: ${item.codigo}", fontSize = 12.sp, color = Color.Gray)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onDecrement,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = item.cantidad.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.Center
                )

                FilledTonalButton(
                    onClick = onIncrement,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// PANTALLA 2: HISTORIAL DE ÓRDENES
// ==========================================
@Composable
fun OrderHistoryScreenContent(viewModel: OrderViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = viewModel.uiState) {
            is OrderUiState.Loading -> CircularProgressIndicator()
            is OrderUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                Button(onClick = { viewModel.fetchOrders() }) { Text("Reintentar") }
            }
            is OrderUiState.Success -> {
                val listaDeOrdenes = state.orders
                if (listaDeOrdenes.isEmpty()) {
                    Text("No tienes órdenes registradas.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = listaDeOrdenes, key = { it.orderId }) { order ->
                            HistoryItemCard(order = order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(order: OrderResponse) {
    var expanded by remember { mutableStateOf(false) }

    // Cambia íconos y colores de acuerdo al estado que procesó Kafka
    val (statusIcon, statusColor) = when (order.estado) {
        "COMPLETADA" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        "RECHAZADA" -> Icons.Default.Error to Color(0xFFF44336)
        else -> Icons.Default.HourglassTop to Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Orden #${order.orderId}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = statusIcon, contentDescription = order.estado, tint = statusColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = order.estado, color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Registrado: ${order.fechaRegistro?.take(16)?.replace("T", " ") ?: "N/A"}", fontSize = 13.sp, color = Color.Gray)

            if (order.estado == "COMPLETADA") {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Factura: ${order.numFactura ?: "Sin número"}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(text = "Entrega: ${order.fechaEntrega ?: "Pendiente"}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }

            // Desplegar artículos usando el molde unificado OrderItemHistory
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Text(text = "Artículos comprendidos:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    order.items.forEach { item: OrderItemHistory ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "• Cod: ${item.code ?: item.productId ?: "Desconocido"}", fontSize = 13.sp)
                            Text(text = "Cant: ${item.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}