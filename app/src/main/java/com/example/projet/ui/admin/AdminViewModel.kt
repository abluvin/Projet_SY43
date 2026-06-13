package com.example.projet.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.User
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = UserRepository(
        (application as ProjetApplication).database.userDao()
    )

    val users: StateFlow<List<User>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleAdmin(user: User) {
        viewModelScope.launch { repo.update(user.copy(isAdmin = !user.isAdmin)) }
    }

    fun setRole(user: User, role: String) {
        viewModelScope.launch { repo.update(user.copy(role = role)) }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch { repo.delete(user) }
    }
}
