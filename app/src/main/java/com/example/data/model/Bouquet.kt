package com.example.data.model

data class Bouquet(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val occasion: String,
    val stems: Map<String, Int>,
    val wrapping: String,
    val ribbon: String,
    val deliveryMessage: String = "",
    val isCustom: Boolean = false
) {
    fun getStemsString(): String {
        return stems.entries.joinToString { "${it.value}x ${it.key}" }
    }
}
