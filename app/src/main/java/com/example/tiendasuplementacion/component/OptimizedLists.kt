package com.example.tiendasuplementacion.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.util.CurrencyFormatter
import com.example.tiendasuplementacion.util.PaginationState
import com.example.tiendasuplementacion.util.RefreshState

/**
 * Lista optimizada de productos con paginación
 */
@Composable
fun OptimizedProductGrid(
    products: List<Product>,
    isLoading: Boolean,
    onProductClick: (Product) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    paginationState: PaginationState = remember { PaginationState() },
    refreshState: RefreshState = remember { RefreshState() }
) {
    // Detectar cuando llegamos al final para cargar más
    val listState = rememberLazyGridState()
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null &&
                    lastVisibleIndex >= products.size - 3 &&
                    !paginationState.isLoading &&
                    paginationState.hasMorePages) {
                    onLoadMore()
                }
            }
    }
    Box(modifier = modifier) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = products,
                key = { product -> product.id }
            ) { product ->
                OptimizedProductCard(
                    imageUrl = product.url_image,
                    title = product.name,
                    subtitle = product.description.take(50) + if (product.description.length > 50) "..." else "",
                    price = CurrencyFormatter.format(product.price),
                    onClick = { onProductClick(product) },
                    isLoading = false
                )
            }
            
            // Loading indicator en la parte inferior
            if (paginationState.isLoading) {
                item(span = { GridItemSpan(columns) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Placeholder items para shimmer effect cuando está cargando inicialmente
            if (isLoading && products.isEmpty()) {
                items(count = 6) { _ -> // Mostrar 6 placeholders
                    OptimizedProductCard(
                        imageUrl = "",
                        title = "",
                        subtitle = "",
                        price = "",
                        onClick = { },
                        isLoading = true
                    )
                }
            }
        }
    }
}

/**
 * Lista simple optimizada
 */
@Composable
fun <T> OptimizedLazyList(
    items: List<T>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = items,
                key = { item -> 
                    // Intentar usar un ID único si el objeto lo tiene
                    when (item) {
                        is Product -> item.id
                        else -> item.hashCode()
                    }
                }
            ) { item ->
                itemContent(item)
            }
            
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Estado vacío optimizado con botón de recarga
 */
@Composable
fun EmptyStateWithRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}