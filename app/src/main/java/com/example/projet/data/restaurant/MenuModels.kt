package com.example.projet.data.restaurant

import kotlinx.serialization.Serializable

@Serializable
data class CrousResponse<T>(
    val success: Boolean,
    val data: T
)

@Serializable
data class MenuDay(
    val date: String,
    val repas: List<RepasData>
)

@Serializable
data class RepasData(
    val type: String,
    val categories: List<CategoryData>
)

@Serializable
data class CategoryData(
    val libelle: String,
    val plats: List<PlatData>
)

@Serializable
data class PlatData(
    val libelle: String
)

data class RestaurantInfo(
    val id: String,
    val name: String,
    val city: String
)

val AvailableRestaurants = listOf(
    RestaurantInfo("2120", "Belfort (Duvillard)", "Belfort"),
    RestaurantInfo("2121", "Sevenans (Resto U)", "Sevenans"),
    RestaurantInfo("2122", "Montbéliard (Portes du Jura)", "Montbéliard"),
    RestaurantInfo("2123", "Belfort (STGI)", "Belfort")
)
