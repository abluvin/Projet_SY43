package com.example.projet.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.User
import com.example.projet.data.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    currentUserId: Int,
    vm: AdminViewModel = viewModel()
) {
    val utbmBlue = Color(0xFF0055A4)
    val users by vm.users.collectAsState()
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = utbmBlue)
                        Text("Gestion des utilisateurs", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = utbmBlue.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "${users.size} utilisateur${if (users.size > 1) "s" else ""} inscrit${if (users.size > 1) "s" else ""}",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = utbmBlue,
                    fontWeight = FontWeight.Medium
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        isSelf = user.id == currentUserId,
                        onToggleAdmin = { vm.toggleAdmin(user) },
                        onSetRole = { role -> vm.setRole(user, role) },
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Supprimer l'utilisateur") },
            text = { Text("Voulez-vous vraiment supprimer le compte de ${user.name} ?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteUser(user); userToDelete = null }) {
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Annuler") }
            }
        )
    }
}

@Composable
private fun UserCard(
    user: User,
    isSelf: Boolean,
    onToggleAdmin: () -> Unit,
    onSetRole: (String) -> Unit,
    onDelete: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    val adminColor = Color(0xFFFF9500)
    val profColor = Color(0xFF34A853)
    val isProf = user.role == UserRole.PROFESSOR.name

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar icon reflecting role
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = when {
                        user.isAdmin -> adminColor.copy(alpha = 0.15f)
                        isProf -> profColor.copy(alpha = 0.15f)
                        else -> utbmBlue.copy(alpha = 0.1f)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = when {
                                user.isAdmin -> Icons.Filled.AdminPanelSettings
                                isProf -> Icons.Filled.School
                                else -> Icons.Filled.Person
                            },
                            contentDescription = null,
                            tint = when {
                                user.isAdmin -> adminColor
                                isProf -> profColor
                                else -> utbmBlue
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Name + badges
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = user.name + if (isSelf) " (vous)" else "",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        RoleBadge(
                            label = if (isProf) "Professeur" else "Étudiant",
                            color = if (isProf) profColor else utbmBlue
                        )
                        if (user.isAdmin) {
                            RoleBadge(label = "Admin", color = adminColor)
                        }
                    }
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete button
                if (!isSelf) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF6B6B))
                    }
                }
            }

            // Action buttons (only for others)
            if (!isSelf) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Toggle primary role
                    OutlinedButton(
                        onClick = {
                            onSetRole(
                                if (isProf) UserRole.STUDENT.name else UserRole.PROFESSOR.name
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isProf) Icons.Filled.Person else Icons.Filled.School,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isProf) "→ Étudiant" else "→ Professeur",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    // Toggle admin
                    OutlinedButton(
                        onClick = onToggleAdmin,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (user.isAdmin) adminColor else Color.Gray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (user.isAdmin) "Retirer admin" else "+ Admin",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
