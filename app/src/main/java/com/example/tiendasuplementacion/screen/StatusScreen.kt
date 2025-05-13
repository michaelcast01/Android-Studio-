package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.StatusViewModel
import com.example.tiendasuplementacion.component.GenericListScreen

@Composable
fun StatusScreen(
    navController: NavController,
    viewModel: StatusViewModel = viewModel()
) {
    val statuses by viewModel.statuses.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchStatuses()
    }

    GenericListScreen(
        title = "Estados",
        items = statuses,
        onItemClick = {},
        onCreateClick = { navController.navigate("statusForm") } // ← Ya corregido aquí
    ) { status ->
        Column(Modifier.padding(8.dp)) {
            Text("Estado: ${status.name}", fontWeight = FontWeight.Bold)
        }
    }
}
