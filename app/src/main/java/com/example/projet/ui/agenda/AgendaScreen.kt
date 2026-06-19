package com.example.projet.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.InputChip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.Event
import com.example.projet.data.EventType
import com.example.projet.data.SampleData
import com.example.projet.ui.chat.ChatHeader
import com.example.projet.ui.theme.ProjetTheme
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// Couleur associée à chaque UV
private fun uvColor(code: String): Color = when (code) {
    "SY43" -> Color(0xFF1565C0)
    "SY41" -> Color(0xFFBF360C)
    "MA50" -> Color(0xFF4A148C)
    "LO43" -> Color(0xFF1B5E20)
    "HM40" -> Color(0xFF006064)
    else   -> Color(0xFF37474F)
}

@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit = {},
    onPasteClick: () -> Unit = {},
    isProf: Boolean = false,
    isAdmin: Boolean = false,
    userId: Int = 0,
    vm: AgendaViewModel = viewModel()
) {
    LaunchedEffect(userId) { vm.setUserId(userId) }

    val weekStart by vm.weekStart.collectAsState()
    val selected by vm.selectedDate.collectAsState()
    val events by vm.events.collectAsState()
    val datesWithEvents by vm.datesWithEvents.collectAsState()
    val allUECodes by vm.allUECodes.collectAsState()
    val isPrivileged = isProf || isAdmin
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = if (isPrivileged) 88.dp else 0.dp)
    ) {

        // Header section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ChatHeader()
                Text(
                    text = "Ajouter votre planning",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Importez vos cours en un clin d'œil. Notre IA détecte automatiquement les salles, horaires et codes d'UV.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // OCR Import Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
                    .clickable { onCameraClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1565C0)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "📷",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = "Importer via Photo/OCR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Prenez en photo votre écran ou importez un PDF",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Copy Paste Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .clickable { onPasteClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "📋",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            text = "Copier-Coller le texte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Utilisez le texte brut d'ADE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Mon Planning section
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Mon Planning",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)
            Text(
                text = "SEMAINE DU ${weekStart.format(fmt).uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Week navigation
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = vm::prevWeek) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Semaine précédente")
                }
                Text(
                    text = "${weekStart.format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))} – ${weekStart.plusDays(4).format(DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH))} ${weekStart.year}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = vm::nextWeek) {
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Semaine suivante")
                }
            }
        }

        // Day selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val dayLabels = listOf("L", "M", "M", "J", "V")
                val today = LocalDate.now()

                repeat(5) { i ->
                    val date = weekStart.plusDays(i.toLong())
                    val isSelected = date == selected
                    val isToday = date == today
                    val hasEvents = date in datesWithEvents

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { vm.selectDate(date) }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Text(
                            text = dayLabels[i],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        } else {
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }

        // Events
        item {
            Spacer(Modifier.height(8.dp))
        }

        if (events.isEmpty()) {
            item {
                EmptyDay()
            }
        } else {
            items(events.size) { index ->
                EventCard(events[index])
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    } // end LazyColumn

    if (isPrivileged) {
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF0055A4),
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter un événement")
        }
    }
    } // end Box

    if (showCreateDialog) {
        CreateEventDialog(
            initialDate = selected,
            availableUECodes = allUECodes,
            onDismiss = { showCreateDialog = false },
            onCreate = { event ->
                vm.addEvent(event)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EmptyDay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😌", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text("Pas de cours ce jour", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Profitez-en pour réviser !",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private val AGENDA_BRANCHES = listOf("TC", "Info", "Méca", "Industrie", "Méca Ergo", "Énergie")

@Composable
private fun CreateEventDialog(
    initialDate: LocalDate,
    availableUECodes: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onCreate: (Event) -> Unit
) {
    val timeFmt = DateTimeFormatter.ofPattern("H:mm")
    var title by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(initialDate.toString()) }
    var startText by remember { mutableStateOf("08:00") }
    var endText by remember { mutableStateOf("10:00") }
    var selectedType by remember { mutableStateOf(EventType.COURS) }
    var isGlobal by remember { mutableStateOf(true) }
    var targetUECodes by remember { mutableStateOf(listOf<String>()) }
    var targetBranches by remember { mutableStateOf(listOf<String>()) }
    var ueMenuExpanded by remember { mutableStateOf(false) }
    var branchMenuExpanded by remember { mutableStateOf(false) }

    val dateValid = runCatching { LocalDate.parse(dateText) }.isSuccess
    val startValid = runCatching { LocalTime.parse(startText, timeFmt) }.isSuccess
    val endValid = runCatching { LocalTime.parse(endText, timeFmt) }.isSuccess
    val targetsValid = isGlobal || targetUECodes.isNotEmpty() || targetBranches.isNotEmpty()
    val isValid = title.isNotBlank() && code.isNotBlank() && location.isNotBlank() &&
        instructor.isNotBlank() && dateValid && startValid && endValid && targetsValid

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Nouvel événement",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Titre *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Code UE * (ex: SY43)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = location, onValueChange = { location = it },
                    label = { Text("Salle * (ex: A101)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = instructor, onValueChange = { instructor = it },
                    label = { Text("Intervenant *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = dateText, onValueChange = { dateText = it },
                    label = { Text("Date * (AAAA-MM-JJ)") },
                    isError = dateText.isNotBlank() && !dateValid,
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startText, onValueChange = { startText = it },
                        label = { Text("Début *") },
                        isError = startText.isNotBlank() && !startValid,
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    OutlinedTextField(
                        value = endText, onValueChange = { endText = it },
                        label = { Text("Fin *") },
                        isError = endText.isNotBlank() && !endValid,
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }

                HorizontalDivider()

                // Visibilité
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visible par tous", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isGlobal,
                        onCheckedChange = {
                            isGlobal = it
                            if (it) { targetUECodes = emptyList(); targetBranches = emptyList() }
                        }
                    )
                }

                if (!isGlobal) {
                    // Ciblage par UE
                    Text("UEs concernées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (targetUECodes.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(targetUECodes) { ueCode ->
                                InputChip(
                                    selected = false,
                                    onClick = { targetUECodes = targetUECodes - ueCode },
                                    label = { Text(ueCode) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                    Box {
                        OutlinedButton(
                            onClick = { ueMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ajouter une UE")
                        }
                        DropdownMenu(
                            expanded = ueMenuExpanded,
                            onDismissRequest = { ueMenuExpanded = false },
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            val available = availableUECodes.filter { it !in targetUECodes }
                            if (available.isEmpty()) {
                                DropdownMenuItem(text = { Text("Toutes les UEs déjà ajoutées", color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = { ueMenuExpanded = false })
                            } else {
                                available.forEach { ueCode ->
                                    DropdownMenuItem(
                                        text = { Text(ueCode) },
                                        onClick = { targetUECodes = targetUECodes + ueCode; ueMenuExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Ciblage par branche
                    Text("Branches concernées", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (targetBranches.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(targetBranches) { branch ->
                                InputChip(
                                    selected = false,
                                    onClick = { targetBranches = targetBranches - branch },
                                    label = { Text(branch) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                    Box {
                        OutlinedButton(
                            onClick = { branchMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Ajouter une branche")
                        }
                        DropdownMenu(
                            expanded = branchMenuExpanded,
                            onDismissRequest = { branchMenuExpanded = false }
                        ) {
                            AGENDA_BRANCHES.filter { it !in targetBranches }.forEach { branch ->
                                DropdownMenuItem(
                                    text = { Text(branch) },
                                    onClick = { targetBranches = targetBranches + branch; branchMenuExpanded = false }
                                )
                            }
                        }
                    }

                    if (!targetsValid) {
                        Text(
                            "Ajoutez au moins une UE ou une branche",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider()
                Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EventType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = uvColor(code),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuler") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onCreate(
                                Event(
                                    id = UUID.randomUUID().toString(),
                                    title = title.trim(),
                                    code = code.trim(),
                                    location = location.trim(),
                                    instructor = instructor.trim(),
                                    date = LocalDate.parse(dateText),
                                    startTime = LocalTime.parse(startText, timeFmt),
                                    endTime = LocalTime.parse(endText, timeFmt),
                                    type = selectedType,
                                    isGlobal = isGlobal,
                                    targetUECodes = targetUECodes,
                                    targetBranches = targetBranches
                                )
                            )
                        },
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055A4))
                    ) { Text("Créer") }
                }
            }
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    val color = uvColor(event.code)
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val typeColor = when (event.type) {
        EventType.EXAM  -> MaterialTheme.colorScheme.errorContainer
        EventType.TP    -> Color(0xFFE8F5E9)
        EventType.TD    -> Color(0xFFFFF9C4)
        else            -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = typeColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = event.type.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${event.startTime.format(fmt)} – ${event.endTime.format(fmt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${event.location}  •  ${event.instructor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

