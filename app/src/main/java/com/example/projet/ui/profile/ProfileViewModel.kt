package com.example.projet.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.PasswordUtils
import com.example.projet.data.UE
import com.example.projet.data.User
import com.example.projet.data.UserUEWithDetails
import com.example.projet.data.repository.PostRepository
import com.example.projet.data.repository.UERepository
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepo: UserRepository
    private val postRepo: PostRepository
    private val ueRepo: UERepository

    init {
        val db = (application as ProjetApplication).database
        userRepo = UserRepository(db.userDao())
        postRepo = PostRepository(db.postDao(), db.commentDao(), db.voiceMessageDao(), db.pollDao(), db.pollOptionDao(), db.pollVoteDao(), db.postLikeDao())
        ueRepo = UERepository(db.ueDao(), db.userUEDao())
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _postCount = MutableStateFlow(0)
    val postCount: StateFlow<Int> = _postCount.asStateFlow()

    private val _allUEs = MutableStateFlow<List<UE>>(emptyList())
    val allUEs: StateFlow<List<UE>> = _allUEs.asStateFlow()

    private val _userUEs = MutableStateFlow<List<UserUEWithDetails>>(emptyList())
    val userUEs: StateFlow<List<UserUEWithDetails>> = _userUEs.asStateFlow()

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
        viewModelScope.launch {
            ueRepo.getAllUEs().collect { _allUEs.value = it }
        }
        viewModelScope.launch {
            ueRepo.getUserUEs(userId).collect { _userUEs.value = it }
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

    fun updateBranch(branch: String) {
        viewModelScope.launch {
            val current = _user.value ?: return@launch
            val updated = current.copy(branch = branch)
            userRepo.update(updated)
            _user.value = updated
        }
    }

    fun addUE(ueId: Int, enseigne: Boolean) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            ueRepo.addUserUE(userId, ueId, enseigne)
        }
    }

    fun removeUE(ueId: Int) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            ueRepo.removeUserUE(userId, ueId)
        }
    }

    fun updateEnseigne(ueId: Int, enseigne: Boolean) {
        viewModelScope.launch {
            val userId = _user.value?.id ?: return@launch
            ueRepo.updateEnseigne(userId, ueId, enseigne)
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
