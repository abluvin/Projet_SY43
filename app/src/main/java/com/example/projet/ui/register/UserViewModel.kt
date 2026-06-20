package com.example.projet.ui.Register

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.PasswordUtils
import com.example.projet.data.User
import com.example.projet.data.firebase.FireStoreRepository
import com.example.projet.data.firebase.UserFireStoreRepository
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(
        (application as ProjetApplication).database.userDao()
    )
    private val firestoreRepo = UserFireStoreRepository(FireStoreRepository())

    sealed class RegisterState {
        object Idle : RegisterState()
        data class Success(val name: String, val id: Int, val isAdmin: Boolean, val role: String) : RegisterState()
        object EmailExists : RegisterState()
        object InvalidEmail : RegisterState()
    }

    sealed class LoginState {
        object Idle : LoginState()
        data class Success(val name: String, val id: Int, val isAdmin: Boolean, val role: String) : LoginState()
        object InvalidEmail : LoginState()
        object InvalidCredentials : LoginState()
    }

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun register(name: String, email: String, password: String, accessCode: String = "") {
        viewModelScope.launch {
            if (!email.matches(Regex("^[^@]+@utbm\\.fr$"))) {
                _registerState.value = RegisterState.InvalidEmail
                return@launch
            }
            if (repo.getByEmail(email) != null) {
                _registerState.value = RegisterState.EmailExists
                return@launch
            }
            val role = if (accessCode.contains("PROF", ignoreCase = true)) "PROFESSOR" else "STUDENT"
            val isAdmin = accessCode.contains("ADMIN", ignoreCase = true)
            
            val user = User(
                name = name, 
                email = email, 
                password = PasswordUtils.hash(password), 
                isAdmin = isAdmin, 
                role = role
            )

            // 1. Sauvegarde dans Room
            val id = repo.insert(user)
            user.id = id.toInt()

            // 2. Sauvegarde synchronisée dans Firestore
            try {
                Log.d("FirestoreSync", "Tentative d'envoi vers Firestore pour l'utilisateur: ${user.name} (ID: ${user.id})")
                firestoreRepo.createUser(user)
                Log.d("FirestoreSync", "Utilisateur envoyé avec succès sur Firestore")
            } catch (e: Exception) {
                Log.e("FirestoreSync", "Erreur lors de l'envoi vers Firestore: ${e.message}", e)
            }

            _registerState.value = RegisterState.Success(name, user.id, isAdmin, role)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            if (!email.matches(Regex("^[^@]+@utbm\\.fr$"))) {
                _loginState.value = LoginState.InvalidEmail
                return@launch
            }
            val user = repo.login(email, PasswordUtils.hash(password))
            if (user == null) {
                _loginState.value = LoginState.InvalidCredentials
            } else {
                _loginState.value = LoginState.Success(user.name, user.id, user.isAdmin, user.role)
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
        _loginState.value = LoginState.Idle
    }
}
