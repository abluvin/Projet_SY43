package com.example.projet.ui.agenda

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.Event
import com.example.projet.data.EventType
import com.example.projet.data.SampleData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    vm: AgendaViewModel = viewModel()
) {
    val weekStart by vm.weekStart.collectAsState()
    val selected by vm.selectedDate.collectAsState()
    val events by vm.events.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        WeekHeader(weekStart, vm::prevWeek, vm::nextWeek)
        DaySelector(weekStart, selected, vm::selectDate)
        HorizontalDivider()
        if (events.isEmpty()) {
            EmptyDay()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(events) { EventCard(it) }
            }
        }
    }
}

@Composable
private fun WeekHeader(
    weekStart: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)
    val label = "${weekStart.format(fmt)} – ${weekStart.plusDays(4).format(fmt)} ${weekStart.year}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Semaine précédente")
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Semaine suivante")
        }
    }
}

@Composable
private fun DaySelector(
    weekStart: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val dayLabels = listOf("L", "M", "M", "J", "V")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 0..4) {
            val date = weekStart.plusDays(i.toLong())
            val isSelected = date == selected
            val isToday = date == today
            val hasEvents = SampleData.getEventsForDate(date).isNotEmpty()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { onSelect(date) }
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
