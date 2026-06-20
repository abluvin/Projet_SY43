package com.example.projet.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.Message
import com.example.projet.data.MessageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    chatItemId: Int,
    isProf: Boolean = false,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val profColor = Color(0xFF34A853)
    val announcementColor = Color(0xFFE65100)

    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    var isAnnouncementMode by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(chatItem?.name ?: "Hub de cours", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                }
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
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text("Aucune annonce pour l'instant.", color = Color.Gray)
                        }
                    }
                } else {
                    items(messages) { message ->
                        when (message.type) {
                            MessageType.POLL -> PollCard(message = message, onVote = { vm.votePoll(message.id, it) })
                            else -> {
                                if (message.isAnnouncement) AnnouncementCard(message = message)
                                else HubMessageCard(message = message)
                            }
                        }
                    }
                }
            }

            Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                if (isProf) {
                    Column(modifier = Modifier.padding(12.dp).navigationBarsPadding().imePadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !isAnnouncementMode,
                                onClick = { isAnnouncementMode = false },
                                label = { Text("Message", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, null, Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = profColor, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White)
                            )
                            FilterChip(
                                selected = isAnnouncementMode,
                                onClick = { isAnnouncementMode = true },
                                label = { Text("Annonce", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Campaign, null, Modifier.size(16.dp)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = announcementColor, selectedLabelColor = Color.White, selectedLeadingIconColor = Color.White)
                            )
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { showPollDialog = true }) {
                                Icon(Icons.Default.Poll, contentDescription = "Créer un sondage", tint = profColor)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = { Text(if (isAnnouncementMode) "Écrire une annonce..." else "Envoyer un message...") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isAnnouncementMode) announcementColor else profColor)
                            )
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        if (isAnnouncementMode) vm.sendAnnouncement(chatItemId, inputText.trim())
                                        else vm.sendMessage(chatItemId, inputText.trim())
                                        inputText = ""
                                    }
                                },
                                enabled = inputText.isNotBlank()
                            ) {
                                Icon(if (isAnnouncementMode) Icons.Default.Campaign else Icons.AutoMirrored.Filled.Send, null, tint = if (inputText.isBlank()) Color.Gray else if (isAnnouncementMode) announcementColor else profColor)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding().fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Mode lecture seule activé", color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showPollDialog) {
        CreatePollDialog(
            onDismiss = { showPollDialog = false },
            onConfirm = { question, options ->
                vm.sendPoll(chatItemId, question, options)
                showPollDialog = false
            }
        )
    }
}

@Composable
fun CreatePollDialog(onDismiss: () -> Unit, onConfirm: (String, List<String>) -> Unit) {
    var question by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf("", "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau sondage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Question") }, modifier = Modifier.fillMaxWidth())
                options.forEachIndexed { index, option ->
                    OutlinedTextField(
                        value = option,
                        onValueChange = { newText -> options = options.toMutableList().also { it[index] = newText } },
                        label = { Text("Option ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (options.size < 4) {
                    TextButton(onClick = { options = options + "" }) {
                        Icon(Icons.Default.Add, null)
                        Text("Ajouter une option")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(question, options.filter { it.isNotBlank() }) }, enabled = question.isNotBlank() && options.count { it.isNotBlank() } >= 2) {
                Text("Créer")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun PollCard(message: Message, onVote: (Int) -> Unit) {
    val totalVotes = message.pollVotes?.values?.sum() ?: 0
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(message.pollQuestion ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            message.pollOptions?.forEachIndexed { index, option ->
                val votes = message.pollVotes?.get(index) ?: 0
                val progress = if (totalVotes > 0) votes.toFloat() / totalVotes else 0f
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Button(
                        onClick = { onVote(index) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(option)
                            Text("$votes votes")
                        }
                    }
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(message: Message) {
    val announcementColor = Color(0xFFE65100)
    Surface(color = announcementColor.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Campaign, null, tint = announcementColor, modifier = Modifier.size(16.dp))
                Text("ANNONCE", color = announcementColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
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
fun HubMessageCard(message: Message) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp), shadowElevation = 1.dp, modifier = Modifier.widthIn(max = 300.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = message.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.End))
            }
        }
    }
}
