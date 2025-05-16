package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tiendasuplementacion.model.Product
import com.example.tiendasuplementacion.model.Category
import com.example.tiendasuplementacion.model.CategoryProduct
import com.example.tiendasuplementacion.viewmodel.ProductViewModel
import com.example.tiendasuplementacion.viewmodel.CategoryViewModel
import com.example.tiendasuplementacion.viewmodel.CategoryProductViewModel
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    categoryProductViewModel: CategoryProductViewModel = viewModel(),
    productId: Long = 0L
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(productId != 0L) }

    val categories by categoryViewModel.categories.observeAsState(emptyList())
    val error by productViewModel.error.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        categoryViewModel.fetchCategories()
        if (isEditing) {
            try {
                val product = productViewModel.getProductById(productId)
                name = product.name
                description = product.description
                price = product.price.toString()
                stock = product.stock.toString()
                imageUrl = product.url_image
                // TODO: Set selected category when available
            } catch (e: Exception) {
                errorMessage = "Error al cargar el producto: ${e.message}"
                showError = true
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            errorMessage = it
            showError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEditing) "Editar Producto" else "Crear Producto",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank() && showError,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isBlank() && showError,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    isError = price.isBlank() && showError,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = stock.isBlank() && showError,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la imagen") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = imageUrl.isBlank() && showError,
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val product = Product(
                                    id = if (isEditing) productId else 0,
                                    name = name,
                                    description = description,
                                    price = price.toDoubleOrNull() ?: 0.0,
                                    stock = stock.toIntOrNull() ?: 0,
                                    url_image = imageUrl
                                )
                                
                                if (isEditing) {
                                    productViewModel.updateProduct(productId, product)
                                } else {
                                    val createdProduct = productViewModel.createProductSuspend(product)
                                    selectedCategory?.let { category ->
                                        categoryProductViewModel.create(
                                            CategoryProduct(
                                                category_id = category.id,
                                                product_id = createdProduct.id
                                            )
                                        )
                                    }
                                }
                                
                                navController.navigate("products") {
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Log.e("ProductFormScreen", "Error al ${if (isEditing) "editar" else "crear"} producto", e)
                                errorMessage = e.message ?: "Error al ${if (isEditing) "editar" else "crear"} el producto"
                                showError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading && name.isNotBlank() && description.isNotBlank() && 
                             price.isNotBlank() && stock.isNotBlank() && 
                             imageUrl.isNotBlank() && selectedCategory != null
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (isEditing) "Guardar Cambios" else "Guardar", color = Color.White)
                    }
                }
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
} 