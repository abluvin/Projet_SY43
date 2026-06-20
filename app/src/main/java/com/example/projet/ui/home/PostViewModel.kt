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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class PostFilter {
    object General : PostFilter()
    object ByBranch : PostFilter()
    data class ByUE(val code: String) : PostFilter()
}

class PostViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: PostRepository
    private val firestoreRepo: PostFireStoreRepository
    private val ueRepo: UERepository
    private val userRepo: UserRepository

    init {
        val db = (application as ProjetApplication).database
        repo = PostRepository(
            db.postDao(), db.commentDao(),
            db.voiceMessageDao(), db.pollDao(), db.pollOptionDao(), db.pollVoteDao(),
            db.postLikeDao()
        )
        firestoreRepo = PostFireStoreRepository(FireStoreRepository())
        ueRepo = UERepository(db.ueDao(), db.userUEDao())
        userRepo = UserRepository(db.userDao())
    }

    private val _userId = MutableStateFlow(0)
    private val _selectedFilter = MutableStateFlow<PostFilter>(PostFilter.General)
    val selectedFilter: StateFlow<PostFilter> = _selectedFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val userUECodes: StateFlow<List<String>> = _userId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf(emptyList())
            else ueRepo.getUserUEs(uid).map { items -> items.map { it.ue.code } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userBranch: StateFlow<String> = _userId
        .flatMapLatest { uid ->
            if (uid == 0) flowOf("")
            else flow { emit(userRepo.getById(uid)?.branch ?: "") }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val allPosts: StateFlow<List<Post>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val posts: StateFlow<List<Post>> = combine(allPosts, _selectedFilter, userBranch) { postList, filter, branch ->
        when (filter) {
            is PostFilter.General -> postList
            is PostFilter.ByBranch -> postList.filter { it.branch == branch }
            is PostFilter.ByUE -> postList.filter { it.ue == filter.code }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserId(userId: Int) {
        if (_userId.value != userId) _userId.value = userId
    }

    fun setFilter(filter: PostFilter) {
        _selectedFilter.value = filter
    }

    fun createPost(text: String, imageUrl: String?, userId: Int = 1, ue: String? = null, branch: String? = null) {
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
            repo.insert(Post(text = text, idUser = userId, imageUrl = imageUrl, ue = ue, branch = branch))
        }
    }

    fun createVoicePost(filePath: String, duration: Long, userId: Int, ue: String? = null, branch: String? = null) {
        viewModelScope.launch {
            val post = Post(text = "Message vocal", idUser = userId, voiceFilePath = filePath, voiceDuration = duration)
            post.id = postId.toInt()

            val postId = repo.insert(Post(text = "Message vocal", idUser = userId, voiceFilePath = filePath, voiceDuration = duration, ue = ue, branch = branch))
            repo.insertVoiceMessage(VoiceMessage(postId = postId.toInt(), userId = userId, filePath = filePath, duration = duration))

            try {
                firestoreRepo.createPost(post)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPostWithPollOptions(question: String, userId: Int, options: List<String>, ue: String? = null, branch: String? = null) {
        viewModelScope.launch {
            val post = Post(text = question, idUser = userId, isPoll = true)
            post.id = postId.toInt()

            val poll = Poll(postId = postId.toInt(), creatorId = userId, question = question)
            poll.id = pollId.toInt()

            val postId = repo.insert(Post(text = question, idUser = userId, isPoll = true, ue = ue, branch = branch))
            val pollId = repo.insertPoll(Poll(postId = postId.toInt(), creatorId = userId, question = question))
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

    fun getComments(postId: Int): Flow<List<CommentWithAuthor>> = repo.getCommentsWithAuthors(postId)

    fun getAuthorName(userId: Int): Flow<String> = flow {
        emit(userRepo.getById(userId)?.name ?: "Utilisateur")
    }

    fun getLikeCount(postId: Int): Flow<Int> = repo.getLikeCount(postId)

    fun toggleLike(postId: Int, userId: Int) {
        viewModelScope.launch {
            val existing = repo.getUserLike(postId, userId)
            if (existing != null) repo.deleteLike(existing)
            else repo.insertLike(PostLike(postId = postId, userId = userId))
        }
    }

    fun hasUserLiked(postId: Int, userId: Int): Flow<Boolean> = repo.hasUserLiked(postId, userId)

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