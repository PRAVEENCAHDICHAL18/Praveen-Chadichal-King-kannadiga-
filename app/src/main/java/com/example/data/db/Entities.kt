package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Bouquet

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bouquetId: String,
    val name: String,
    val description: String,
    val price: Double,
    val stemsString: String,
    val wrapping: String,
    val ribbon: String,
    val isCustom: Boolean,
    val quantity: Int = 1,
    val note: String = ""
) {
    fun toBouquet(): Bouquet {
        // Parse stems like "12x Red Roses, 3x Lilies"
        val stemsMap = mutableMapOf<String, Int>()
        try {
            stemsString.split(",").forEach { stem ->
                val trimmed = stem.trim()
                if (trimmed.isNotEmpty()) {
                    val parts = trimmed.split("x ")
                    if (parts.size >= 2) {
                        val qty = parts[0].toIntOrNull() ?: 1
                        val name = parts.drop(1).joinToString("x ")
                        stemsMap[name] = qty
                    } else {
                        stemsMap[trimmed] = 1
                    }
                }
            }
        } catch (_: Exception) {}

        return Bouquet(
            id = bouquetId,
            name = name,
            description = description,
            price = price,
            occasion = if (isCustom) "AI Custom" else "Pre-made",
            stems = stemsMap,
            wrapping = wrapping,
            ribbon = ribbon,
            deliveryMessage = note,
            isCustom = isCustom
        )
    }
}

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipientName: String,
    val recipientAddress: String,
    val recipientPhone: String,
    val deliveryMessage: String,
    val sameDayShipping: Boolean,
    val orderTime: Long = System.currentTimeMillis(),
    val totalCost: Double,
    // Store simple serialized representation of items (e.g. names and quantities)
    val itemsSummary: String,
    val status: String = "Placed" // "Placed", "Preparing", "Out for Delivery", "Delivered"
)
