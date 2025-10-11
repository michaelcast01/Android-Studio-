package com.example.tiendasuplementacion.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.viewmodel.UserViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun UserScreen(
    navController: NavController,
    viewModel: UserViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GenericListScreen(
            title = "Usuarios",
            items = users,
            onItemClick = { },
            onCreateClick = {
                navController.navigate("userForm")
            }
        ) { user ->
            AnimatedVisibility(
                visible = true,
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                Column(Modifier.padding(8.dp)) {
                    Text("Usuario: ${user.username}", fontWeight = FontWeight.Bold)
                    Text("Email: ${user.email}")
                    Text("Rol ID: ${user.role_id}")
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
