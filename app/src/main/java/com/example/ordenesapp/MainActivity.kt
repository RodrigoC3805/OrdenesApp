package com.example.ordenesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                    OrderScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(viewModel: OrderViewModel) {
    val items by viewModel.items.collectAsState()
    val apiResponse by viewModel.apiResponse.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Selección de Productos") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Lista de Ítems (Scrolleable)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    ItemRow(
                        item = item,
                        onIncrement = { viewModel.incrementQuantity(item.codigo) },
                        onDecrement = { viewModel.decrementQuantity(item.codigo) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón inferior para enviar la orden con indicador de carga
            Button(
                onClick = { viewModel.enviarOrden() },
                enabled = !isLoading, // Se deshabilita si está cargando
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
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
    }

    // Ventana flotante en el centro (Dialog) que formatea la respuesta de la API
    apiResponse?.let { response ->
        val isSuccess = response.status == "SUCCESS" || response.status == "RECEIVED"

        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = {
                Text(
                    text = if (isSuccess) "¡Órden Procesada!" else "Error en la Orden",
                    color = if (isSuccess) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = response.mensaje,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
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
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Información del Ítem (Código y Nombre)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Código: ${item.codigo}", fontSize = 12.sp, color = Color.Gray)
            }

            // Controladores de Cantidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onDecrement,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(35.dp)
                ) {
                    Text("-", fontSize = 18.sp)
                }

                Text(
                    text = item.cantidad.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = TextAlign.Center
                )

                FilledTonalButton(
                    onClick = onIncrement,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(35.dp)
                ) {
                    Text("+", fontSize = 18.sp)
                }
            }
        }
    }
}