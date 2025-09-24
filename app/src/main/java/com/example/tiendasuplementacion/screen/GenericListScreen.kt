package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericListScreen(
    title: String,
    items: List<T>,
    onItemClick: (T) -> Unit,
    onCreateClick: () -> Unit,
    itemContent: @Composable (T) -> Unit
) {
    val primaryText = remember { Color(0xFFF6E7DF) }
    val topBarBg = remember { Color(0xFF18191C) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = primaryText) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = topBarBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            items(items, key = { it?.hashCode() ?: 0 }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onItemClick(item) }
                ) {
                    itemContent(item)
                }
            }
        }
    }
}
