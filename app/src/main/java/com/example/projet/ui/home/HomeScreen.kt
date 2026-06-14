package com.example.projet.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
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
import com.example.projet.data.Post
import com.example.projet.data.Poll
import com.example.projet.data.VoiceMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    currentUserId: Int = 0,
    postVm: PostViewModel = viewModel(),
    onCreatePostClick: () -> Unit = {}
) {
    val utbmDarkColor = Color(0xFF001B3C)
    val addIconColor = Color(0xFF5992E4)

    val posts by postVm.posts.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    UtbmLogo(iconSize = 32.dp)
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = utbmDarkColor)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = utbmDarkColor)
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

            Spacer(modifier = Modifier.height(32.dp))

            if (posts.isEmpty()) {
                Text(
                    text = "Aucun post pour l'instant.\nSoyez le premier à publier !",
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
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var showVoiceMessages by remember { mutableStateOf(false) }
    var showPolls by remember { mutableStateOf(false) }
    var showCreatePoll by remember { mutableStateOf(false) }
    var showRecordAudio by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var newCommentText by remember { mutableStateOf("") }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }

    val commentsFlow = remember(post.id) { vm.getComments(post.id) }
    val comments by commentsFlow.collectAsState(initial = emptyList())

    val voiceMessagesFlow = remember(post.id) { vm.getVoiceMessages(post.id) }
    val voiceMessages by voiceMessagesFlow.collectAsState(initial = emptyList())

    val pollsFlow = remember(post.id) { vm.getPolls(post.id) }
    val polls by pollsFlow.collectAsState(initial = emptyList())

    val dateStr = remember(post.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH).format(Date(post.timestamp))
    }
    val authorLabel = if (post.idUser == currentUserId) "Vous" else "Étudiant"

    // Timer pour l'enregistrement audio
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                recordingTime += 1000
            }
        }
    }

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
                    Text(
                        text = post.text,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                ElevatedButton(
                    onClick = {
                        expanded = !expanded
                        if (!expanded) {
                            showComments = false
                            showVoiceMessages = false
                            showPolls = false
                            showCreatePoll = false
                        }
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
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Favorite, contentDescription = "Like", tint = Color.White)
                        }
                        IconButton(onClick = { showComments = !showComments; showVoiceMessages = false; showPolls = false; showCreatePoll = false }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Commentaires", tint = Color.White)
                        }
                        IconButton(onClick = { showVoiceMessages = !showVoiceMessages; showComments = false; showPolls = false; showCreatePoll = false }) {
                            Icon(Icons.Filled.Mic, contentDescription = "Messages vocaux", tint = Color.White)
                        }
                        IconButton(onClick = { showPolls = !showPolls; showComments = false; showVoiceMessages = false; if (showPolls) showCreatePoll = false }) {
                            Icon(Icons.Filled.HowToVote, contentDescription = "Sondages", tint = Color.White)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Share, contentDescription = "Partager", tint = Color.White)
                        }
                    }

                    // Commentaires
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
                                    Text(
                                        text = "• ${comment.content}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
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

                    // Messages vocaux
                    AnimatedVisibility(
                        visible = showVoiceMessages,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Messages vocaux",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (voiceMessages.isEmpty()) {
                                Text(
                                    text = "Aucun message vocal. Enregistrez le premier !",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                voiceMessages.forEach { voiceMsg ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Mic, contentDescription = "Audio", tint = Color.White, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.padding(start = 8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Durée: ${voiceMsg.duration / 1000}s",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date(voiceMsg.timestamp)),
                                                color = Color.White.copy(alpha = 0.6f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ElevatedButton(
                                    onClick = { showRecordAudio = !showRecordAudio; isRecording = false; recordingTime = 0L },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Mic, contentDescription = "Enregistrer", modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.padding(start = 4.dp))
                                    Text(if (showRecordAudio) "Annuler" else "Enregistrer")
                                }
                            }

                            AnimatedVisibility(
                                visible = showRecordAudio,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (!isRecording) {
                                        Text(
                                            text = "Appuyez pour enregistrer",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )
                                        ElevatedButton(
                                            onClick = { isRecording = true; recordingTime = 0L },
                                            modifier = Modifier.size(80.dp),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Filled.Mic, contentDescription = "Démarrer", modifier = Modifier.size(40.dp))
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                Icons.Filled.Mic,
                                                contentDescription = "Enregistrement",
                                                tint = Color.Red,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.padding(start = 8.dp))
                                            Text(
                                                text = "Durée: ${recordingTime / 1000}s",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            ElevatedButton(
                                                onClick = { isRecording = false; recordingTime = 0L },
                                                modifier = Modifier.weight(1f),
                                                colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                                                    containerColor = Color.Red.copy(alpha = 0.8f)
                                                )
                                            ) {
                                                Icon(Icons.Filled.Stop, contentDescription = "Arrêter", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                                Text("Arrêter")
                                            }
                                            ElevatedButton(
                                                onClick = {
                                                    // Simuler l'ajout d'un message vocal
                                                    vm.addVoiceMessage(post.id, currentUserId, "recording_${post.id}.3gp", recordingTime * 1000)
                                                    isRecording = false
                                                    recordingTime = 0L
                                                    showRecordAudio = false
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Filled.CheckCircle, contentDescription = "Valider", modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                                Text("Valider")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Sondages
                    AnimatedVisibility(
                        visible = showPolls,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Sondages",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            if (polls.isEmpty()) {
                                Text(
                                    text = "Aucun sondage. Créez le premier !",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                polls.forEach { poll ->
                                    PollItem(poll = poll, vm = vm, currentUserId = currentUserId)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            ElevatedButton(onClick = { showCreatePoll = !showCreatePoll }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.HowToVote, contentDescription = "Créer", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.padding(start = 4.dp))
                                Text("Créer un sondage")
                            }

                            AnimatedVisibility(
                                visible = showCreatePoll,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    OutlinedTextField(
                                        value = pollQuestion,
                                        onValueChange = { pollQuestion = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Question du sondage...", color = Color.White.copy(alpha = 0.6f)) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.White,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                            cursorColor = Color.White,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    pollOptions.forEachIndexed { index, option ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedTextField(
                                                value = option,
                                                onValueChange = { newValue ->
                                                    pollOptions = pollOptions.toMutableList().also { it[index] = newValue }
                                                },
                                                modifier = Modifier.weight(1f),
                                                placeholder = { Text("Option ${index + 1}...", color = Color.White.copy(alpha = 0.6f)) },
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color.White,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                                    cursorColor = Color.White,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                textStyle = MaterialTheme.typography.bodySmall
                                            )
                                            if (pollOptions.size > 2) {
                                                IconButton(onClick = { pollOptions = pollOptions.filterIndexed { i, _ -> i != index } }) {
                                                    Text("✕", color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ElevatedButton(onClick = { pollOptions = pollOptions + "" }, modifier = Modifier.weight(1f)) {
                                            Text("+ Option")
                                        }
                                        ElevatedButton(
                                            onClick = {
                                                if (pollQuestion.isNotBlank() && pollOptions.any { it.isNotBlank() }) {
                                                    vm.createPoll(post.id, currentUserId, pollQuestion, pollOptions.filter { it.isNotBlank() })
                                                    pollQuestion = ""
                                                    pollOptions = listOf("", "")
                                                    showCreatePoll = false
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Créer")
                                        }
                                    }
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
    val optionsFlow = remember(poll.id) { vm.getPollOptions(poll.id) }
    val options by optionsFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp)
    ) {
        Text(
            text = poll.question,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        options.forEach { option ->
            val votesFlow = remember(option.id) { vm.getPollVotes(option.id) }
            val votes by votesFlow.collectAsState(initial = emptyList())
            val userHasVoted = remember(votes, currentUserId) { votes.any { it.userId == currentUserId } }

            ElevatedButton(
                onClick = { if (!userHasVoted) vm.votePoll(option.id, currentUserId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                enabled = !userHasVoted
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (userHasVoted) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Voté", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.padding(start = 8.dp))
                    }
                    Text(text = option.text, modifier = Modifier.weight(1f))
                    Text(text = "${votes.size} votes", fontSize = 12.sp)
                }
            }
        }
    }
}
