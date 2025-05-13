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
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.component.GenericListScreen

@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingViewModel = viewModel()) {
    val settings by viewModel.settings.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchSettings()
    }

    GenericListScreen(
        title = "Configuraciones",
        items = settings,
        onItemClick = {},
        onCreateClick = { navController.navigate("settingForm") }
    ) { setting ->
        Column(Modifier.padding(8.dp)) {
            Text("Nombre: ${setting.name}")
            Text("Ciudad: ${setting.city}")
            Text("Teléfono: ${setting.phone}")
        }
    }
}

