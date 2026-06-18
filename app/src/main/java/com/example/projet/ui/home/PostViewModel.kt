package com.example.projet.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projet.ProjetApplication
import com.example.projet.data.*
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
        repo = PostRepository(
            db.postDao(),
            db.commentDao(),
            db.voiceMessageDao(),
            db.pollDao(),
            db.pollOptionDao(),
            db.pollVoteDao()
        )
    }

    val posts: StateFlow<List<Post>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPost(text: String, imageUrl: String?, userId: Int = 1, voiceFilePath: String? = null, voiceDuration: Long = 0L, isPoll: Boolean = false) {
        viewModelScope.launch {
            val post = Post(
                text = text,
                idUser = userId,
                imageUrl = imageUrl,
                voiceFilePath = voiceFilePath,
                voiceDuration = voiceDuration,
                isPoll = isPoll
            )
            val postId = repo.insert(post)
            
            // Si c'est un sondage, créer les options
            if (isPoll) {
                // Les options du sondage seront créées via l'UI
            }
        }
    }

    fun createPostWithPollOptions(text: String, imageUrl: String?, userId: Int = 1, options: List<String>) {
        viewModelScope.launch {
            val post = Post(
                text = text,
                idUser = userId,
                imageUrl = imageUrl,
                isPoll = true
            )
            val postId = repo.insert(post)

            val pollId = repo.insertPoll(Poll(
                postId = postId.toInt(),
                creatorId = userId,
                question = text,
                timestamp = System.currentTimeMillis()
            ))

            options.forEach { optionText ->
                repo.insertPollOption(PollOption(
                    pollId = pollId.toInt(),
                    text = optionText
                ))
            }
        }
    }

    // Commentaires
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

    // Messages vocaux
    fun getVoiceMessages(postId: Int): Flow<List<VoiceMessage>> = repo.getVoiceMessages(postId)

    fun addVoiceMessage(postId: Int, userId: Int = 1, filePath: String, duration: Long) {
        viewModelScope.launch {
            repo.insertVoiceMessage(VoiceMessage(
                postId = postId,
                userId = userId,
                filePath = filePath,
                duration = duration,
                timestamp = System.currentTimeMillis()
            ))
        }
    }

    fun deleteVoiceMessage(voiceMessage: VoiceMessage) {
        viewModelScope.launch {
            repo.deleteVoiceMessage(voiceMessage)
        }
    }

    // Sondages
    fun getPolls(postId: Int): Flow<List<Poll>> = repo.getPolls(postId)

    fun createPoll(postId: Int, creatorId: Int = 1, question: String, options: List<String>) {
        viewModelScope.launch {
            val pollId = repo.insertPoll(Poll(
                postId = postId,
                creatorId = creatorId,
                question = question,
                timestamp = System.currentTimeMillis()
            ))
            options.forEach { optionText ->
                repo.insertPollOption(PollOption(
                    pollId = pollId.toInt(),
                    text = optionText
                ))
            }
        }
    }

    fun deletePoll(poll: Poll) {
        viewModelScope.launch {
            repo.deletePoll(poll)
        }
    }

    // Options de sondage
    fun getPollOptions(pollId: Int): Flow<List<PollOption>> = repo.getPollOptions(pollId)

    // Votes de sondage
    fun getPollVotes(optionId: Int): Flow<List<PollVote>> = repo.getPollVotes(optionId)

    fun votePoll(optionId: Int, userId: Int = 1) {
        viewModelScope.launch {
            val existingVote = repo.getUserVote(optionId, userId)
            if (existingVote == null) {
                repo.insertPollVote(PollVote(
                    optionId = optionId,
                    userId = userId,
                    timestamp = System.currentTimeMillis()
                ))
                // Incrémenter le nombre de votes de l'option
                val option = repo.getPollOptions(0).collect { options ->
                    options.find { it.id == optionId }?.let {
                        it.voteCount += 1
                        repo.updatePollOption(it)
                    }
                }
            }
        }
    }
}
