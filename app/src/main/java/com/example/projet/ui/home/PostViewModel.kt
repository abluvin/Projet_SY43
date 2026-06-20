package com.example.projet.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.*
import com.example.projet.data.firebase.FireStoreRepository
import com.example.projet.data.firebase.PostFireStoreRepository
import com.example.projet.data.repository.PostRepository
import com.example.projet.data.repository.UERepository
import com.example.projet.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class PostFilter {
    object General : PostFilter()
    object ByBranch : PostFilter()
    data class ByUE(val code: String) : PostFilter()
}

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PostRepository
    private val userRepo: UserRepository
    private val ueRepo: UERepository
    private val firestoreRepo: PostFireStoreRepository

    private val _filter = MutableStateFlow<PostFilter>(PostFilter.General)
    val filter = _filter.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int>(0)

    init {
        val db = (application as ProjetApplication).database
        repo = PostRepository(
            db.postDao(), db.commentDao(),
            db.voiceMessageDao(), db.pollDao(), db.pollOptionDao(), db.pollVoteDao()
        )
        userRepo = UserRepository(db.userDao())
        ueRepo = UERepository(db.ueDao(), db.userUEDao())
        firestoreRepo = PostFireStoreRepository(FireStoreRepository())
    }

    fun setUserId(userId: Int) {
        _currentUserId.value = userId
    }

    val userBranch: StateFlow<String> = _currentUserId
        .map { id -> if (id == 0) "" else userRepo.getById(id)?.branch ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val userUECodes: StateFlow<List<String>> = _currentUserId
        .flatMapLatest { id ->
            if (id == 0) flowOf(emptyList())
            else ueRepo.getUserUEs(id).map { list -> list.map { it.ue.code } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<Post>> = combine(repo.getAll(), _filter, userBranch, userUECodes) { allPosts, currentFilter, branch, ues ->
        when (currentFilter) {
            is PostFilter.General -> allPosts.filter { it.ue == null }
            is PostFilter.ByBranch -> allPosts.filter { it.ue == null } // Adjust if branch logic is different
            is PostFilter.ByUE -> allPosts.filter { it.ue == currentFilter.code }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUniqueUEsFromPosts: StateFlow<List<String>> = repo.getAll()
        .map { posts -> posts.mapNotNull { it.ue }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(newFilter: PostFilter) {
        _filter.value = newFilter
    }

    fun createPost(text: String, imageUrl: String?, userId: Int = 1, ue: String? = null) {
        viewModelScope.launch {
            val post = Post(text = text, idUser = userId, imageUrl = imageUrl, ue = ue)
            val id = repo.insert(post)
            post.id = id.toInt()
            try {
                firestoreRepo.createPost(post)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createVoicePost(filePath: String, duration: Long, userId: Int) {
        viewModelScope.launch {
            val post = Post(text = "Message vocal", idUser = userId, voiceFilePath = filePath, voiceDuration = duration)
            val postId = repo.insert(post)
            post.id = postId.toInt()

            repo.insertVoiceMessage(VoiceMessage(postId = postId.toInt(), userId = userId, filePath = filePath, duration = duration))

            try {
                firestoreRepo.createPost(post)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPostWithPollOptions(question: String, userId: Int, options: List<String>) {
        viewModelScope.launch {
            val post = Post(text = question, idUser = userId, isPoll = true)
            val postId = repo.insert(post)
            post.id = postId.toInt()

            val poll = Poll(postId = postId.toInt(), creatorId = userId, question = question)
            val pollId = repo.insertPoll(poll)
            poll.id = pollId.toInt()

            options.forEach { repo.insertPollOption(PollOption(pollId = pollId.toInt(), text = it)) }

            try {
                firestoreRepo.createPost(post)
                firestoreRepo.createPoll(poll)
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
