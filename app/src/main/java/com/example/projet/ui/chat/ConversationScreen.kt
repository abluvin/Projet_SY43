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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    chatItemId: Int,
    onBack: () -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val item = chatItem ?: return
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    var newMessageText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    val utbmBlue = Color(0xFF0055A4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (item.hasOnlineStatus) {
                            Text("En ligne", fontSize = 12.sp, color = Color(0xFF34A853))
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
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessageText,
                        onValueChange = { newMessageText = it },
                        placeholder = { Text("Écrivez votre message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = utbmBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newMessageText.isNotBlank()) {
                                vm.sendMessage(chatItemId, newMessageText)
                                newMessageText = ""
                            }
                        },
                        enabled = newMessageText.isNotBlank(),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = utbmBlue
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val bubbleColor = if (message.isFromUser) Color(0xFF0055A4) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 15.sp
                )
                Text(
                    text = message.time,
                    color = if (message.isFromUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
