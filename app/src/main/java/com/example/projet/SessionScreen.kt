package com.example.projet

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.CampusType
import com.example.projet.data.SessionRevision
import com.example.projet.ui.sessions.SessionRevisionViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val utbmBlue = Color(0xFF0055A4)

private fun ueColor(code: String): Color = when {
    code.startsWith("SY") -> Color(0xFF1565C0)
    code.startsWith("MA") -> Color(0xFF4A148C)
    code.startsWith("LO") -> Color(0xFF1B5E20)
    code.startsWith("HM") -> Color(0xFF006064)
    code.startsWith("TC") -> Color(0xFF880E4F)
    else -> Color(0xFF37474F)
}

@Composable
fun SessionScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    userId: String = "user_me",
    vm: SessionRevisionViewModel = viewModel()
) {
    val sessions by vm.sessions.collectAsState()
    val filterCampus by vm.filterCampus.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedSession by remember { mutableStateOf<SessionRevision?>(null) }

    val filtered = sessions.filter { filterCampus == null || it.campus == filterCampus }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Séances de révision",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Collaborez et réservez une salle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = utbmBlue),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Créer")
                }
            }
        }

        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterCampus == null,
                        onClick = { vm.setFilterCampus(null) },
                        label = { Text("Tous") }
                    )
                }
                items(CampusType.entries.toList()) { campus ->
                    FilterChip(
                        selected = filterCampus == campus,
                        onClick = { vm.setFilterCampus(if (filterCampus == campus) null else campus) },
                        label = { Text(campus.label) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune séance pour ce campus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { session ->
            SessionCard(
                session = session,
                userId = userId,
                onJoin = { vm.joinSession(session.id, userId, username) },
                onLeave = { vm.leaveSession(session.id, userId) },
                onDetails = { selectedSession = session }
            )
            Spacer(Modifier.height(8.dp))
        }

        item { Spacer(Modifier.height(16.dp)) }
    }

    selectedSession?.let { session ->
        SessionDetailDialog(
            session = session,
            userId = userId,
            onDismiss = { selectedSession = null },
            onJoin = { vm.joinSession(session.id, userId, username); selectedSession = null },
            onLeave = { vm.leaveSession(session.id, userId); selectedSession = null }
        )
    }

    if (showCreateDialog) {
        CreateSessionDialog(
            username = username,
            userId = userId,
            onDismiss = { showCreateDialog = false },
            onCreate = { session ->
                vm.createSession(session)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: SessionRevision,
    userId: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onDetails: () -> Unit
) {
    val context = LocalContext.current
    val color = ueColor(session.ue)
    val isJoined = session.participantIds.contains(userId)
    val isFull = session.status == "FULL"
    val isCreator = session.creatorId == userId
    val dateFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onDetails,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = session.seance_name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = session.ue,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = session.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${session.date.format(dateFmt)}  •  ${session.startTime.format(timeFmt)}–${session.endTime.format(timeFmt)}  •  ${session.campus.label} ${session.room}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${session.participantIds.size}/${session.maxParticipants}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                val texte = "Séance : ${session.seance_name}\n" +
                                    "UV : ${session.ue}\n" +
                                    "Date : ${session.date.format(dateFmt)}\n" +
                                    "Horaires : ${session.startTime.format(timeFmt)}–${session.endTime.format(timeFmt)}\n" +
                                    "Salle : ${session.campus.label} ${session.room}\n" +
                                    "Organisateur : ${session.creatorName}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, texte)
                                }
                                context.startActivity(Intent.createChooser(intent, "Partager la séance"))
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Partager",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (!isCreator) {
                            if (isJoined) {
                                OutlinedButton(
                                    onClick = onLeave,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("Quitter", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Button(
                                    onClick = onJoin,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = utbmBlue),
                                    enabled = !isFull
                                ) {
                                    Text(
                                        text = if (isFull) "Complet" else "Rejoindre",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionDetailDialog(
    session: SessionRevision,
    userId: String,
    onDismiss: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    val isJoined = session.participantIds.contains(userId)
    val isFull = session.status == "FULL"
    val isCreator = session.creatorId == userId
    val dateFmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH)
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(session.seance_name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${session.ue}  •  ${session.date.format(dateFmt)}  •  ${session.startTime.format(timeFmt)}–${session.endTime.format(timeFmt)}")
                Text("${session.campus.label}, salle ${session.room}")
                Text("Organisé par ${session.creatorName}")
                Spacer(Modifier.height(2.dp))
                Text(
                    "${session.participantNames.joinToString(", ")}  (${session.participantIds.size}/${session.maxParticipants})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            if (!isCreator) {
                if (isJoined) {
                    TextButton(onClick = onLeave) { Text("Quitter", color = MaterialTheme.colorScheme.error) }
                } else {
                    Button(
                        onClick = onJoin,
                        enabled = !isFull,
                        colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
                    ) { Text(if (isFull) "Complet" else "Rejoindre") }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
private fun CreateSessionDialog(
    username: String,
    userId: String,
    onDismiss: () -> Unit,
    onCreate: (SessionRevision) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var ue by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var campus by remember { mutableStateOf(CampusType.SEVENANS) }
    var room by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var startStr by remember { mutableStateOf("14:00") }
    var endStr by remember { mutableStateOf("16:00") }
    var maxPart by remember { mutableStateOf("8") }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.heightIn(max = 620.dp)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Nouvelle séance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de la séance *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ue,
                        onValueChange = { ue = it.uppercase() },
                        label = { Text("Code UV (ex: SY43) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Column {
                        Text(
                            text = "Campus *",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CampusType.entries.forEach { ct ->
                                FilterChip(
                                    selected = campus == ct,
                                    onClick = { campus = ct },
                                    label = { Text(ct.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it.uppercase() },
                        label = { Text("Salle (ex: M204) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = dateStr,
                        onValueChange = { dateStr = it; dateError = false },
                        label = { Text("Date (AAAA-MM-JJ) *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = dateError,
                        supportingText = if (dateError) { { Text("Format invalide, ex: 2026-05-25") } } else null
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startStr,
                            onValueChange = { startStr = it; timeError = false },
                            label = { Text("Début *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = timeError
                        )
                        OutlinedTextField(
                            value = endStr,
                            onValueChange = { endStr = it; timeError = false },
                            label = { Text("Fin *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            isError = timeError
                        )
                    }
                    if (timeError) {
                        Text(
                            text = "Format invalide, ex: 14:00",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    OutlinedTextField(
                        value = maxPart,
                        onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) maxPart = it },
                        label = { Text("Max participants") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) { Text("Annuler") }
                        Button(
                            onClick = {
                                val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { null }
                                val start = try { LocalTime.parse(startStr) } catch (e: Exception) { null }
                                val end = try { LocalTime.parse(endStr) } catch (e: Exception) { null }
                                if (date == null) { dateError = true; return@Button }
                                if (start == null || end == null) { timeError = true; return@Button }
                                onCreate(
                                    SessionRevision(
                                        id = UUID.randomUUID().toString(),
                                        seance_name = name.trim(),
                                        ue = ue.trim(),
                                        description = description.trim(),
                                        campus = campus,
                                        room = room.trim(),
                                        creatorId = userId,
                                        creatorName = username,
                                        participantIds = listOf(userId),
                                        participantNames = listOf(username),
                                        status = "OPEN",
                                        date = date,
                                        startTime = start,
                                        endTime = end,
                                        maxParticipants = maxPart.toIntOrNull() ?: 8
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = utbmBlue),
                            enabled = name.isNotBlank() && ue.isNotBlank() && room.isNotBlank()
                        ) { Text("Créer") }
                    }
                }
            }
        }
    }
}
