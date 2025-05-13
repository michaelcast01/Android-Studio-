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
import com.example.tiendasuplementacion.viewmodel.CategoryViewModel
import com.example.tiendasuplementacion.component.GenericListScreen

@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = viewModel()
) {
    val categories by viewModel.categories.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
    }

    GenericListScreen(
        title = "Categorías",
        items = categories,
        onItemClick = {},
        onCreateClick = { navController.navigate("categoryForm") }
    ) { category ->
        Column(Modifier.padding(8.dp)) {
            Text("Nombre: ${category.name}", fontWeight = FontWeight.Bold)
            Text("Descripción: ${category.description}")
        }
    }
}
