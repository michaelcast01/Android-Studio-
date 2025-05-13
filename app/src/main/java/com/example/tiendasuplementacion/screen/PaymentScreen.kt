package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.livedata.observeAsState


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.PaymentViewModel
import com.example.tiendasuplementacion.component.GenericListScreen

@Composable
fun PaymentScreen(navController: NavController, viewModel: PaymentViewModel = viewModel()) {
    val payments by viewModel.payments.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchPayments()
    }

    GenericListScreen(
        title = "Métodos de Pago",
        items = payments,
        onItemClick = {},
        onCreateClick = { navController.navigate("paymentForm") }
    ) { payment ->
        Column(Modifier.padding(8.dp)) {
            Text("Método: ${payment.method}")
        }
    }
}


