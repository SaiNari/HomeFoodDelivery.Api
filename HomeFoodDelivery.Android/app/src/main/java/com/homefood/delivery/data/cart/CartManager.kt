package com.homefood.delivery.data.cart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.homefood.delivery.data.model.MenuItem

data class CartLine(
    val item: MenuItem,
    val quantity: Int
) {
    val lineTotal: Double get() = item.pricePerPortion * quantity
}

/**
 * In-memory cart shared across screens. Compose observes [lines] directly.
 *
 * For this MVP the cart holds items from a single cook at a time — adding from a
 * different kitchen clears the previous cart (mirrors how Swiggy/Zomato behave).
 */
object CartManager {

    private val _lines = mutableStateListOf<CartLine>()
    val lines: List<CartLine> get() = _lines

    var cookId: Int by mutableStateOf(0)
        private set

    val totalAmount: Double get() = _lines.sumOf { it.lineTotal }
    val totalItems: Int get() = _lines.sumOf { it.quantity }

    fun add(item: MenuItem) {
        if (cookId != 0 && cookId != item.cookId) clear()
        cookId = item.cookId
        val idx = _lines.indexOfFirst { it.item.menuId == item.menuId }
        if (idx >= 0) {
            val line = _lines[idx]
            _lines[idx] = line.copy(quantity = line.quantity + 1)
        } else {
            _lines.add(CartLine(item, 1))
        }
    }

    fun decrement(menuId: Int) {
        val idx = _lines.indexOfFirst { it.item.menuId == menuId }
        if (idx < 0) return
        val line = _lines[idx]
        if (line.quantity <= 1) _lines.removeAt(idx)
        else _lines[idx] = line.copy(quantity = line.quantity - 1)
        if (_lines.isEmpty()) cookId = 0
    }

    fun quantityOf(menuId: Int): Int =
        _lines.firstOrNull { it.item.menuId == menuId }?.quantity ?: 0

    fun clear() {
        _lines.clear()
        cookId = 0
    }
}
