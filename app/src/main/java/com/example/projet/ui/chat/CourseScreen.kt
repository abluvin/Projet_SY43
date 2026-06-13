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

    val chatItem by vm.getChatItem(chatItemId).collectAsState(initial = null)
    val messages by vm.getMessages(chatItemId).collectAsState(initial = emptyList())
    var inputText by remember { mutableStateOf("") }
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
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "Aucune annonce pour l'instant.",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        HubMessageCard(message = message)
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
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .imePadding()
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Envoyer une annonce...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    vm.sendMessage(chatItemId, inputText.trim())
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Envoyer",
                                tint = if (inputText.isNotBlank()) profColor else Color.Gray
                            )
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
