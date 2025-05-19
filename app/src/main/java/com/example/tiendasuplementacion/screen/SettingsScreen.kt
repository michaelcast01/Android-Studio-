package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.SettingViewModel
import com.example.tiendasuplementacion.viewmodel.AuthViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    settingViewModel: SettingViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val settings by settingViewModel.settings.observeAsState(emptyList())
    val userSetting = settings.find { it.id == currentUser?.setting_id }

    LaunchedEffect(currentUser?.setting_id) {
        currentUser?.setting_id?.let { settingId ->
            settingViewModel.fetchSettings()
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Configuraciones",
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.padding(16.dp))
        
        userSetting?.let { setting ->
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Nombre: ${setting.name}")
                Text("Ciudad: ${setting.city}")
                Text("Teléfono: ${setting.phone}")
                Text("Dirección: ${setting.address}")
            }
        } ?: run {
            Text("No se encontraron configuraciones")
        }
    }
}

