package com.example.projet.data.restaurant

import kotlinx.serialization.Serializable

@Serializable
data class CrousResponse<T>(
    val success: Boolean,
    val data: T
)

@Serializable
data class MenuCategory(
    val categorie: String,
    val plats: List<String>
)

@Serializable
data class RestaurantMenu(
    val midi: List<MenuCategory>? = null,
    val soir: List<MenuCategory>? = null
)

data class RestaurantInfo(
    val id: String,
    val name: String,
    val city: String
)

val AvailableRestaurants = listOf(
    RestaurantInfo("2120", "Duvillard (Belfort)", "Belfort"),
    RestaurantInfo("2122", "Portes du Jura (Montbéliard)", "Montbéliard"),
    RestaurantInfo("2123", "Cafet' STGI (Belfort)", "Belfort")
)
