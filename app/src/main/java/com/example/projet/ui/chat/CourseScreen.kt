package com.example.projet.ui.chat

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.projet.data.Message
import com.example.projet.data.MessageType
import kotlinx.coroutines.delay

private enum class CourseInputMode { TEXT, RECORDING, AUDIO_PREVIEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    chatItemId: Int,
    isProf: Boolean = false,
    currentUserId: Int = 0,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val utbmBlue = Color(0xFF0055A4)
    val profColor = Color(0xFF34A853)
    val announcementColor = Color(0xFFE65100)

    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    val allUsers by vm.allUsers.collectAsState()
    val usersById = remember(allUsers) { allUsers.associateBy({ it.id }, { it.name }) }
    var inputText by remember { mutableStateOf("") }
    var isAnnouncementMode by remember { mutableStateOf(false) }
    var inputMode by remember { mutableStateOf(CourseInputMode.TEXT) }
    var audioFilePath by remember { mutableStateOf("") }
    var audioDurationMs by remember { mutableStateOf(0L) }
    var recordingSeconds by remember { mutableStateOf(0) }
    var recordStart by remember { mutableStateOf(0L) }
    var showPollDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val mediaRecorder = remember {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
    }

    DisposableEffect(Unit) {
        onDispose { try { mediaRecorder.release() } catch (_: Exception) {} }
    }

    LaunchedEffect(inputMode) {
        if (inputMode == CourseInputMode.RECORDING) {
            recordingSeconds = 0
            while (inputMode == CourseInputMode.RECORDING) {
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
                inputMode = CourseInputMode.RECORDING
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
                inputMode = CourseInputMode.RECORDING
            } catch (_: Exception) {}
        } else {
            audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        try { mediaRecorder.stop(); mediaRecorder.reset() } catch (_: Exception) {}
        audioDurationMs = System.currentTimeMillis() - recordStart
        inputMode = CourseInputMode.AUDIO_PREVIEW
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            chatItem?.name ?: "Hub de cours",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isProf) "Vous êtes professeur de ce hub" else "Lecture seule",
                            fontSize = 12.sp,
                            color = if (isProf) profColor else Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Seul le professeur peut envoyer des messages",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucune annonce pour l'instant.", color = Color.Gray)
                        }
                    }
                } else {
                    items(messages) { message ->
                        val isFromUser = message.senderId != 0 && message.senderId == currentUserId
                        val senderName = if (!isFromUser) usersById[message.senderId] else null
                        when {
                            message.isAnnouncement -> AnnouncementCard(message = message, senderName = senderName)
                            message.messageType == MessageType.VOICE_MESSAGE -> AudioMessageBubble(message, currentUserId, senderName)
                            message.messageType == MessageType.POLL && message.pollId != null ->
                                PollBubble(
                                    message = message,
                                    pollId = message.pollId,
                                    currentUserId = currentUserId,
                                    vm = vm,
                                    senderName = senderName
                                )
                            else -> HubMessageCard(message = message, senderName = senderName)
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                if (!isProf) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Mode lecture seule activé pour ce canal", color = Color.Gray)
                    }
                } else {
                    when (inputMode) {
                        CourseInputMode.TEXT -> Column(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .navigationBarsPadding()
                                .imePadding()
                        ) {
                            // Sélecteur de mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !isAnnouncementMode,
                                    onClick = { isAnnouncementMode = false },
                                    label = { Text("Message", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = profColor,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                                FilterChip(
                                    selected = isAnnouncementMode,
                                    onClick = { isAnnouncementMode = true },
                                    label = { Text("Annonce globale", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(14.dp))
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = announcementColor,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { showPollDialog = true }) {
                                    Icon(Icons.Default.Poll, contentDescription = "Sondage", tint = Color.Gray)
                                }
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = {
                                        Text(
                                            if (isAnnouncementMode) "Écrire une annonce visible par tous…"
                                            else "Envoyer un message…"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(24.dp),
                                    maxLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isAnnouncementMode) announcementColor else profColor
                                    )
                                )
                                if (inputText.isBlank()) {
                                    IconButton(onClick = { startRecording() }) {
                                        Icon(Icons.Default.Mic, contentDescription = "Enregistrer", tint = Color.Gray)
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (inputText.isNotBlank()) {
                                                if (isAnnouncementMode) vm.sendAnnouncement(chatItemId, inputText.trim())
                                                else vm.sendMessage(chatItemId, inputText.trim())
                                                inputText = ""
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (isAnnouncementMode) Icons.Default.Campaign else Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Envoyer",
                                            tint = when {
                                                inputText.isBlank() -> Color.Gray
                                                isAnnouncementMode -> announcementColor
                                                else -> profColor
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        CourseInputMode.RECORDING -> {
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
                                    modifier = Modifier.size(12.dp).clip(CircleShape)
                                        .background(Color.Red.copy(alpha = alpha))
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Enregistrement… ${"%02d:%02d".format(recordingSeconds / 60, recordingSeconds % 60)}",
                                    modifier = Modifier.weight(1f),
                                    color = Color.Red,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(onClick = {
                                    try { mediaRecorder.stop(); mediaRecorder.reset() } catch (_: Exception) {}
                                    inputMode = CourseInputMode.TEXT
                                }) { Text("Annuler", color = Color.Gray) }
                                Spacer(Modifier.width(4.dp))
                                Button(
                                    onClick = { stopRecording() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Arrêter", color = Color.White)
                                }
                            }
                        }
                        CourseInputMode.AUDIO_PREVIEW -> {
                            val seconds = (audioDurationMs / 1000).toInt()
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .navigationBarsPadding()
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = profColor)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Message vocal (${"%02d:%02d".format(seconds / 60, seconds % 60)})",
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { inputMode = CourseInputMode.TEXT }) {
                                    Text("Annuler", color = Color.Gray)
                                }
                                Spacer(Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        vm.sendAudioMessage(chatItemId, audioFilePath, audioDurationMs)
                                        inputMode = CourseInputMode.TEXT
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = profColor)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Envoyer", color = Color.White)
                                }
                            }
                        }
                    }
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
fun AnnouncementCard(message: Message, senderName: String? = null) {
    val announcementColor = Color(0xFFE65100)
    Surface(
        color = announcementColor.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, tint = announcementColor, modifier = Modifier.size(16.dp))
                Text("ANNONCE", color = announcementColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                if (senderName != null) {
                    Text("· $senderName", fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                Text(message.time, fontSize = 10.sp, color = Color.Gray)
            }
            HorizontalDivider(color = announcementColor.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            Text(text = message.text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HubMessageCard(message: Message, senderName: String? = null) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (senderName != null) {
            Text(
                text = senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF0055A4),
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
                shadowElevation = 1.dp,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = message.text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = message.time,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
