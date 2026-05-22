package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CompressorPassword
import com.example.data.CompressorPasswordRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompressorPasswordViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CompressorPasswordRepository

    init {
        val database = AppDatabase.getDatabase(application)
        val dao = database.compressorPasswordDao()
        repository = CompressorPasswordRepository(dao)
        
        // Populate sample data so the app has rich and illustrative contents on first boot
        viewModelScope.launch {
            repository.allPasswords.first() // trigger initial flow collect
            insertSampleDataIfNeeded()
        }
    }

    // Full database flow
    val allPasswords: StateFlow<List<CompressorPassword>> = repository.allPasswords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique fields for dynamic suggestions autocompletion
    val uniqueBrands: StateFlow<List<String>> = repository.uniqueBrands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueMachineModels: StateFlow<List<String>> = repository.uniqueMachineModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueControlBoards: StateFlow<List<String>> = repository.uniqueControlBoards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uniqueClients: StateFlow<List<String>> = repository.uniqueClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State of search and selected filters
    val searchQuery = MutableStateFlow("")
    val selectedBrandFilter = MutableStateFlow<String?>(null)
    val selectedClientFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter = MutableStateFlow<String?>(null)

    // Derived filtered reactive list
    val filteredPasswords: StateFlow<List<CompressorPassword>> = combine(
        allPasswords,
        searchQuery,
        selectedBrandFilter,
        selectedClientFilter,
        selectedCategoryFilter
    ) { passwords, query, brand, client, category ->
        passwords.filter { item ->
            // Search criteria
            val matchesQuery = query.isEmpty() ||
                    item.machineBrand.contains(query, ignoreCase = true) ||
                    item.machineModel.contains(query, ignoreCase = true) ||
                    item.controlBoardModel.contains(query, ignoreCase = true) ||
                    item.clientName.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)

            // Selectable Filter criteria
            val matchesBrand = brand == null || item.machineBrand.equals(brand, ignoreCase = true)
            val matchesClient = client == null || item.clientName.equals(client, ignoreCase = true)
            val matchesCategory = category == null || item.category.equals(category, ignoreCase = true)

            matchesQuery && matchesBrand && matchesClient && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Data mutation actions
    fun savePassword(
        brand: String,
        machineModel: String,
        controlBoard: String,
        client: String,
        code: String,
        category: String,
        notes: String,
        idToEdit: Int? = null
    ) {
        viewModelScope.launch {
            val entry = CompressorPassword(
                id = idToEdit ?: 0,
                machineBrand = brand.trim(),
                machineModel = machineModel.trim(),
                controlBoardModel = controlBoard.trim(),
                clientName = client.trim(),
                accessCode = code.trim(),
                category = category.trim(),
                notes = notes.trim(),
                lastUpdated = System.currentTimeMillis()
            )
            if (idToEdit == null) {
                repository.insert(entry)
            } else {
                repository.update(entry)
            }
        }
    }

    fun deletePassword(password: CompressorPassword) {
        viewModelScope.launch {
            repository.delete(password)
        }
    }

    // Injects representative initial records for standard brands (Atlas Copco, Kaeser, Sullair)
    private suspend fun insertSampleDataIfNeeded() {
        val currentList = repository.allPasswords.first()
        if (currentList.isEmpty()) {
            val samples = listOf(
                CompressorPassword(
                    machineBrand = "Atlas Copco",
                    machineModel = "GA 37 VSD+",
                    controlBoardModel = "Elektronikon Mk5 Touch",
                    clientName = "Metalúrgica Del Plata S.A.",
                    accessCode = "1928",
                    category = "Servicio",
                    notes = "Nivel de acceso técnico para calibración de presiones de arranque y parada. No modificar presiones límites de fábrica."
                ),
                CompressorPassword(
                    machineBrand = "Atlas Copco",
                    machineModel = "GA 75 VSD",
                    controlBoardModel = "Elektronikon MKIV",
                    clientName = "Siderurgia Minera",
                    accessCode = "2835",
                    category = "Servicio",
                    notes = "Clave para calibrar el deshumidificador integrado y ciclos de marcha en vacío."
                ),
                CompressorPassword(
                    machineBrand = "Atlas Copco",
                    machineModel = "GA 37 VSD+",
                    controlBoardModel = "Elektronikon Mk5 Touch",
                    clientName = "Alimentos Patagónicos S.R.L.",
                    accessCode = "1928",
                    category = "Usuario",
                    notes = "Muestra que el mismo modelo GA 37 VSD+ puede estar asignado a Alimentos Patagónicos con los códigos estándar."
                ),
                CompressorPassword(
                    machineBrand = "Kaeser",
                    machineModel = "CSD 125",
                    controlBoardModel = "Sigma Control 2",
                    clientName = "Plásticos del Sur",
                    accessCode = "8642",
                    category = "Fábrica",
                    notes = "Código maestro de Kaeser Sigma Control 2 para restaurar a parámetros térmicos iniciales de fábrica."
                ),
                CompressorPassword(
                    machineBrand = "Kaeser",
                    machineModel = "ASD 40",
                    controlBoardModel = "Sigma Control Basic",
                    clientName = "Industrias Lácteas Mendoza",
                    accessCode = "4512",
                    category = "Servicio",
                    notes = "Parámetros de aviso de mantenimiento preventivo y límites de filtro de aire."
                ),
                CompressorPassword(
                    machineBrand = "Sullair",
                    machineModel = "LS16",
                    controlBoardModel = "Supervisor Controller",
                    clientName = "Química del Norte S.A.",
                    accessCode = "9951",
                    category = "Servicio",
                    notes = "Autenticación para programar ciclos automáticos de vaciado de condensado."
                )
            )
            for (sample in samples) {
                repository.insert(sample)
            }
        }
    }
}
