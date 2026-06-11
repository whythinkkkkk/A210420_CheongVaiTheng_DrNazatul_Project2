package com.example.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.util.UUID

// Represents a single item in the shopping cart
data class CartItem(
    val name: String,
    val price: Double,
    val quantity: Int
) {
    val subtotal: Double get() = price * quantity
}

data class OrderUiState(
    val itemName: String = "",
    val itemPrice: Double = 0.00,
    val quantity: Int = 1,
    val totalAmount: Double = 0.0,
    val isLoggedIn: Boolean = false,
    val cartItems: List<CartItem> = emptyList(),
    val selectedPaymentMethod: String = "Cash On Delivery",
    val userName: String = "Vai Theng",
    val pendingBillId: String = UUID.randomUUID().toString()   // Generated once per session, refreshed after each order
) {
    val cartTotal: Double get() = cartItems.sumOf { it.subtotal }
    val cartCount: Int get() = cartItems.sumOf { it.quantity }
}

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    var selectedOrderForDetail: OrderEntity? = null
        private set

    // Holds all items of the bill the user tapped in history
    var selectedBillForDetail: List<OrderEntity> = emptyList()
        private set

    // Snapshot of the cart at the moment payment is confirmed, used by ReceiptScreen
    var receiptSnapshot: List<OrderEntity> = emptyList()
        private set
    var receiptPaymentMethod: String = "Cash"
        private set

    // Room stream: Order History
    val orderHistory: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // ── Item selection (for checkout screen) ──────────────────────────────────

    fun selectItem(name: String, price: Double) {
        _uiState.update { it.copy(itemName = name, itemPrice = price) }
    }

    fun updateQuantityInput(quantity: Int) {
        _uiState.update { currentState ->
            val rounded = ((currentState.itemPrice * quantity) * 100).roundToInt() / 100.0
            currentState.copy(quantity = quantity, totalAmount = rounded)
        }
    }

    // ── Cart operations ────────────────────────────────────────────────────────

    // Adds selected item+qty to cart (or increments qty if it already exists)
    fun addToCart(name: String, price: Double, quantity: Int) {
        _uiState.update { state ->
            val existing = state.cartItems.find { it.name == name }
            val updated = if (existing != null) {
                state.cartItems.map {
                    if (it.name == name) it.copy(quantity = it.quantity + quantity) else it
                }
            } else {
                state.cartItems + CartItem(name, price, quantity)
            }
            state.copy(cartItems = updated, itemName = "", itemPrice = 0.0, quantity = 1)
        }
    }

    fun removeFromCart(name: String) {
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.filter { it.name != name })
        }
    }

    fun updateCartItemQuantity(name: String, newQty: Int) {
        if (newQty <= 0) { removeFromCart(name); return }
        _uiState.update { state ->
            state.copy(cartItems = state.cartItems.map {
                if (it.name == name) it.copy(quantity = newQty) else it
            })
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList()) }
    }

    // ── Payment method ─────────────────────────────────────────────────────────

    fun selectPaymentMethod(method: String) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    // ── Complete order (saves all cart items to Room & pushes to Firebase) ─────

    fun completeOrder() {
        val state = _uiState.value
        if (state.cartItems.isEmpty()) return

        // Reuse the billId that was already shown on PaymentScreen
        val billId = state.pendingBillId
        val today = "10/06/2026"

        // Snapshot for ReceiptScreen before cart is cleared
        receiptPaymentMethod = state.selectedPaymentMethod
        receiptSnapshot = state.cartItems.map { cartItem ->
            OrderEntity(
                billId = billId,
                name = cartItem.name,
                quantity = cartItem.quantity,
                totalPrice = cartItem.subtotal,
                date = today
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Using updated repository function 'insertOrder' to handle both SQLite and Firebase
            receiptSnapshot.forEach { repository.insert(it) }

            clearCart()
            _uiState.update {
                it.copy(
                    itemName = "",
                    itemPrice = 0.0,
                    quantity = 1,
                    totalAmount = 0.0,
                    pendingBillId = UUID.randomUUID().toString()  // Fresh ID for next order
                )
            }
        }
    }

    // ── Order history / detail ─────────────────────────────────────────────────

    fun selectOrderForDetail(order: OrderEntity) {
        selectedOrderForDetail = order
    }

    // Update the selectBillForDetail function
    fun selectBillForDetail(items: List<OrderEntity>) {
        selectedBillForDetail = items
        // Also set the first item for backward compatibility
        selectedOrderForDetail = items.firstOrNull()
    }

    // Update the markOrderAsReceived function (Saves to Room and updates Firebase)
    fun markOrderAsReceived() {
        val billItems = selectedBillForDetail
        if (billItems.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            billItems.forEach { order ->
                // Using updated repository function 'updateOrder' to update local + cloud status
                repository.update(order.copy(status = "Received"))
            }
            // Refresh the selectedBillForDetail with updated status
            val updatedItems = billItems.map { it.copy(status = "Received") }
            selectedBillForDetail = updatedItems
            selectedOrderForDetail = updatedItems.firstOrNull()
        }
    }

    // ── Cloud Synchronization ──────────────────────────────────────────────────

    /**
     * Pulls historical down from Firebase into the local Room cache database layout.
     */
    fun syncCloudData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncFromCloud()
        }
    }

    // ── Auth ───────────────────────────────────────────────────────────────────

    fun performLogin(id: String, pass: String): Boolean {
        return if (id == "admin" && pass == "admin123") {
            _uiState.update { it.copy(isLoggedIn = true) }
            // TRIGGER SYNC: Pull down existing database logs from Firebase right upon login success
            syncCloudData()
            true
        } else false
    }

    fun logout() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    // ── Profile ────────────────────────────────────────────────────────────────

    fun updateUserName(name: String) {
        if (name.isNotBlank()) _uiState.update { it.copy(userName = name) }
    }
}