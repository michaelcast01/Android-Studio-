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
import com.example.tiendasuplementacion.viewmodel.UserViewModel

@Composable
fun UserScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel()
) {
    val users by viewModel.users.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    GenericListScreen(
        title = "Usuarios",
        items = users,
        onItemClick = { },
        onCreateClick = { navController.navigate("userForm") }
    ) { user ->
        Column(Modifier.padding(8.dp)) {
            Text("Usuario: ${user.username}", fontWeight = FontWeight.Bold)
            Text("Email: ${user.email}")
            Text("Rol ID: ${user.role_id}")
        }
    }
}
