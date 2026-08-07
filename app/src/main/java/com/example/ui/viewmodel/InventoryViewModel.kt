package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.data.InventoryItemEntity
import com.example.data.data.UserEntity
import com.example.data.repository.InventoryRepository
import com.example.data.repository.UserResult
import com.example.data.sms.SmsAlertManager
import com.example.data.sms.SmsSendResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object Inventory : Screen()
    object SmsPermission : Screen()
}

enum class AuthMode {
    LOGIN, CREATE_ACCOUNT
}

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository.getRepository(application)
    val smsAlertManager = SmsAlertManager(application)

    // Current Screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Logged in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Auth Form State
    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    val usernameInput = MutableStateFlow("")
    val passwordInput = MutableStateFlow("")
    val fullNameInput = MutableStateFlow("")

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    // Search and Filter State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    @OptIn(ExperimentalCoroutinesApi::class)
    val inventoryItems: StateFlow<List<InventoryItemEntity>> = searchQuery
        .flatMapLatest { query ->
            repository.searchItemsFlow(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Add / Edit Modal State
    private val _isAddEditModalOpen = MutableStateFlow(false)
    val isAddEditModalOpen: StateFlow<Boolean> = _isAddEditModalOpen.asStateFlow()

    private val _editingItem = MutableStateFlow<InventoryItemEntity?>(null)
    val editingItem: StateFlow<InventoryItemEntity?> = _editingItem.asStateFlow()

    // Delete Confirmation State
    private val _itemToDelete = MutableStateFlow<InventoryItemEntity?>(null)
    val itemToDelete: StateFlow<InventoryItemEntity?> = _itemToDelete.asStateFlow()

    // Feedback Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // SMS Phone Number State
    val alertPhoneNumber = MutableStateFlow(smsAlertManager.getAlertPhoneNumber())

    // SMS Status Banner Message
    private val _smsStatusMessage = MutableStateFlow<String?>(null)
    val smsStatusMessage: StateFlow<String?> = _smsStatusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleAuthMode() {
        _authMode.value = if (_authMode.value == AuthMode.LOGIN) AuthMode.CREATE_ACCOUNT else AuthMode.LOGIN
        _authErrorMessage.value = null
    }

    fun login() {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val result = repository.loginUser(usernameInput.value, passwordInput.value)
            _authLoading.value = false

            when (result) {
                is UserResult.Success -> {
                    _currentUser.value = result.user
                    _currentScreen.value = Screen.Inventory
                    showSnackbar("Welcome back, ${result.user.fullName}!")
                }
                is UserResult.Error -> {
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun createAccount() {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val result = repository.createAccount(
                usernameInput.value,
                passwordInput.value,
                fullNameInput.value
            )
            _authLoading.value = false

            when (result) {
                is UserResult.Success -> {
                    _currentUser.value = result.user
                    _currentScreen.value = Screen.Inventory
                    showSnackbar("Account created! Logged in as ${result.user.username}.")
                }
                is UserResult.Error -> {
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun quickDemoLogin() {
        viewModelScope.launch {
            _authLoading.value = true
            val result = repository.loginUser("admin", "password123")
            _authLoading.value = false

            when (result) {
                is UserResult.Success -> {
                    _currentUser.value = result.user
                    _currentScreen.value = Screen.Inventory
                    showSnackbar("Logged in via Demo Mode.")
                }
                is UserResult.Error -> {
                    // Create demo admin if missing
                    val createResult = repository.createAccount("admin", "password123", "Demo Warehouse Manager")
                    if (createResult is UserResult.Success) {
                        _currentUser.value = createResult.user
                        _currentScreen.value = Screen.Inventory
                        showSnackbar("Logged in via Demo Mode.")
                    } else {
                        _authErrorMessage.value = "Demo login error: ${(createResult as? UserResult.Error)?.message}"
                    }
                }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        usernameInput.value = ""
        passwordInput.value = ""
        fullNameInput.value = ""
        _currentScreen.value = Screen.Login
        showSnackbar("Logged out successfully.")
    }

    fun openAddItemModal() {
        _editingItem.value = null
        _isAddEditModalOpen.value = true
    }

    fun openEditItemModal(item: InventoryItemEntity) {
        _editingItem.value = item
        _isAddEditModalOpen.value = true
    }

    fun closeAddEditModal() {
        _isAddEditModalOpen.value = false
        _editingItem.value = null
    }

    fun saveItem(
        name: String,
        quantityStr: String,
        sku: String,
        category: String,
        location: String,
        notes: String
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                showSnackbar("Item name cannot be empty.")
                return@launch
            }
            val quantity = quantityStr.toIntOrNull() ?: 0
            val cleanSku = if (sku.isBlank()) "SKU-${(100..999).random()}" else sku.trim()
            val cleanCat = if (category.isBlank()) "General" else category.trim()

            val currentEditing = _editingItem.value
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    name = name.trim(),
                    quantity = quantity.coerceAtLeast(0),
                    sku = cleanSku,
                    category = cleanCat,
                    location = location.trim(),
                    notes = notes.trim(),
                    lastUpdated = System.currentTimeMillis()
                )
                repository.updateItem(updated)
                showSnackbar("Updated item '${updated.name}'.")
            } else {
                val newItem = InventoryItemEntity(
                    name = name.trim(),
                    quantity = quantity.coerceAtLeast(0),
                    sku = cleanSku,
                    category = cleanCat,
                    location = location.trim(),
                    notes = notes.trim()
                )
                repository.insertItem(newItem)
                showSnackbar("Added new item '${newItem.name}'.")
            }

            closeAddEditModal()
        }
    }

    fun increaseQuantity(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.increaseQuantity(item.id)
            showSnackbar("Increased '${item.name}' stock to ${item.quantity + 1}.")
        }
    }

    fun decreaseQuantity(item: InventoryItemEntity) {
        viewModelScope.launch {
            if (item.quantity <= 0) {
                showSnackbar("'${item.name}' is already at 0 stock.")
                return@launch
            }
            val newQty = item.quantity - 1
            repository.decreaseQuantity(item.id)

            if (newQty == 0) {
                val smsResult = smsAlertManager.sendZeroStockAlert(item.name, item.sku)
                when (smsResult) {
                    is SmsSendResult.Success -> showSnackbar("ALERT: '${item.name}' reached 0 stock! SMS sent.")
                    is SmsSendResult.PermissionDenied -> showSnackbar("ALERT: '${item.name}' reached 0 stock! (SMS permission not granted)")
                    else -> showSnackbar("ALERT: '${item.name}' reached 0 stock!")
                }
            } else {
                showSnackbar("Decreased '${item.name}' stock to $newQty.")
            }
        }
    }

    fun promptDeleteItem(item: InventoryItemEntity) {
        _itemToDelete.value = item
    }

    fun cancelDeleteItem() {
        _itemToDelete.value = null
    }

    fun confirmDeleteItem() {
        val item = _itemToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteItem(item)
            _itemToDelete.value = null
            showSnackbar("Deleted '${item.name}' from inventory.")
        }
    }

    fun saveAlertPhoneNumber(newNumber: String) {
        smsAlertManager.setAlertPhoneNumber(newNumber)
        alertPhoneNumber.value = newNumber
        showSnackbar("Alert phone number saved.")
    }

    fun sendTestSms() {
        viewModelScope.launch {
            val result = smsAlertManager.sendTestSms()
            when (result) {
                is SmsSendResult.Success -> {
                    _smsStatusMessage.value = result.message
                    showSnackbar(result.message)
                }
                is SmsSendResult.PermissionDenied -> {
                    _smsStatusMessage.value = "SMS Permission Denied. Please tap 'Request Permission' above."
                    showSnackbar("SMS permission required to send text messages.")
                }
                is SmsSendResult.Failed -> {
                    _smsStatusMessage.value = result.message
                    showSnackbar(result.message)
                }
                is SmsSendResult.Disabled -> {
                    _smsStatusMessage.value = result.message
                    showSnackbar(result.message)
                }
            }
        }
    }

    fun showSnackbar(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
