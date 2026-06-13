package com.example.projet.ui.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.data.restaurant.AvailableRestaurants
import com.example.projet.data.restaurant.CrousApiService
import com.example.projet.data.restaurant.RestaurantInfo
import com.example.projet.data.restaurant.RestaurantMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MenuState {
    object Idle : MenuState()
    object Loading : MenuState()
    data class Success(val menu: RestaurantMenu) : MenuState()
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
            try {
                val response = apiService.getMenu(_selectedRestaurant.value.id)
                if (response.success) {
                    _menuState.value = MenuState.Success(response.data)
                } else {
                    _menuState.value = MenuState.Error("Impossible de charger le menu.")
                }
            } catch (e: Exception) {
                _menuState.value = MenuState.Error("Erreur réseau : ${e.localizedMessage}")
            }
        }
    }
}
