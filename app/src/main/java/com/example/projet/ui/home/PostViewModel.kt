package com.example.projet.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.Comment
import com.example.projet.data.Post
import com.example.projet.data.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PostRepository
    init {
        val db = (application as ProjetApplication).database
        repo = PostRepository(db.postDao(), db.commentDao())
    }

    val posts: StateFlow<List<Post>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPost(text: String, imageUrl: String?, userId: Int = 1) {
        viewModelScope.launch {
            repo.insert(Post(text = text, idUser = userId, imageUrl = imageUrl))
        }
    }

    fun getComments(postId: Int): Flow<List<Comment>> = repo.getComments(postId)

    fun addComment(postId: Int, userId: Int = 1, content: String) {
        viewModelScope.launch {
            repo.insertComment(Comment(
                postId = postId,
                userId = userId,
                content = content,
                timestamp = System.currentTimeMillis()
            ))
        }
    }
}
