package com.example.projet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.projet.ui.components.UtbmLogo
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.R
import com.example.projet.data.Poll
import com.example.projet.data.PollOption
import com.example.projet.data.Post
import com.example.projet.data.VoiceMessage
import com.example.projet.ui.utils.AudioRecorderManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    currentUserId: Int = 0,
    isAdmin: Boolean = false,
    postVm: PostViewModel = viewModel(),
    onCreatePostClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val utbmDarkColor = Color(0xFF001B3C)
    val addIconColor = Color(0xFF5992E4)

    LaunchedEffect(currentUserId) { postVm.setUserId(currentUserId) }

    val posts by postVm.posts.collectAsState()
    val selectedFilter by postVm.selectedFilter.collectAsState()
    val userUECodes by postVm.userUECodes.collectAsState()
    val userBranch by postVm.userBranch.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    UtbmLogo(iconSize = 32.dp)
                },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "Réglages", tint = utbmDarkColor)
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Filled.Person, contentDescription = "Profil", tint = utbmDarkColor)
                    }
                }
            )
        },
        floatingActionButton = {
            IconButton(onClick = onCreatePostClick, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Ajouter un post",
                    modifier = Modifier.size(56.dp),
                    tint = addIconColor
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (username.isNotBlank()) "Bonjour, $username 👋" else "Bienvenue! 👋",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Voici le fil d'actualités du jour",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chips : Général → Branche → UEs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter is PostFilter.General,
                    onClick = { postVm.setFilter(PostFilter.General) },
                    label = { Text("Général") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0055A4),
                        selectedLabelColor = Color.White
                    )
                )
                if (userBranch.isNotBlank()) {
                    FilterChip(
                        selected = selectedFilter is PostFilter.ByBranch,
                        onClick = { postVm.setFilter(PostFilter.ByBranch) },
                        label = { Text(userBranch) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF5E35B1),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                userUECodes.forEach { ueCode ->
                    FilterChip(
                        selected = selectedFilter is PostFilter.ByUE && (selectedFilter as PostFilter.ByUE).code == ueCode,
                        onClick = { postVm.setFilter(PostFilter.ByUE(ueCode)) },
                        label = { Text(ueCode) },
                        leadingIcon = {
                            Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF34A853),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (posts.isEmpty()) {
                Text(
                    text = when (val f = selectedFilter) {
                        is PostFilter.ByUE -> "Aucun post pour l'UE ${f.code}."
                        is PostFilter.ByBranch -> "Aucun post pour la branche $userBranch."
                        else -> "Aucun post pour l'instant.\nSoyez le premier à publier !"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                posts.forEach { post ->
                    PostBloc(
                        post = post,
                        vm = postVm,
                        currentUserId = currentUserId,
                        isAdmin = isAdmin,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PostBloc(
    post: Post,
    vm: PostViewModel,
    currentUserId: Int,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { AudioRecorderManager(context) }
    var isPostAudioPlaying by remember { mutableStateOf(false) }
    var playingVoiceId by remember { mutableStateOf<Int?>(null) }
    var playbackProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPostAudioPlaying, playingVoiceId) {
        if (isPostAudioPlaying || playingVoiceId != null) {
            while (isPostAudioPlaying || playingVoiceId != null) {
                val dur = audioManager.getDuration()
                val pos = audioManager.getCurrentPosition()
                playbackProgress = if (dur > 0) pos.toFloat() / dur else 0f
                kotlinx.coroutines.delay(100)
            }
        } else { playbackProgress = 0f }
    }

    DisposableEffect(Unit) { onDispose { audioManager.cleanup() } }

    var expanded by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }

    val commentsFlow = remember(post.id) { vm.getComments(post.id) }
    val comments by commentsFlow.collectAsState(initial = emptyList())
    val likeCount by remember(post.id) { vm.getLikeCount(post.id) }.collectAsState(initial = 0)
    val hasLiked by remember(post.id) { vm.hasUserLiked(post.id, currentUserId) }.collectAsState(initial = false)
    val pollsFlow = remember(post.id) { vm.getPolls(post.id) }
    val polls by pollsFlow.collectAsState(initial = emptyList())
    val voiceMessagesFlow = remember(post.id) { vm.getVoiceMessages(post.id) }
    val voiceMessages by voiceMessagesFlow.collectAsState(initial = emptyList())

    val dateStr = remember(post.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date(post.timestamp))
    }
    val postAuthorName by remember(post.idUser) { vm.getAuthorName(post.idUser) }.collectAsState(initial = "")
    val authorLabel = if (post.idUser == currentUserId) "Vous" else postAuthorName.ifBlank { "Utilisateur" }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF003061), Color(0xFF004689))
    )

    Surface(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = gradientBrush),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // UE badge
            if (post.ue != null) {
                Surface(
                    color = Color(0xFF34A853).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = Color(0xFF34A853), modifier = Modifier.size(12.dp))
                        Text(post.ue!!, color = Color(0xFF34A853), style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = authorLabel,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = post.text, color = Color.White.copy(alpha = 0.9f))

                    // Lecture audio inline pour posts vocaux
                    val voicePath = post.voiceFilePath
                    if (voicePath != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (isPostAudioPlaying) { audioManager.stopPlayback(); isPostAudioPlaying = false }
                                else { audioManager.playRecording(voicePath) { isPostAudioPlaying = false }; isPostAudioPlaying = true }
                            }) {
                                Icon(if (isPostAudioPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = null, tint = if (isPostAudioPlaying) Color(0xFF4CAF50) else Color.White, modifier = Modifier.size(28.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                if (isPostAudioPlaying) {
                                    LinearProgressIndicator(progress = { playbackProgress }, modifier = Modifier.fillMaxWidth(),
                                        color = Color(0xFF4CAF50), trackColor = Color.White.copy(alpha = 0.3f))
                                }
                                Text("${post.voiceDuration / 1000}s", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                            }
                        }
                    }

                    // Sondage inline
                    if (post.isPoll && polls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        polls.forEach { poll -> PollItem(poll = poll, vm = vm, currentUserId = currentUserId) }
                    }
                }
                ElevatedButton(
                    onClick = {
                        expanded = !expanded
                        if (!expanded) showComments = false
                    }
                ) {
                    Text(if (expanded) "Réduire" else "Réagir")
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.toggleLike(post.id, currentUserId) }) {
                                Icon(
                                    Icons.Filled.Favorite,
                                    contentDescription = "Like",
                                    tint = if (hasLiked) Color(0xFFE53935) else Color.White
                                )
                            }
                            if (likeCount > 0) {
                                Text(
                                    text = likeCount.toString(),
                                    color = Color(0xFFE53935),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        IconButton(onClick = { showComments = !showComments }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Commentaires", tint = Color.White)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Share, contentDescription = "Partager", tint = Color.White)
                        }
                        if (isAdmin || post.idUser == currentUserId) {
                            IconButton(onClick = { vm.deletePost(post) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF6B6B))
                            }
                        }
                    }

                    // Messages vocaux en réaction
                    if (voiceMessages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Vocaux", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        voiceMessages.forEach { vm2 ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                IconButton(onClick = {
                                    if (playingVoiceId == vm2.id) { audioManager.stopPlayback(); playingVoiceId = null }
                                    else { isPostAudioPlaying.let { if (it) { audioManager.stopPlayback(); isPostAudioPlaying = false } }; playingVoiceId?.let { audioManager.stopPlayback() }; audioManager.playRecording(vm2.filePath) { playingVoiceId = null }; playingVoiceId = vm2.id }
                                }) {
                                    Icon(if (playingVoiceId == vm2.id) Icons.Filled.Stop else Icons.Filled.PlayArrow, contentDescription = null,
                                        tint = if (playingVoiceId == vm2.id) Color(0xFF4CAF50) else Color.White, modifier = Modifier.size(20.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    if (playingVoiceId == vm2.id) {
                                        LinearProgressIndicator(progress = { playbackProgress }, modifier = Modifier.fillMaxWidth(),
                                            color = Color(0xFF4CAF50), trackColor = Color.White.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text("${vm2.duration / 1000}s", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showComments,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            if (comments.isEmpty()) {
                                Text(
                                    text = "Aucun commentaire. Soyez le premier !",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                comments.forEach { comment ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = comment.authorName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = comment.content,
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = {
                                        Text("Ajouter un commentaire...", color = Color.White.copy(alpha = 0.6f))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                        cursorColor = Color.White,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                IconButton(onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        vm.addComment(post.id, currentUserId, newCommentText)
                                        newCommentText = ""
                                    }
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PollItem(poll: Poll, vm: PostViewModel, currentUserId: Int) {
    val options by vm.getPollOptions(poll.id).collectAsState(initial = emptyList())
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.1f)).padding(12.dp)
    ) {
        Text(poll.question, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        options.forEach { option -> PollOptionRow(option = option, vm = vm, currentUserId = currentUserId) }
    }
}

@Composable
private fun PollOptionRow(option: PollOption, vm: PostViewModel, currentUserId: Int) {
    val votes by vm.getPollVotes(option.id).collectAsState(initial = emptyList())
    val userVoted = votes.any { it.userId == currentUserId }
    ElevatedButton(
        onClick = { if (!userVoted) vm.votePoll(option.id, currentUserId) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        enabled = !userVoted,
        colors = ButtonDefaults.elevatedButtonColors(
            disabledContainerColor = Color(0xFF4CAF50),
            disabledContentColor = Color.White
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (userVoted) { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)); Spacer(modifier = Modifier.padding(start = 6.dp)) }
            Text(option.text, modifier = Modifier.weight(1f))
            Text("${votes.size} votes", fontSize = 12.sp)
        }
    }
}
