package com.example.projet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.ui.agenda.AgendaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit,
    vm: AgendaViewModel = viewModel()
) {
    val weekStart by vm.weekStart.collectAsState()
    val selected by vm.selectedDate.collectAsState()
    val events by vm.events.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
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
                    .padding(bottom = 16.dp),
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
            Spacer(Modifier.height(8.dp))
        }

        // Planning content
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                repeat(5) { dayIndex ->
                    val date = weekStart.plusDays(dayIndex.toLong())
                    val dayEvents = events.filter { it.date == date }.sortedBy { it.startTime }

                    if (dayEvents.isNotEmpty()) {
                        DaySection(date, dayEvents)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DaySection(date: LocalDate, dayEvents: List<com.example.projet.data.Event>) {
    val fmt = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)

    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = fmt.format(date).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        dayEvents.forEach { event ->
            EventTimelineItem(event)
        }
    }
}

@Composable
private fun EventTimelineItem(event: com.example.projet.data.Event) {
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    val color = when (event.code) {
        "SY43" -> Color(0xFF1565C0)
        "SY41" -> Color(0xFFBF360C)
        "MA50" -> Color(0xFF4A148C)
        "LO43" -> Color(0xFF1B5E20)
        "HM40" -> Color(0xFF006064)
        else   -> Color(0xFF37474F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        // Time and line
        Column(
            modifier = Modifier.width(50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = event.startTime.format(fmt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp)
                    .background(color, RoundedCornerShape(1.5.dp))
            )
        }

        Spacer(Modifier.width(12.dp))

        // Event info
        Column(
            modifier = Modifier
                .weight(1f)
                .background(
                    color.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
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
            if (event.location.isNotEmpty() || event.instructor.isNotEmpty()) {
                Text(
                    text = "${event.location}  •  ${event.instructor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}





