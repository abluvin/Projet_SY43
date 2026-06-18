package com.example.projet.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.PasswordUtils
import com.example.projet.data.User
import com.example.projet.data.repository.PostRepository
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo: UserRepository
    private val postRepo: PostRepository

    init {
        val db = (application as ProjetApplication).database
        userRepo = UserRepository(db.userDao())
        postRepo = PostRepository(db.postDao(), db.commentDao())
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _postCount = MutableStateFlow(0)
    val postCount: StateFlow<Int> = _postCount.asStateFlow()

    private var loadedUserId: Int? = null

    fun load(userId: Int) {
        if (loadedUserId == userId) return
        loadedUserId = userId
        viewModelScope.launch {
            _user.value = userRepo.getById(userId)
        }
        viewModelScope.launch {
            postRepo.getByUser(userId).collect { posts -> _postCount.value = posts.size }
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            val current = _user.value ?: return@launch
            if (newName.isBlank()) return@launch
            val updated = current.copy(name = newName)
            userRepo.update(updated)
            _user.value = updated
        }
    }

    sealed class PasswordResult {
        object Idle : PasswordResult()
        object Success : PasswordResult()
        object WrongCurrent : PasswordResult()
    }

    private val _passwordResult = MutableStateFlow<PasswordResult>(PasswordResult.Idle)
    val passwordResult: StateFlow<PasswordResult> = _passwordResult.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val current = _user.value ?: return@launch
            if (current.password != PasswordUtils.hash(currentPassword)) {
                _passwordResult.value = PasswordResult.WrongCurrent
                return@launch
            }
            val updated = current.copy(password = PasswordUtils.hash(newPassword))
            userRepo.update(updated)
            _user.value = updated
            _passwordResult.value = PasswordResult.Success
        }
    }

    fun resetPasswordResult() {
        _passwordResult.value = PasswordResult.Idle
    }
}
