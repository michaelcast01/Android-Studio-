package com.example.tiendasuplementacion.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import com.example.tiendasuplementacion.viewmodel.UiEvent
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    categoryProductViewModel: CategoryProductViewModel = viewModel(),
    productId: Long = 0L
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var stock by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    // store only category id to keep it saveable
    var selectedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var isEditing by rememberSaveable { mutableStateOf(productId != 0L) }
    val scrollState = rememberScrollState()

    val MAX_NAME_LENGTH = 100
    val MAX_DESCRIPTION_LENGTH = 255
    val MAX_IMAGE_URL_LENGTH = 255

    val categories by categoryViewModel.categories.collectAsState(initial = emptyList())
    val error by productViewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // derive selectedCategory from id to avoid holding whole object in saveable state
    val selectedCategory = remember(selectedCategoryId, categories) { selectedCategoryId?.let { id -> categories.find { it.id == id } } }

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
                // If the relation exists elsewhere, it could be set here. Keep as null by default.
            } catch (e: Exception) {
                errorMessage = "Error al cargar el producto: ${e.message}"
                showError = true
            }
        }
    }

    // Collect one-shot UI events from the ViewModel
    LaunchedEffect(Unit) {
        productViewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.ShowError -> {
                    errorMessage = event.message
                    showError = true
                }
                is UiEvent.Navigate -> {
                    navController.navigate(event.route) { launchSingleTop = true }
                }
                is UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
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
        // Snackbar host for one-shot snackbars emitted from ViewModel
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(scrollState),
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
                    onValueChange = { if (it.length <= MAX_NAME_LENGTH) name = it },
                    label = { Text("Nombre") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank() && showError,
                    supportingText = { Text("${name.length}/$MAX_NAME_LENGTH") },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= MAX_DESCRIPTION_LENGTH) description = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isBlank() && showError,
                    supportingText = { Text("${description.length}/$MAX_DESCRIPTION_LENGTH") },
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
                    onValueChange = { if (it.length <= MAX_IMAGE_URL_LENGTH) imageUrl = it },
                    label = { Text("URL de la imagen") },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = imageUrl.isBlank() && showError,
                    supportingText = { Text("${imageUrl.length}/$MAX_IMAGE_URL_LENGTH") },
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
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Use derived state to minimize recompositions for enabled check
                val isFormValid by remember(name, description, price, stock, imageUrl, selectedCategoryId) {
                    derivedStateOf {
                        name.isNotBlank() && description.isNotBlank() && price.isNotBlank() && stock.isNotBlank() && imageUrl.isNotBlank() && selectedCategoryId != null
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                // Validaciones antes de crear/editar
                                when {
                                    name.length > MAX_NAME_LENGTH -> throw IllegalArgumentException("El nombre no puede tener más de $MAX_NAME_LENGTH caracteres")
                                    description.length > MAX_DESCRIPTION_LENGTH -> throw IllegalArgumentException("La descripción no puede tener más de $MAX_DESCRIPTION_LENGTH caracteres")
                                    imageUrl.length > MAX_IMAGE_URL_LENGTH -> throw IllegalArgumentException("La URL de la imagen no puede tener más de $MAX_IMAGE_URL_LENGTH caracteres")
                                }

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
                                errorMessage = when {
                                    e.message?.contains("value too long") == true -> "Uno o más campos exceden la longitud máxima permitida"
                                    else -> e.message ?: "Error al ${if (isEditing) "editar" else "crear"} el producto"
                                }
                                showError = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading && isFormValid
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