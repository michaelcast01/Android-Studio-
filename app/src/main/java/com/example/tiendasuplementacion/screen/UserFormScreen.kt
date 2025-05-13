package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.model.User
import com.example.tiendasuplementacion.viewmodel.UserViewModel

@Composable
fun UserFormScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var roleId by remember { mutableStateOf("") }
    var settingId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Crear Usuario", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de Usuario") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = roleId,
            onValueChange = { roleId = it },
            label = { Text("ID de Rol") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = settingId,
            onValueChange = { settingId = it },
            label = { Text("ID de Configuración") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                viewModel.createUser(
                    User(
                        id = 0,
                        username = username,
                        email = email,
                        password = password,
                        role_id = roleId.toLongOrNull() ?: 0L,
                        setting_id = settingId.toLongOrNull()
                    )
                )
                navController.navigate("users") {
                    popUpTo("login") { inclusive = true }
                }
            },
            enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Guardar")
        }
    }
}
