package com.example.data.repository

import com.example.data.db.CartDao
import com.example.data.db.CartItem
import com.example.data.db.Order
import com.example.data.db.OrderDao
import com.example.data.model.Bouquet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BloomRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao
) {
    // Pre-defined Catalog Bouquet Items
    val catalogBouquets = listOf(
        Bouquet(
            id = "pre_sunset_sonata",
            name = "Sunset Sonata",
            description = "A warm, glowing arrangement of sunset colors, perfect to celebrate another beautiful year.",
            price = 49.99,
            occasion = "Birthday",
            stems = mapOf("Coral Roses" to 6, "Orange Gerberas" to 4, "Yellow Tulips" to 3, "Eucalyptus" to 2),
            wrapping = "Eco Kraft Paper",
            ribbon = "Coral Satin"
        ),
        Bouquet(
            id = "pre_royal_velvet",
            name = "Royal Velvet",
            description = "Deep velvety crimson roses paired with sweet lavender, celebrating enduring romance.",
            price = 64.99,
            occasion = "Anniversary",
            stems = mapOf("Crimson Roses" to 12, "Purple Lilacs" to 4, "White Carnations" to 3, "Baby's Breath" to 2),
            wrapping = "Teal Waxed Paper",
            ribbon = "Champagne Gold Silk"
        ),
        Bouquet(
            id = "pre_pure_grace",
            name = "Pure Grace",
            description = "An exquisite arrangement of snowy lilies and delicate pink tulips to mark grand moments.",
            price = 54.99,
            occasion = "Congratulations",
            stems = mapOf("White Lilies" to 5, "Pink Tulips" to 6, "White Spray Roses" to 3, "Eucalyptus" to 2),
            wrapping = "Soft Pink Linen Fabric",
            ribbon = "Ivory Organza"
        ),
        Bouquet(
            id = "pre_sweet_meadow",
            name = "Sweet Meadow",
            description = "A bright, cheerful burst of sunflower and daisies to say a warm thank you.",
            price = 39.99,
            occasion = "Thank You",
            stems = mapOf("Sunflowers" to 4, "White Daisies" to 8, "Baby's Breath" to 3, "Chamomile" to 4),
            wrapping = "Natural Rustic Burlap",
            ribbon = "Jute Twine Ribbon"
        ),
        Bouquet(
            id = "pre_serene_solace",
            name = "Serene Solace",
            description = "A peaceful, soothing arrangement in soft whites and comforting lavender.",
            price = 44.99,
            occasion = "Sympathy",
            stems = mapOf("White Roses" to 6, "White Chrysanthemums" to 4, "Lavender Stems" to 5, "Eucalyptus" to 2),
            wrapping = "Matte Slate Gray Paper",
            ribbon = "Soft Slate Organza"
        )
    )

    // Cart Services
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
    val cartBouquets: Flow<List<Bouquet>> = cartDao.getCartItems().map { list ->
        list.map { it.toBouquet() }
    }

    suspend fun addBouquetToCart(bouquet: Bouquet, count: Int = 1) {
        val existing = cartDao.getCartItemByBouquetId(bouquet.id)
        if (existing != null) {
            val updated = existing.copy(
                quantity = existing.quantity + count,
                note = bouquet.deliveryMessage.ifEmpty { existing.note }
            )
            cartDao.insertCartItem(updated)
        } else {
            val item = CartItem(
                bouquetId = bouquet.id,
                name = bouquet.name,
                description = bouquet.description,
                price = bouquet.price,
                stemsString = bouquet.getStemsString(),
                wrapping = bouquet.wrapping,
                ribbon = bouquet.ribbon,
                isCustom = bouquet.isCustom,
                quantity = count,
                note = bouquet.deliveryMessage
            )
            cartDao.insertCartItem(item)
        }
    }

    suspend fun removeCartItem(itemId: Int) {
        cartDao.deleteCartItemById(itemId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Order Services
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun placeOrder(
        recipientName: String,
        recipientAddress: String,
        recipientPhone: String,
        deliveryMessage: String,
        sameDayShipping: Boolean,
        cartItems: List<CartItem>,
        totalCost: Double
    ) {
        val summaryBuilder = StringBuilder()
        cartItems.forEachIndexed { index, item ->
            summaryBuilder.append("${item.quantity}x ${item.name}")
            if (index < cartItems.size - 1) summaryBuilder.append(", ")
        }

        val order = Order(
            recipientName = recipientName,
            recipientAddress = recipientAddress,
            recipientPhone = recipientPhone,
            deliveryMessage = deliveryMessage,
            sameDayShipping = sameDayShipping,
            totalCost = totalCost,
            itemsSummary = summaryBuilder.toString(),
            status = "Placed"
        )
        orderDao.insertOrder(order)
        cartDao.clearCart()
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun clearAllOrders() {
        orderDao.clearAllOrders()
    }
}
