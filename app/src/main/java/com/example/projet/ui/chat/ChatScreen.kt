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
    val allUEs by vm.allUEs.collectAsState()
    val profUEs by vm.profUEs.collectAsState()
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            ChatHeader()

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
                items(filteredItems) { item ->
                    val lastMsg by vm.getLastMessage(item.id).collectAsState(initial = item.lastMessage)
                    val directName by vm.getDirectChatName(item.id).collectAsState(initial = null)
                    val displayItem = if (!item.isGroup && !item.isCourse && directName != null)
                        item.copy(name = directName!!)
                    else item
                    if (item.isCourse) {
                        CourseHubCard(
                            item = displayItem,
                            lastMessage = lastMsg ?: item.lastMessage,
                            onClick = { onConversationClick(item) }
                        )
                    } else {
                        ChatListItem(
                            item = displayItem,
                            lastMessage = lastMsg ?: item.lastMessage,
                            onClick = { onConversationClick(item) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateHubDialog) {
        val uesForPicker = if (profUEs.isNotEmpty()) profUEs.map { it.ue } else allUEs
        CreateHubDialog(
            availableUEs = uesForPicker,
            onDismiss = { showCreateHubDialog = false },
            onCreate = { hubName, ueCode ->
                vm.createCourseHub(hubName, profName, ueCode)
                showCreateHubDialog = false
            }
        )
    }
}

@Composable
fun CreateHubDialog(
    availableUEs: List<UE>,
    onDismiss: () -> Unit,
    onCreate: (name: String, ueCode: String?) -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    val profColor = Color(0xFF34A853)
    var searchQuery by remember { mutableStateOf("") }
    var selectedUE by remember { mutableStateOf<UE?>(null) }

    val filteredUEs = remember(searchQuery, availableUEs) {
        if (searchQuery.isBlank()) availableUEs
        else availableUEs.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        tint = profColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Créer un hub de cours",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = utbmBlue
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (selectedUE != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = profColor.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    selectedUE!!.code,
                                    fontWeight = FontWeight.Bold,
                                    color = profColor,
                                    fontSize = 16.sp
                                )
                                Text(
                                    selectedUE!!.name,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            TextButton(onClick = { selectedUE = null }) {
                                Text("Changer", color = profColor, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher une UE…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = profColor,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredUEs) { ue ->
                        val isSelected = selectedUE?.id == ue.id
                        Surface(
                            onClick = { selectedUE = if (isSelected) null else ue },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) profColor.copy(alpha = 0.12f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        ue.code,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) profColor else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        ue.name,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = profColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Annuler", color = Color.Gray) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val ue = selectedUE
                            if (ue != null) onCreate("${ue.code} – ${ue.name}", ue.code)
                        },
                        enabled = selectedUE != null,
                        colors = ButtonDefaults.buttonColors(containerColor = profColor)
                    ) {
                        Text("Créer")
                    }
                }
            }
        }
    }
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
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
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
fun CourseHubCard(item: ChatItem, lastMessage: String, onClick: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = utbmBlue)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "HUB DE COURS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (item.ueCode != null) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            item.ueCode,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = item.time,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessage,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                            .background(MaterialTheme.colorScheme.background)
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
                    Text(text = item.time, color = Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (item.messageType) {
                        MessageType.VOICE_MESSAGE -> {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Message vocal", color = Color.Gray, fontSize = 14.sp)
                        }
                        MessageType.IMAGE -> {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("A envoyé une photo", color = Color.Gray, fontSize = 14.sp)
                        }
                        MessageType.POLL -> {
                            Icon(Icons.Default.Poll, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sondage", color = Color.Gray, fontSize = 14.sp)
                        }
                        else -> {
                            Text(
                                text = lastMessage,
                                color = if (item.unreadCount > 0) MaterialTheme.colorScheme.onSurface else Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (item.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                            )
                        }
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
