package com.example.projet.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    chatItemId: Int,
    isProf: Boolean = false,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val utbmBlue = Color(0xFF0055A4)
    val profColor = Color(0xFF34A853)
    val announcementColor = Color(0xFFE65100)

    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
    var isAnnouncementMode by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
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
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Seul le professeur peut envoyer des messages",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucune annonce pour l'instant.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        if (message.isAnnouncement) {
                            AnnouncementCard(message = message)
                        } else {
                            HubMessageCard(message = message)
                        }
                    }
                }
            }

            // Bottom bar
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                if (isProf) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .imePadding()
                    ) {
                        // Mode toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !isAnnouncementMode,
                                onClick = { isAnnouncementMode = false },
                                label = { Text("Message", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = inputText,
                                onValueChange = { inputText = it },
                                placeholder = {
                                    Text(
                                        if (isAnnouncementMode) "Écrire une annonce visible par tous..."
                                        else "Envoyer un message..."
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (isAnnouncementMode) announcementColor else profColor
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (inputText.isNotBlank()) {
                                        if (isAnnouncementMode) {
                                            vm.sendAnnouncement(chatItemId, inputText.trim())
                                        } else {
                                            vm.sendMessage(chatItemId, inputText.trim())
                                        }
                                        inputText = ""
                                    }
                                },
                                enabled = inputText.isNotBlank()
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
                } else {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Mode lecture seule activé pour ce canal",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementCard(message: Message) {
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
                Icon(
                    Icons.Default.Campaign,
                    contentDescription = null,
                    tint = announcementColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "ANNONCE",
                    color = announcementColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.weight(1f))
                Text(message.time, fontSize = 10.sp, color = Color.Gray)
            }
            HorizontalDivider(color = announcementColor.copy(alpha = 0.2f))
            Spacer(Modifier.height(8.dp))
            Text(
                text = message.text,
                fontSize = 15.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HubMessageCard(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, fontSize = 15.sp)
                Text(
                    text = message.time,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
