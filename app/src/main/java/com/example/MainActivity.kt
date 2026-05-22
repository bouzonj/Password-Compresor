package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CompressorPassword
import com.example.ui.CompressorPasswordViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CompressorPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Main Container aligning fully to safe areas
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompressorPassScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CompressorPassScreen(viewModel: CompressorPasswordViewModel) {
    val context = LocalContext.current
    
    // Collecting reactive list of filtered passwords
    val passwords by viewModel.filteredPasswords.collectAsStateWithLifecycle()
    val allPasswordsRaw by viewModel.allPasswords.collectAsStateWithLifecycle()

    // Query states
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedBrandFilter by viewModel.selectedBrandFilter.collectAsStateWithLifecycle()
    val selectedClientFilter by viewModel.selectedClientFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()

    // Unique lists for autocompletion
    val brands by viewModel.uniqueBrands.collectAsStateWithLifecycle()
    val machineModels by viewModel.uniqueMachineModels.collectAsStateWithLifecycle()
    val boards by viewModel.uniqueControlBoards.collectAsStateWithLifecycle()
    val clients by viewModel.uniqueClients.collectAsStateWithLifecycle()

    // UI form state for adding/editing password
    var showFormDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<CompressorPassword?>(null) }

    // Dialog state variables
    var brandInput by remember { mutableStateOf("") }
    var machineModelInput by remember { mutableStateOf("") }
    var controlBoardInput by remember { mutableStateOf("") }
    var clientInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Servicio") }
    var notesInput by remember { mutableStateOf("") }

    // Function to launch dialog for adding new
    fun openAddDialog() {
        editingEntry = null
        brandInput = ""
        machineModelInput = ""
        controlBoardInput = ""
        clientInput = ""
        passwordInput = ""
        categoryInput = "Servicio"
        notesInput = ""
        showFormDialog = true
    }

    // Function to launch dialog for editing existing
    fun openEditDialog(entry: CompressorPassword) {
        editingEntry = entry
        brandInput = entry.machineBrand
        machineModelInput = entry.machineModel
        controlBoardInput = entry.controlBoardModel
        clientInput = entry.clientName
        passwordInput = entry.accessCode
        categoryInput = entry.category
        notesInput = entry.notes
        showFormDialog = true
    }

    Scaffold(
        topBar = {
            HeaderSection(
                allCount = allPasswordsRaw.size,
                filteredCount = passwords.size,
                uniqueClientsCount = allPasswordsRaw.distinctBy { it.clientName }.size
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("add_password_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir contraseña")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Clave", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_text_field"),
                placeholder = { Text("Buscar marca, modelo, cliente, notas...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )

            // Horizontal Filter Chips Block
            FilterBar(
                brands = brands,
                clients = clients,
                selectedBrand = selectedBrandFilter,
                selectedClient = selectedClientFilter,
                selectedCategory = selectedCategoryFilter,
                onBrandSelect = { viewModel.selectedBrandFilter.value = it },
                onClientSelect = { viewModel.selectedClientFilter.value = it },
                onCategorySelect = { viewModel.selectedCategoryFilter.value = it }
            )

            // Password Content List or Empty State
            if (passwords.isEmpty()) {
                EmptyStateView(
                    isFiltering = searchQuery.isNotEmpty() || selectedBrandFilter != null || selectedClientFilter != null || selectedCategoryFilter != null,
                    onClearFilters = {
                        viewModel.searchQuery.value = ""
                        viewModel.selectedBrandFilter.value = null
                        viewModel.selectedClientFilter.value = null
                        viewModel.selectedCategoryFilter.value = null
                    },
                    onAddPrompt = { openAddDialog() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = passwords,
                        key = { it.id }
                    ) { item ->
                        PasswordCard(
                            item = item,
                            onEdit = { openEditDialog(item) },
                            onDelete = { viewModel.deletePassword(item) }
                        )
                    }
                }
            }
        }
    }

    // Modal Form Dialog for Adding & Editing entries
    if (showFormDialog) {
        Dialog(
            onDismissRequest = { showFormDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .animateContentSize(),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingEntry == null) "Nueva Clave de Placa" else "Editar Clave de Placa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showFormDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Machine Brand Field with context suggestions
                    Text(
                        text = "Marca de Máquina *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = brandInput,
                        onValueChange = { brandInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("brand_input_field"),
                        placeholder = { Text("Ej. Atlas Copco, Kaeser, Sullair...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Embedded Suggestions on Focus for Brand
                    SuggestionRow(items = brands, currentInput = brandInput, onSelect = { brandInput = it })

                    Spacer(modifier = Modifier.height(12.dp))

                    // Machine Model Field with context suggestions
                    Text(
                        text = "Modelo de Máquina *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = machineModelInput,
                        onValueChange = { machineModelInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("machine_model_input_field"),
                        placeholder = { Text("Ej. GA 37 VSD+, CSD 125, LS16...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        shape = RoundedCornerShape(12.dp)
                    )
                    SuggestionRow(items = machineModels, currentInput = machineModelInput, onSelect = { machineModelInput = it })

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Board Model Field with context suggestions
                    // Representing "modelo de placa de control"
                    Text(
                        text = "Modelo de Placa de Control *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = controlBoardInput,
                        onValueChange = { controlBoardInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("board_input_field"),
                        placeholder = { Text("Ej. Elektronikon MK5, Sigma Control 2...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp)
                    )
                    SuggestionRow(items = boards, currentInput = controlBoardInput, onSelect = { controlBoardInput = it })

                    Spacer(modifier = Modifier.height(12.dp))

                    // Client/Customer Name Field
                    Text(
                        text = "Cliente *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = clientInput,
                        onValueChange = { clientInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("client_input_field"),
                        placeholder = { Text("Ej. Metalúrgica Del Plata, Química Norte...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        shape = RoundedCornerShape(12.dp)
                    )
                    SuggestionRow(items = clients, currentInput = clientInput, onSelect = { clientInput = it })
                    Text(
                        text = "Un mismo modelo puede registrarse para diferentes clientes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector Row (Servicio, Usuario, Fábrica, Especial)
                    Text(
                        text = "Nivel de Acceso / Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Servicio", "Usuario", "Fábrica", "Especial")
                        categories.forEach { cat ->
                            val isSelected = categoryInput == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { categoryInput = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password/Key Field
                    Text(
                        text = "Contraseña o Código de Acceso *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("access_code_input_field"),
                        placeholder = { Text("Ej. 1928, 8642, admin123...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notes / Comments Field
                    Text(
                        text = "Notas y Comentarios Especiales",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("notes_input_field"),
                        placeholder = { Text("Ej. Clave para programar el transductor de presión...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showFormDialog = false },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancelar")
                        }
                        
                        Button(
                            onClick = {
                                if (brandInput.isBlank() || machineModelInput.isBlank() ||
                                    controlBoardInput.isBlank() || clientInput.isBlank() ||
                                    passwordInput.isBlank()
                                ) {
                                    Toast.makeText(
                                        context,
                                        "Por favor, complete todos los campos requeridos (*)",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    viewModel.savePassword(
                                        brand = brandInput,
                                        machineModel = machineModelInput,
                                        controlBoard = controlBoardInput,
                                        client = clientInput,
                                        code = passwordInput,
                                        category = categoryInput,
                                        notes = notesInput,
                                        idToEdit = editingEntry?.id
                                    )
                                    showFormDialog = false
                                    Toast.makeText(
                                        context,
                                        if (editingEntry == null) "Clave creada exitosamente" else "Clave actualizada exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier.testTag("save_password_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar clave")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(allCount: Int, filteredCount: Int, uniqueClientsCount: Int) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .statusBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Compressor Icon Background circular styling
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Industrial App Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Passwords Compresores",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Códigos de control industrial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Short Dashboard Analytics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryStat(
                    label = "Total Claves",
                    value = allCount.toString(),
                    icon = Icons.Default.Key,
                    tint = MaterialTheme.colorScheme.primary
                )
                SummaryStat(
                    label = "Clientes",
                    value = uniqueClientsCount.toString(),
                    icon = Icons.Default.Business,
                    tint = MaterialTheme.colorScheme.secondary
                )
                SummaryStat(
                    label = "Filtrados",
                    value = filteredCount.toString(),
                    icon = Icons.Default.FilterList,
                    tint = if (filteredCount != allCount) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SummaryStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun FilterBar(
    brands: List<String>,
    clients: List<String>,
    selectedBrand: String?,
    selectedClient: String?,
    selectedCategory: String?,
    onBrandSelect: (String?) -> Unit,
    onClientSelect: (String?) -> Unit,
    onCategorySelect: (String?) -> Unit
) {
    val showBrandDropdown = remember { mutableStateOf(false) }
    val showClientDropdown = remember { mutableStateOf(false) }
    val showCategoryDropdown = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand selector chip
            item {
                Box {
                    FilterChip(
                        selected = selectedBrand != null,
                        onClick = { showBrandDropdown.value = true },
                        label = { Text(selectedBrand ?: "Todas las Marcas") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                    DropdownMenu(
                        expanded = showBrandDropdown.value,
                        onDismissRequest = { showBrandDropdown.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las marcas") },
                            onClick = {
                                onBrandSelect(null)
                                showBrandDropdown.value = false
                            }
                        )
                        brands.forEach { brand ->
                            DropdownMenuItem(
                                text = { Text(brand) },
                                onClick = {
                                    onBrandSelect(brand)
                                    showBrandDropdown.value = false
                                }
                            )
                        }
                    }
                }
            }

            // Client selector chip
            item {
                Box {
                    FilterChip(
                        selected = selectedClient != null,
                        onClick = { showClientDropdown.value = true },
                        label = { Text(selectedClient ?: "Todos los Clientes") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                    DropdownMenu(
                        expanded = showClientDropdown.value,
                        onDismissRequest = { showClientDropdown.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los clientes") },
                            onClick = {
                                onClientSelect(null)
                                showClientDropdown.value = false
                            }
                        )
                        clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client) },
                                onClick = {
                                    onClientSelect(client)
                                    showClientDropdown.value = false
                                }
                            )
                        }
                    }
                }
            }

            // Category selector chip
            item {
                Box {
                    FilterChip(
                        selected = selectedCategory != null,
                        onClick = { showCategoryDropdown.value = true },
                        label = { Text(selectedCategory ?: "Todos los niveles") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown.value,
                        onDismissRequest = { showCategoryDropdown.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los niveles") },
                            onClick = {
                                onCategorySelect(null)
                                showCategoryDropdown.value = false
                            }
                        )
                        listOf("Servicio", "Usuario", "Fábrica", "Especial").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    onCategorySelect(cat)
                                    showCategoryDropdown.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionRow(items: List<String>, currentInput: String, onSelect: (String) -> Unit) {
    val filtered = items.filter { it.contains(currentInput, ignoreCase = true) && !it.equals(currentInput, ignoreCase = true) }
    if (filtered.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 4.dp, start = 4.dp)) {
            Text(text = "Sugerencias guardadas:", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered) { text ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .clickable { onSelect(text) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordCard(
    item: CompressorPassword,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var codeVisible by remember { mutableStateOf(false) }
    var expandedNotes by remember { mutableStateOf(false) }

    // Color categories mapped visually
    val categoryColor = when (item.category) {
        "Servicio" -> MaterialTheme.colorScheme.primaryContainer
        "Fábrica" -> MaterialTheme.colorScheme.errorContainer
        "Usuario" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val categoryTextColor = when (item.category) {
        "Servicio" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Fábrica" -> MaterialTheme.colorScheme.onErrorContainer
        "Usuario" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("password_card_${item.id}")
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // First Row: Machine Brand + Chip access category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Brand Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.machineBrand.uppercase(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(categoryColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.category.uppercase(),
                        fontSize = 9.sp,
                        color = categoryTextColor,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Second Row: Machine Model (large font) & Control Board Model
            Text(
                text = item.machineModel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "Placa: ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.controlBoardModel,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client row with high visibility
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = "Cliente",
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cliente:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.clientName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ACCESS CODE BOX! High-fidelity security layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Clave",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (codeVisible) item.accessCode else "••••",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    // Toggle visibility
                    IconButton(
                        onClick = { codeVisible = !codeVisible },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (codeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (codeVisible) "Ocultar" else "Mostrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Código Compresor", item.accessCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "¡Código copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar Código",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Note Section expanding
            if (item.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedNotes = !expandedNotes }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notas de operación",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Icon(
                            imageVector = if (expandedNotes) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Desplegar notas",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    AnimatedVisibility(
                        visible = expandedNotes,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Text(
                            text = item.notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
                        )
                    }

                    if (!expandedNotes) {
                        Text(
                            text = item.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Card Action Tray (Modify & Trash buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("edit_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar Registro",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        onDelete()
                        Toast.makeText(context, "Registro eliminado exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("delete_button_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Registro",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(
    isFiltering: Boolean,
    onClearFilters: () -> Unit,
    onAddPrompt: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFiltering) Icons.Default.Info else Icons.Default.Build,
            contentDescription = "Sin registros",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isFiltering) "No se encontraron claves" else "No hay claves guardadas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = if (isFiltering) 
                "Prueba ajustando los criterios del buscador o reseteando los chips de filtros superiores." 
                else "Registra códigos técnicos de nivel de Servicio, Fábrica o Usuario cargando la marca, modelo de máquina, placa y cliente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        if (isFiltering) {
            Button(onClick = onClearFilters) {
                Text("Limpiar filtros de búsqueda")
            }
        } else {
            Button(onClick = onAddPrompt) {
                Icon(Icons.Default.Add, contentDescription = "Nueva clave")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar primera clave")
            }
        }
    }
}
