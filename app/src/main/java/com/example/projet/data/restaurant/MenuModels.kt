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
    val city: String,
    val latitude: Double,
    val longitude: Double
)

val AvailableRestaurants = listOf(
   // RestaurantInfo("2120", "Belfort (Duvillard)", "Belfort", 47.6394, 6.8632),
    RestaurantInfo("2123", "Belfort (STGI)", "Belfort", 47.6447, 6.8386),
    RestaurantInfo("2121", "Sevenans (Resto U)", "Sevenans", 47.6289, 6.8561),
    RestaurantInfo("2122", "Montbéliard (Portes du Jura)", "Montbéliard", 47.5053, 6.7982)
)

