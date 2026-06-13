package com.example.projet.ui.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.data.restaurant.AvailableRestaurants
import com.example.projet.data.restaurant.CrousApiService
import com.example.projet.data.restaurant.MenuDay
import com.example.projet.data.restaurant.RestaurantInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

sealed class MenuState {
    object Idle : MenuState()
    object Loading : MenuState()
    data class Success(val menuDays: List<MenuDay>) : MenuState()
    data class Error(val message: String) : MenuState()
}

class RestaurantViewModel : ViewModel() {
    private val apiService = CrousApiService.create()

    private val _selectedRestaurant = MutableStateFlow(AvailableRestaurants[0])
    val selectedRestaurant: StateFlow<RestaurantInfo> = _selectedRestaurant.asStateFlow()

    private val _menuState = MutableStateFlow<MenuState>(MenuState.Idle)
    val menuState: StateFlow<MenuState> = _menuState.asStateFlow()

    init {
        fetchMenu()
    }

    fun selectRestaurant(restaurant: RestaurantInfo) {
        _selectedRestaurant.value = restaurant
        fetchMenu()
    }

    fun fetchMenu() {
        viewModelScope.launch {
            _menuState.value = MenuState.Loading
            val restaurantId = _selectedRestaurant.value.id
            
            try {
                // 1. Tenter de récupérer le menu pour AUJOURD'HUI
                val todayDate = Date()
                val formats = listOf("dd-MM-yyyy", "yyyy-MM-dd")
                
                for (format in formats) {
                    try {
                        val sdf = SimpleDateFormat(format, Locale.US)
                        val dateStr = sdf.format(todayDate)
                        val response = apiService.getMenuByDate(restaurantId, dateStr)
                        if (response.success && response.data.repas.isNotEmpty()) {
                            _menuState.value = MenuState.Success(listOf(response.data))
                            return@launch
                        }
                    } catch (e: Exception) {
                        // Ignorer et tenter le format suivant
                    }
                }

                // 2. Si aujourd'hui échoue, tenter de récupérer la LISTE COMPLÈTE
                try {
                    val listResponse = apiService.getMenuList(restaurantId)
                    if (listResponse.success && listResponse.data.isNotEmpty()) {
                        _menuState.value = MenuState.Success(listResponse.data)
                        return@launch
                    }
                } catch (e: HttpException) {
                    // Si 404 sur la liste, c'est fréquent pour certains restos (Belfort)
                } catch (e: Exception) {
                    // Erreur technique diverse
                }

                // 3. Fallback : Si on n'a rien trouvé, afficher un message explicatif au lieu d'un 404 brut
                _menuState.value = MenuState.Error("Le CROUS n'a pas encore publié de menu détaillé pour ce restaurant aujourd'hui.")
                
            } catch (e: Exception) {
                // Erreur critique (réseau coupé, etc.)
                _menuState.value = MenuState.Error("Problème de connexion : ${e.localizedMessage}")
            }
        }
    }
}
