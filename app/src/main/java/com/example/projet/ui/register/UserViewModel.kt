package com.example.projet.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.User
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(
        (application as ProjetApplication).database.userDao()
    )

    sealed class RegisterState {
        object Idle : RegisterState()
        data class Success(val name: String) : RegisterState()
        object EmailExists : RegisterState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            if (repo.getByEmail(email) != null) {
                _registerState.value = RegisterState.EmailExists
                return@launch
            }
            repo.insert(User(name = name, email = email, password = password))
            _registerState.value = RegisterState.Success(name)
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}
