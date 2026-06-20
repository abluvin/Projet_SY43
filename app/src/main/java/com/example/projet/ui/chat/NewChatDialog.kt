package com.example.projet.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projet.data.User

@Composable
fun NewChatDialog(
    allUsers: List<User>,
    currentUserId: Int,
    onDismiss: () -> Unit,
    onStartDirectChat: (User) -> Unit,
    onCreateGroup: (List<User>) -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    var searchQuery by remember { mutableStateOf("") }
    val selectedUsers = remember { mutableStateListOf<User>() }

    val filteredUsers = remember(searchQuery, allUsers) {
        if (searchQuery.isBlank()) allUsers.filter { it.id != currentUserId }
        else allUsers.filter {
            it.id != currentUserId &&
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val isGroup = selectedUsers.size >= 2
    val title = when {
        selectedUsers.isEmpty() -> "Nouvelle discussion"
        isGroup -> "Nouveau groupe (${selectedUsers.size})"
        else -> "Discussion avec ${selectedUsers[0].name}"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = utbmBlue
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Chips des personnes sélectionnées
                if (selectedUsers.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(selectedUsers) { user ->
                            SelectedUserChip(
                                user = user,
                                onRemove = { selectedUsers.remove(user) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Barre de recherche
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Rechercher par nom…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = utbmBlue,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Liste des utilisateurs
                if (filteredUsers.isEmpty() && searchQuery.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucun utilisateur trouvé",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(filteredUsers) { user ->
                            val isSelected = user in selectedUsers
                            UserSearchItem(
                                user = user,
                                isSelected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedUsers.remove(user)
                                    } else {
                                        selectedUsers.add(user)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            when {
                                selectedUsers.size == 1 -> onStartDirectChat(selectedUsers[0])
                                selectedUsers.size >= 2 -> onCreateGroup(selectedUsers.toList())
                            }
                        },
                        enabled = selectedUsers.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
                    ) {
                        Text(if (isGroup) "Créer le groupe" else "Démarrer")
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedUserChip(user: User, onRemove: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = utbmBlue.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.name.split(" ").first(),
                fontSize = 13.sp,
                color = utbmBlue,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Retirer",
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove),
                tint = utbmBlue
            )
        }
    }
}

@Composable
private fun UserSearchItem(user: User, isSelected: Boolean, onClick: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Surface(
        onClick = onClick,
        color = if (isSelected) utbmBlue.copy(alpha = 0.08f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) utbmBlue else Color(0xFFD1D1D1)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) utbmBlue else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Text(
                    text = if (user.role == "PROFESSOR") "Professeur" else "Étudiant",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(utbmBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
