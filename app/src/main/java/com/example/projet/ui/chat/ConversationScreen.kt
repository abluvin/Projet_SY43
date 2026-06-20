package com.example.projet.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.*
import kotlinx.coroutines.delay

private enum class InputMode { TEXT, RECORDING, AUDIO_PREVIEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    chatItemId: Int,
    currentUserId: Int = 0,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val item = chatItem ?: return
    val directChatName by vm.getDirectChatName(chatItemId).collectAsState(initial = null)
    val displayName = if (!item.isGroup && !item.isCourse) directChatName ?: item.name else item.name
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    val allUsers by vm.allUsers.collectAsState()
    val usersById = remember(allUsers) { allUsers.associateBy({ it.id }, { it.name }) }
    val listState = rememberLazyListState()

    var newMessageText by remember { mutableStateOf("") }
    var inputMode by remember { mutableStateOf(InputMode.TEXT) }
    var audioFilePath by remember { mutableStateOf("") }
    var audioDurationMs by remember { mutableStateOf(0L) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var showPollDialog by remember { mutableStateOf(false) }

    val mediaRecorder = remember {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            MediaRecorder(context)
        else
            MediaRecorder()
    }
    var recordStart by remember { mutableStateOf(0L) }

    DisposableEffect(Unit) {
        onDispose {
            try { mediaRecorder.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(inputMode) {
        if (inputMode == InputMode.RECORDING) {
            recordingSeconds = 0
            while (inputMode == InputMode.RECORDING) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val audioPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            audioFilePath = "${context.filesDir}/audio_${System.currentTimeMillis()}.3gp"
            recordStart = System.currentTimeMillis()
            try {
                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                inputMode = InputMode.RECORDING
            } catch (_: Exception) {}
        }
    }

    fun startRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            audioFilePath = "${context.filesDir}/audio_${System.currentTimeMillis()}.3gp"
            recordStart = System.currentTimeMillis()
            try {
                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(audioFilePath)
                    prepare()
                    start()
                }
                inputMode = InputMode.RECORDING
            } catch (_: Exception) {}
        } else {
            audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
        } catch (_: Exception) {}
        audioDurationMs = System.currentTimeMillis() - recordStart
        inputMode = InputMode.AUDIO_PREVIEW
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val utbmBlue = Color(0xFF0055A4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (item.hasOnlineStatus) {
                            Text("En ligne", fontSize = 12.sp, color = Color(0xFF34A853))
                        } else if (item.isGroup) {
                            Text("Groupe", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isFromUser = message.senderId != 0 && message.senderId == currentUserId
                    val senderName = if (item.isGroup && !isFromUser) usersById[message.senderId] else null
                    Box(modifier = Modifier.animateItem()) {
                        when (message.messageType) {
                            MessageType.VOICE_MESSAGE -> AudioMessageBubble(message, currentUserId, senderName)
                            MessageType.POLL -> {
                                if (message.pollId != null) {
                                    PollBubble(
                                        message = message,
                                        pollId = message.pollId,
                                        currentUserId = currentUserId,
                                        vm = vm,
                                        senderName = senderName
                                    )
                                } else MessageBubble(message, currentUserId, senderName)
                            }
                            else -> MessageBubble(message, currentUserId, senderName)
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                when (inputMode) {
                    InputMode.TEXT -> TextInputBar(
                        text = newMessageText,
                        onTextChange = { newMessageText = it },
                        isGroup = item.isGroup,
                        onSend = {
                            if (newMessageText.isNotBlank()) {
                                vm.sendMessage(chatItemId, newMessageText)
                                newMessageText = ""
                            }
                        },
                        onMic = { startRecording() },
                        onPoll = { showPollDialog = true }
                    )
                    InputMode.RECORDING -> RecordingBar(
                        seconds = recordingSeconds,
                        onStop = { stopRecording() },
                        onCancel = {
                            try { mediaRecorder.stop(); mediaRecorder.reset() } catch (_: Exception) {}
                            inputMode = InputMode.TEXT
                        }
                    )
                    InputMode.AUDIO_PREVIEW -> AudioPreviewBar(
                        durationMs = audioDurationMs,
                        onSend = {
                            vm.sendAudioMessage(chatItemId, audioFilePath, audioDurationMs)
                            inputMode = InputMode.TEXT
                        },
                        onCancel = { inputMode = InputMode.TEXT }
                    )
                }
            }
        }
    }

    if (showPollDialog) {
        PollCreationDialog(
            onDismiss = { showPollDialog = false },
            onCreate = { question, options ->
                vm.createPoll(chatItemId, question, options)
                showPollDialog = false
            }
        )
    }
}

@Composable
private fun TextInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    isGroup: Boolean,
    onSend: () -> Unit,
    onMic: () -> Unit,
    onPoll: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isGroup) {
            IconButton(onClick = onPoll) {
                Icon(Icons.Default.Poll, contentDescription = "Sondage", tint = Color.Gray)
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Écrivez votre message…") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = utbmBlue
            )
        )
        Spacer(Modifier.width(4.dp))
        AnimatedContent(
            targetState = text.isBlank(),
            transitionSpec = {
                (scaleIn(tween(160), initialScale = 0.72f) + fadeIn(tween(160))) togetherWith
                (scaleOut(tween(120), targetScale = 0.72f) + fadeOut(tween(120)))
            },
            label = "send_mic_toggle"
        ) { isBlank ->
            if (isBlank) {
                IconButton(onClick = onMic) {
                    Icon(Icons.Default.Mic, contentDescription = "Enregistrer", tint = Color.Gray)
                }
            } else {
                IconButton(
                    onClick = onSend,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = utbmBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                }
            }
        }
    }
}

@Composable
private fun RecordingBar(seconds: Int, onStop: () -> Unit, onCancel: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "pulse_alpha"
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Enregistrement… ${"%02d:%02d".format(seconds / 60, seconds % 60)}",
            modifier = Modifier.weight(1f),
            color = Color.Red,
            fontWeight = FontWeight.Medium
        )
        TextButton(onClick = onCancel) { Text("Annuler", color = Color.Gray) }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onStop,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Icon(Icons.Default.Stop, contentDescription = "Arrêter", tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("Arrêter", color = Color.White)
        }
    }
}

@Composable
private fun AudioPreviewBar(durationMs: Long, onSend: () -> Unit, onCancel: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    val seconds = (durationMs / 1000).toInt()
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Mic, contentDescription = null, tint = utbmBlue)
        Spacer(Modifier.width(8.dp))
        Text(
            "Message vocal (${"%02d:%02d".format(seconds / 60, seconds % 60)})",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onCancel) { Text("Annuler", color = Color.Gray) }
        Spacer(Modifier.width(4.dp))
        Button(
            onClick = onSend,
            colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(4.dp))
            Text("Envoyer", color = Color.White)
        }
    }
}

@Composable
fun MessageBubble(message: Message, currentUserId: Int = 0, senderName: String? = null) {
    val isFromUser = message.senderId != 0 && message.senderId == currentUserId
    val bubbleColor = if (isFromUser) Color(0xFF0055A4) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isFromUser) Alignment.End else Alignment.Start
    val shape = if (isFromUser) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (senderName != null) {
            Text(
                text = senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0055A4),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(text = message.text, color = textColor, fontSize = 15.sp)
                Text(
                    text = message.time,
                    color = if (isFromUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun AudioMessageBubble(message: Message, currentUserId: Int = 0, senderName: String? = null) {
    val context = LocalContext.current
    val utbmBlue = Color(0xFF0055A4)
    val isFromUser = message.senderId != 0 && message.senderId == currentUserId
    val alignment = if (isFromUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isFromUser) utbmBlue else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isFromUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isFromUser) RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
                else RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)

    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose { try { mediaPlayer.release() } catch (_: Exception) {} }
    }

    val durationSecs = (message.audioDuration / 1000).toInt()
    val durationLabel = "%02d:%02d".format(durationSecs / 60, durationSecs % 60)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (senderName != null) {
            Text(
                text = senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0055A4),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (!isPlaying && message.audioPath != null) {
                            try {
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(message.audioPath)
                                mediaPlayer.prepare()
                                mediaPlayer.start()
                                isPlaying = true
                                mediaPlayer.setOnCompletionListener { isPlaying = false }
                            } catch (_: Exception) { isPlaying = false }
                        } else {
                            mediaPlayer.pause()
                            isPlaying = false
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lire",
                        tint = contentColor
                    )
                }
                Spacer(Modifier.width(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(80.dp)
                ) {
                    val heights = listOf(8, 14, 6, 18, 10, 16, 8, 12, 6, 14, 10, 8, 16, 12, 6)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(contentColor.copy(alpha = 0.7f))
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(durationLabel, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(message.time, color = contentColor.copy(alpha = 0.7f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun PollBubble(
    message: Message,
    pollId: Int,
    currentUserId: Int,
    vm: ChatViewModel,
    senderName: String? = null
) {
    val utbmBlue = Color(0xFF0055A4)
    val options by vm.getPollOptions(pollId).collectAsState(initial = emptyList())
    val userVotes by vm.getUserVotesForPoll(currentUserId, pollId).collectAsState(initial = emptyList())
    val votedOptionIds = userVotes.map { it.optionId }.toSet()
    val totalVotes = options.sumOf { it.voteCount }
    val isFromUser = message.senderId != 0 && message.senderId == currentUserId
    val alignment = if (isFromUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (senderName != null) {
            Text(
                text = senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0055A4),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Poll,
                        contentDescription = null,
                        tint = utbmBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "SONDAGE",
                        color = utbmBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.weight(1f))
                    Text(message.time, fontSize = 10.sp, color = Color.Gray)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    message.text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                options.forEach { option ->
                    val isVoted = option.id in votedOptionIds
                    val pct = if (totalVotes > 0) option.voteCount.toFloat() / totalVotes else 0f
                    PollOptionRow(
                        text = option.text,
                        voteCount = option.voteCount,
                        percent = pct,
                        isVoted = isVoted,
                        onClick = { vm.voteForOption(option.id) }
                    )
                    Spacer(Modifier.height(6.dp))
                }
                Text(
                    "$totalVotes vote${if (totalVotes != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun PollOptionRow(
    text: String,
    voteCount: Int,
    percent: Float,
    isVoted: Boolean,
    onClick: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isVoted) utbmBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = if (isVoted)
            androidx.compose.foundation.BorderStroke(1.5.dp, utbmBlue)
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .height(40.dp)
                    .background(utbmBlue.copy(alpha = 0.06f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = if (isVoted) FontWeight.Bold else FontWeight.Normal,
                    color = if (isVoted) utbmBlue else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${(percent * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = if (isVoted) utbmBlue else Color.Gray,
                    fontWeight = if (isVoted) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PollCreationDialog(
    onDismiss: () -> Unit,
    onCreate: (question: String, options: List<String>) -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Poll, contentDescription = null, tint = utbmBlue) },
        title = { Text("Créer un sondage") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Question") },
                    placeholder = { Text("Votre question…") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = utbmBlue)
                )
                Spacer(Modifier.height(12.dp))
                Text("Options", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                options.forEachIndexed { index, opt ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = opt,
                            onValueChange = { options[index] = it },
                            label = { Text("Option ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = utbmBlue)
                        )
                        if (options.size > 2) {
                            IconButton(onClick = { options.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Supprimer", tint = Color.Gray)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                if (options.size < 4) {
                    TextButton(onClick = { options.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Ajouter une option")
                    }
                }
            }
        },
        confirmButton = {
            val validOptions = options.filter { it.isNotBlank() }
            TextButton(
                onClick = { if (question.isNotBlank() && validOptions.size >= 2) onCreate(question, validOptions) },
                enabled = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
            ) {
                Text("Créer", color = utbmBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
