package com.example.projet.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.*
import com.example.projet.ui.components.UtbmLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onConversationClick: (ChatItem) -> Unit,
    onCourseHubClick: () -> Unit,
    onNewChatClick: () -> Unit,
    isProf: Boolean = false,
    profName: String = "",
    modifier: Modifier = Modifier,
    vm: ChatViewModel = viewModel()
) {
    val utbmBlue = Color(0xFF0055A4)
    val profColor = Color(0xFF34A853)

    val allChatItems by vm.conversations.collectAsState()
    var showCreateHubDialog by remember { mutableStateOf(false) }

    val filters = listOf("Tout", "Non lus", "Groupes", "Cours")
    var selectedFilter by remember { mutableStateOf("Tout") }

    val filteredItems = remember(selectedFilter, allChatItems) {
        when (selectedFilter) {
            "Non lus" -> allChatItems.filter { it.unreadCount > 0 }
            "Groupes" -> allChatItems.filter { it.isGroup }
            "Cours" -> allChatItems.filter { it.isCourse }
            else -> allChatItems
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isProf) {
                    SmallFloatingActionButton(
                        onClick = { showCreateHubDialog = true },
                        containerColor = profColor,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.School, contentDescription = "Créer un hub")
                    }
                }
                FloatingActionButton(
                    onClick = onNewChatClick,
                    containerColor = utbmBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Nouveau Message")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            ChatHeader()

            // Search Bar
            SearchBar()

            // Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        label = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Hub de cours
                item {
                    CourseHubCard(onClick = onCourseHubClick)
                }

                // Chat List
                items(filteredItems) { item ->
                    val lastMsg by vm.getLastMessage(item.id).collectAsState(initial = item.lastMessage)
                    ChatListItem(
                        item = item,
                        lastMessage = lastMsg ?: item.lastMessage,
                        onClick = { onConversationClick(item) }
                    )
                }
            }
        }
    }

    if (showCreateHubDialog) {
        CreateHubDialog(
            onDismiss = { showCreateHubDialog = false },
            onCreate = { hubName ->
                vm.createCourseHub(hubName, profName)
                showCreateHubDialog = false
            }
        )
    }
}

@Composable
fun CreateHubDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var hubName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF34A853)) },
        title = { Text("Créer un hub de cours") },
        text = {
            OutlinedTextField(
                value = hubName,
                onValueChange = { hubName = it.uppercase() },
                label = { Text("Code ou nom de l'UE") },
                placeholder = { Text("ex: SY43, MT22...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (hubName.isNotBlank()) onCreate(hubName) },
                enabled = hubName.isNotBlank()
            ) { Text("Créer", color = Color(0xFF34A853)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
fun ChatHeader() {
    val utbmBlue = Color(0xFF0055A4)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        UtbmLogo(iconSize = 36.dp)
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = utbmBlue,
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(56.dp),
        placeholder = { Text("Rechercher messages, personnes ou groupes...", color = Color.Gray, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF1F3F4),
            focusedContainerColor = Color(0xFFF1F3F4),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent
        ),
        singleLine = true
    )
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) utbmBlue else Color(0xFFE8EAED),
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseHubCard(onClick: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = utbmBlue)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "HUB DE COURS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SY43",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Professeur : 14 nouveaux documents partagés",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .offset(x = (it * (-8)).dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8AB4F8))
                            .offset(x = (-16).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+12", color = utbmBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatListItem(item: ChatItem, lastMessage: String, onClick: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (item.isGroup || item.isCourse) Color(0xFFE8EAED) else Color(0xFFD1D1D1)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.isGroup) {
                        Icon(Icons.Default.Groups, contentDescription = null, tint = utbmBlue)
                    } else if (item.isCourse) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = utbmBlue)
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }
                if (item.hasOnlineStatus) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34A853))
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.time,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.messageType == MessageType.VOICE_MESSAGE) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Message vocal (0:42)", color = Color.Gray, fontSize = 14.sp)
                    } else if (item.messageType == MessageType.IMAGE) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("A envoyé une photo", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        Text(
                            text = lastMessage,
                            color = if (item.unreadCount > 0) Color.Black else Color.Gray,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (item.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    
                    if (item.unreadCount > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(utbmBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
