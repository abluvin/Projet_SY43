package com.example.projet.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.UE
import com.example.projet.data.UserRole
import com.example.projet.data.UserUEWithDetails

private val UtbmBlue = Color(0xFF0055A4)
private val ProfColor = Color(0xFF34A853)
private val AdminColor = Color(0xFFFF9500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Int,
    vm: ProfileViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(userId) { vm.load(userId) }

    val user by vm.user.collectAsState()
    val postCount by vm.postCount.collectAsState()
    val passwordResult by vm.passwordResult.collectAsState()
    val allUEs by vm.allUEs.collectAsState()
    val userUEs by vm.userUEs.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAddUEDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mon profil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = UtbmBlue)
                    }
                }
            )
        }
    ) { innerPadding ->
        val currentUser = user
        if (currentUser == null) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = UtbmBlue)
            }
        } else {
            val isProf = currentUser.role == UserRole.PROFESSOR.name

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = UtbmBlue.copy(alpha = 0.1f),
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = UtbmBlue,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentUser.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RoleBadge(
                        label = if (isProf) "Professeur" else "Étudiant",
                        icon = if (isProf) Icons.Filled.School else Icons.Filled.Person,
                        color = if (isProf) ProfColor else UtbmBlue
                    )
                    if (currentUser.isAdmin) {
                        RoleBadge(label = "Admin", icon = Icons.Filled.AdminPanelSettings, color = AdminColor)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = UtbmBlue)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier le profil")
                    }
                    Button(
                        onClick = { showPasswordDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = UtbmBlue.copy(alpha = 0.85f))
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mot de passe")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                BranchCard(
                    selectedBranch = currentUser.branch,
                    onBranchSelected = { vm.updateBranch(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                UEsCard(
                    userUEs = userUEs,
                    isProf = isProf,
                    onAddClick = { showAddUEDialog = true },
                    onRemove = { ueId -> vm.removeUE(ueId) },
                    onToggleEnseigne = { ueId, enseigne -> vm.updateEnseigne(ueId, enseigne) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Activité", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = UtbmBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publications", style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                postCount.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = UtbmBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showEditDialog) {
                EditProfileDialog(
                    initialName = currentUser.name,
                    onDismiss = { showEditDialog = false },
                    onSave = { newName ->
                        vm.updateName(newName)
                        showEditDialog = false
                    }
                )
            }

            if (showPasswordDialog) {
                PasswordChangeDialog(
                    passwordResult = passwordResult,
                    onConfirm = { current, new -> vm.changePassword(current, new) },
                    onDismiss = { showPasswordDialog = false; vm.resetPasswordResult() }
                )
            }

            if (showAddUEDialog) {
                val available = allUEs.filter { ue -> userUEs.none { it.ue.id == ue.id } }
                AddUEDialog(
                    availableUEs = available,
                    userBranch = currentUser.branch,
                    isProf = isProf,
                    onDismiss = { showAddUEDialog = false },
                    onConfirm = { ueId, enseigne ->
                        vm.addUE(ueId, enseigne)
                        showAddUEDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun UEsCard(
    userUEs: List<UserUEWithDetails>,
    isProf: Boolean,
    onAddClick: () -> Unit,
    onRemove: (Int) -> Unit,
    onToggleEnseigne: (Int, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mes UEs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${userUEs.size}/10",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (userUEs.size >= 10) Color(0xFFD32F2F) else UtbmBlue
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            if (userUEs.isEmpty()) {
                Text(
                    "Aucune UE sélectionnée",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                userUEs.forEach { item ->
                    UERow(
                        item = item,
                        isProf = isProf,
                        onRemove = { onRemove(item.ue.id) },
                        onToggleEnseigne = { enseigne -> onToggleEnseigne(item.ue.id, enseigne) }
                    )
                    if (item != userUEs.last()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onAddClick,
                enabled = userUEs.size < 10,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (userUEs.size >= 10) "Limite atteinte (10/10)" else "Ajouter une UE")
            }
        }
    }
}

@Composable
private fun UERow(
    item: UserUEWithDetails,
    isProf: Boolean,
    onRemove: () -> Unit,
    onToggleEnseigne: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = UtbmBlue.copy(alpha = 0.12f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                item.ue.code,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = UtbmBlue
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            item.ue.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (isProf) {
            Switch(
                checked = item.userUE.enseigne,
                onCheckedChange = onToggleEnseigne,
                colors = SwitchDefaults.colors(checkedThumbColor = ProfColor, checkedTrackColor = ProfColor.copy(alpha = 0.4f))
            )
        }
        IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Supprimer", tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
        }
    }
}

private val BRANCHES = listOf("TC", "Info", "Méca", "Industrie", "Méca Ergo", "Énergie")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BranchCard(selectedBranch: String, onBranchSelected: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ma branche", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(BRANCHES) { branch ->
                    FilterChip(
                        selected = selectedBranch == branch,
                        onClick = { onBranchSelected(if (selectedBranch == branch) "" else branch) },
                        label = { Text(branch) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UtbmBlue.copy(alpha = 0.15f),
                            selectedLabelColor = UtbmBlue
                        )
                    )
                }
            }
            if (selectedBranch.isBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sélectionnez votre branche pour filtrer les UEs disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddUEDialog(
    availableUEs: List<UE>,
    userBranch: String,
    isProf: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ueId: Int, enseigne: Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUE by remember { mutableStateOf<UE?>(null) }
    var enseigne by remember { mutableStateOf(false) }

    val filtered = availableUEs.filter { ue ->
        val matchesBranch = userBranch.isBlank() || ue.branch == userBranch || ue.branch == "Transversal"
        val matchesSearch = searchQuery.isBlank() ||
            ue.code.contains(searchQuery, ignoreCase = true) ||
            ue.name.contains(searchQuery, ignoreCase = true)
        matchesBranch && matchesSearch
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Ajouter une UE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (userBranch.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Branche $userBranch + Transversales",
                        style = MaterialTheme.typography.labelSmall,
                        color = UtbmBlue
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it; selectedUE = null },
                    label = { Text("Rechercher") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aucune UE disponible",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                        items(filtered) { ue ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedUE = ue }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedUE?.id == ue.id,
                                    onClick = { selectedUE = ue }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ue.code, fontWeight = FontWeight.Bold, color = UtbmBlue, style = MaterialTheme.typography.bodyMedium)
                                    Text(ue.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (ue.branch != "Transversal") {
                                    Surface(
                                        color = UtbmBlue.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            ue.branch,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = UtbmBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isProf) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("J'enseigne cette UE", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = enseigne,
                            onCheckedChange = { enseigne = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = ProfColor, checkedTrackColor = ProfColor.copy(alpha = 0.4f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Button(
                        onClick = { selectedUE?.let { onConfirm(it.id, enseigne) } },
                        enabled = selectedUE != null,
                        colors = ButtonDefaults.buttonColors(containerColor = UtbmBlue)
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le profil") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun PasswordChangeDialog(
    passwordResult: ProfileViewModel.PasswordResult,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val mismatch = newPwd.isNotEmpty() && confirm.isNotEmpty() && newPwd != confirm
    val wrongCurrent = passwordResult is ProfileViewModel.PasswordResult.WrongCurrent

    LaunchedEffect(passwordResult) {
        if (passwordResult is ProfileViewModel.PasswordResult.Success) onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Changer le mot de passe", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Mot de passe actuel") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = wrongCurrent,
                    supportingText = if (wrongCurrent) { { Text("Mot de passe incorrect", color = Color(0xFFD32F2F)) } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text("Nouveau mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirmer le nouveau mot de passe") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = mismatch,
                    supportingText = if (mismatch) { { Text("Les mots de passe ne correspondent pas", color = Color(0xFFD32F2F)) } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(current, newPwd) },
                enabled = current.isNotBlank() && newPwd.isNotBlank() && newPwd == confirm,
                colors = ButtonDefaults.buttonColors(containerColor = UtbmBlue)
            ) {
                Text("Confirmer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@Composable
private fun RoleBadge(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
