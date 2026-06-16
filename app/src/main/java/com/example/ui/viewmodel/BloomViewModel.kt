package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiService
import com.example.data.db.CartItem
import com.example.data.db.Order
import com.example.data.model.Bouquet
import com.example.data.repository.BloomRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BloomViewModel(private val repository: BloomRepository) : ViewModel() {

    // Tab Navigation
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    fun selectTab(index: Int) {
        _currentTab.value = index
    }

    // Catalog filtering
    val occasions = listOf("All", "Birthday", "Anniversary", "Congratulations", "Thank You", "Sympathy")
    private val _selectedCatalogOccasion = MutableStateFlow("All")
    val selectedCatalogOccasion: StateFlow<String> = _selectedCatalogOccasion.asStateFlow()

    fun setCatalogOccasion(occasion: String) {
        _selectedCatalogOccasion.value = occasion
    }

    val filteredBouquets: StateFlow<List<Bouquet>> = MutableStateFlow<List<Bouquet>>(emptyList()).apply {
        viewModelScope.launch {
            selectedCatalogOccasion.collect { selectedOccasion ->
                val all = repository.catalogBouquets
                value = if (selectedOccasion == "All") {
                    all
                } else {
                    all.filter { it.occasion.equals(selectedOccasion, ignoreCase = true) }
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.catalogBouquets)

    // Creator Tab State (Gemini Builder)
    val designerOccasions = listOf("Anniversary", "Birthday", "Congratulations", "Thank You", "Sympathy", "Get Well Soon", "Love & Romance")
    val designerColors = listOf("Pastel Dream & White", "Romantic Sunset Crimson", "Sunshine Yellow & Peach", "Elegant White & Lavender", "Vibrant Wild Garden Mixed")
    val designerStems = listOf("Crimson Rose", "Pink Rose", "White Lily", "Yellow Tulip", "Golden Sunflower", "Lavender Sprig", "White Daisy", "Eucalyptus")

    private val _creatorOccasion = MutableStateFlow(designerOccasions[0])
    val creatorOccasion = _creatorOccasion.asStateFlow()

    private val _creatorColor = MutableStateFlow(designerColors[0])
    val creatorColor = _creatorColor.asStateFlow()

    private val _creatorPrompt = MutableStateFlow("")
    val creatorPrompt = _creatorPrompt.asStateFlow()

    private val _selectedStems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val selectedStems = _selectedStems.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError = _generationError.asStateFlow()

    private val _generatedBouquet = MutableStateFlow<Bouquet?>(null)
    val generatedBouquet = _generatedBouquet.asStateFlow()

    init {
        updateGeneratedPrompt()
        // Simulate same-day delivery stage progress in background
        _currentTab.value = 0 // default tab
    }

    fun setCreatorOccasion(value: String) {
        _creatorOccasion.value = value
        updateGeneratedPrompt()
    }
    
    fun setCreatorColor(value: String) {
        _creatorColor.value = value
        updateGeneratedPrompt()
    }
    
    fun setCreatorPrompt(value: String) { _creatorPrompt.value = value }
    
    fun updateStemCount(stem: String, count: Int) {
        val current = _selectedStems.value.toMutableMap()
        if (count <= 0) {
            current.remove(stem)
        } else {
            current[stem] = count.coerceAtMost(24)
        }
        _selectedStems.value = current
        updateGeneratedPrompt()
    }

    fun clearStemSelections() {
        _selectedStems.value = emptyMap()
        updateGeneratedPrompt()
    }

    private fun updateGeneratedPrompt() {
        val stemsPart = if (_selectedStems.value.isNotEmpty()) {
            " Specifically incorporate these quantities: " + _selectedStems.value.entries.joinToString(", ") { "${it.value} ${it.key}" } + "."
        } else ""

        val compiled = "A gorgeous same-day floral bouquet suited for a ${_creatorOccasion.value} occasion. The design theme is centered around a ${_creatorColor.value} color palette.$stemsPart Please wrap it elegantly with premium wrapping and matching ribbon."
        _creatorPrompt.value = compiled
    }

    fun clearGeneratedBouquet() { _generatedBouquet.value = null }

    fun generateCustomBouquet() {
        viewModelScope.launch {
            _isGenerating.value = true
            _generationError.value = null
            _generatedBouquet.value = null

            val result = GeminiService.generateCustomBouquet(
                occasion = _creatorOccasion.value,
                colorTheme = _creatorColor.value,
                details = _creatorPrompt.value
            )

            result.fold(
                onSuccess = { bouquet ->
                    _generatedBouquet.value = bouquet
                },
                onFailure = { error ->
                    _generationError.value = error.message ?: "Failed to design bouquet. Please try again."
                }
            )
            _isGenerating.value = false
        }
    }

    // Cart State
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addBouquetToCart(bouquet: Bouquet, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addBouquetToCart(bouquet, quantity)
        }
    }

    fun removeCartItem(itemId: Int) {
        viewModelScope.launch {
            repository.removeCartItem(itemId)
        }
    }

    // Checkout Forms
    private val _recipientName = MutableStateFlow("")
    val recipientName = _recipientName.asStateFlow()

    private val _recipientAddress = MutableStateFlow("")
    val recipientAddress = _recipientAddress.asStateFlow()

    private val _recipientPhone = MutableStateFlow("")
    val recipientPhone = _recipientPhone.asStateFlow()

    private val _deliveryNote = MutableStateFlow("")
    val deliveryNote = _deliveryNote.asStateFlow()

    private val _sameDayShipping = MutableStateFlow(true)
    val sameDayShipping = _sameDayShipping.asStateFlow()

    fun setRecipientName(value: String) { _recipientName.value = value }
    fun setRecipientAddress(value: String) { _recipientAddress.value = value }
    fun setRecipientPhone(value: String) { _recipientPhone.value = value }
    fun setDeliveryNote(value: String) { _deliveryNote.value = value }
    fun setSameDayShipping(value: Boolean) { _sameDayShipping.value = value }

    fun checkout() {
        val name = _recipientName.value.trim()
        val address = _recipientAddress.value.trim()
        val phone = _recipientPhone.value.trim()
        val note = _deliveryNote.value.trim()
        val sameDay = _sameDayShipping.value
        val items = cartItems.value

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty() || items.isEmpty()) return

        viewModelScope.launch {
            val subtotal = items.sumOf { it.price * it.quantity }
            val shippingFee = if (sameDay) 9.99 else 4.99
            val total = subtotal + shippingFee

            repository.placeOrder(
                recipientName = name,
                recipientAddress = address,
                recipientPhone = phone,
                deliveryMessage = note,
                sameDayShipping = sameDay,
                cartItems = items,
                totalCost = total
            )

            // Clear checkout form
            _recipientName.value = ""
            _recipientAddress.value = ""
            _recipientPhone.value = ""
            _deliveryNote.value = ""
            
            // Navigate to active orders tab
            _currentTab.value = 3
        }
    }

    // Orders state
    val orders: StateFlow<List<Order>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Simulate same-day delivery stage progress in background
    init {
        viewModelScope.launch {
            orders.collect { orderList ->
                orderList.filter { it.status == "Placed" }.forEach { activeOrder ->
                    launch {
                        delay(8000) // 8s to Preparing
                        repository.updateOrderStatus(activeOrder.id, "Arranging Bouquet")
                        delay(12000) // 12s later to courier dispatch
                        repository.updateOrderStatus(activeOrder.id, "With Dispatch Courier")
                        delay(15000) // 15s later to en route
                        repository.updateOrderStatus(activeOrder.id, "Out for same-day delivery")
                        delay(20000) // 20s later to delivered
                        repository.updateOrderStatus(activeOrder.id, "Delivered")
                    }
                }
            }
        }
    }

    fun clearAllOrders() {
        viewModelScope.launch {
            repository.clearAllOrders()
        }
    }
}

class BloomViewModelFactory(private val repository: BloomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BloomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BloomViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
