package com.example.projet.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.*
import com.example.projet.data.firebase.FireStoreRepository
import com.example.projet.data.firebase.PostFireStoreRepository
import com.example.projet.data.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PostRepository
    private val firestoreRepo: PostFireStoreRepository

    init {
        val db = (application as ProjetApplication).database
        repo = PostRepository(
            db.postDao(), db.commentDao(),
            db.voiceMessageDao(), db.pollDao(), db.pollOptionDao(), db.pollVoteDao()
        )
        firestoreRepo = PostFireStoreRepository(FireStoreRepository())
    }

    val posts: StateFlow<List<Post>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPost(text: String, imageUrl: String?, userId: Int = 1, ue: String? = null) {
        viewModelScope.launch {
            val post = Post(text = text, idUser = userId, imageUrl = imageUrl, ue = ue)
            val id = repo.insert(post)
            post.id = id.toInt()
            // Optionnel : sauvegarde aussi sur Firestore
            try {
                firestoreRepo.createPost(post)
            } catch (e: Exception) {
                // Gérer l'erreur Firestore sans impacter Room
            }
        }
    }

    fun createVoicePost(filePath: String, duration: Long, userId: Int) {
        viewModelScope.launch {
            val postId = repo.insert(Post(text = "Message vocal", idUser = userId, voiceFilePath = filePath, voiceDuration = duration))
            repo.insertVoiceMessage(VoiceMessage(postId = postId.toInt(), userId = userId, filePath = filePath, duration = duration))
        }
    }

    fun createPostWithPollOptions(question: String, userId: Int, options: List<String>) {
        viewModelScope.launch {
            val postId = repo.insert(Post(text = question, idUser = userId, isPoll = true))
            val pollId = repo.insertPoll(Poll(postId = postId.toInt(), creatorId = userId, question = question))
            options.forEach { repo.insertPollOption(PollOption(pollId = pollId.toInt(), text = it)) }
        }
    }

    fun deletePost(post: Post) { viewModelScope.launch { repo.delete(post) } }

    fun getComments(postId: Int): Flow<List<Comment>> = repo.getComments(postId)

    fun addComment(postId: Int, userId: Int, content: String) {
        viewModelScope.launch {
            repo.insertComment(Comment(postId = postId, userId = userId, content = content, timestamp = System.currentTimeMillis()))
        }
    }

    fun getVoiceMessages(postId: Int): Flow<List<VoiceMessage>> = repo.getVoiceMessages(postId)

    fun addVoiceMessage(postId: Int, userId: Int, filePath: String, duration: Long) {
        viewModelScope.launch {
            repo.insertVoiceMessage(VoiceMessage(postId = postId, userId = userId, filePath = filePath, duration = duration, timestamp = System.currentTimeMillis()))
        }
    }

    fun deleteVoiceMessage(voiceMessage: VoiceMessage) { viewModelScope.launch { repo.deleteVoiceMessage(voiceMessage) } }

    fun getPolls(postId: Int): Flow<List<Poll>> = repo.getPolls(postId)

    fun getPollOptions(pollId: Int): Flow<List<PollOption>> = repo.getPollOptions(pollId)

    fun getPollVotes(optionId: Int): Flow<List<PollVote>> = repo.getPollVotes(optionId)

    fun votePoll(optionId: Int, userId: Int) {
        viewModelScope.launch {
            if (repo.getUserVote(optionId, userId) == null) {
                repo.insertPollVote(PollVote(optionId = optionId, userId = userId, timestamp = System.currentTimeMillis()))
            }
        }
    }
}
