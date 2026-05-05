package com.example.projet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.ui.agenda.AgendaViewModel
import com.example.projet.data.ScheduleParser
import com.example.projet.ui.home.HomeScreen
import com.example.projet.ui.agenda.AgendaScreen
import com.example.projet.ui.camera.CameraScreen
import com.example.projet.ui.theme.ProjetTheme

enum class Screen {
    HOME, AGENDA, CHAT, GROUPS, MENU
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var showCamera by remember { mutableStateOf(false) }
                var showPasteDialog by remember { mutableStateOf(false) }
                var recognizedText by remember { mutableStateOf("") }
                var pasteText by remember { mutableStateOf("") }

                val agendaVM: AgendaViewModel = viewModel()

                val navItems = listOf(
                    BottomNavItem(Screen.HOME, "Home", Icons.Filled.Home),
                    BottomNavItem(Screen.AGENDA, "Agenda", Icons.Filled.DateRange),
                    BottomNavItem(Screen.CHAT, "Chat", Icons.Filled.MoreVert),
                    BottomNavItem(Screen.GROUPS, "Groupes", Icons.Filled.Person),
                    BottomNavItem(Screen.MENU, "Menu", Icons.Filled.Settings)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            navItems.forEach { item ->
                                NavigationBarItem(
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                    selected = currentScreen == item.screen,
                                    onClick = {
                                        if (!showCamera) {
                                            currentScreen = item.screen
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    if (showCamera) {
                        CameraScreen(
                            onTextRecognized = { text ->
                                recognizedText = text
                                showCamera = false
                                // Parse the recognized text into events
                                val events = ScheduleParser.parseScheduleText(text)
                                // TODO: Add events to database/storage
                            },
                            onBack = { showCamera = false }
                        )
                    } else {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(
                                modifier = Modifier.padding(innerPadding)
                            )
                            Screen.AGENDA -> AgendaScreen(
                                modifier = Modifier.padding(innerPadding),
                                onCameraClick = { showCamera = true },
                                onPasteClick = { showPasteDialog = true },
                                vm = agendaVM
                            )
                            Screen.CHAT -> PlaceholderScreen("Chat", Modifier.padding(innerPadding))
                            Screen.GROUPS -> PlaceholderScreen("Groupes", Modifier.padding(innerPadding))
                            Screen.MENU -> PlaceholderScreen("Menu", Modifier.padding(innerPadding))
                        }
                    }
                }

                if (recognizedText.isNotEmpty()) {
                    Dialog(onDismissRequest = { recognizedText = "" }) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(0.9f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Texte reconnu",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(recognizedText, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { recognizedText = "" },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Annuler")
                                    }
                                    Button(
                                        onClick = {
                                            val events = ScheduleParser.parseScheduleText(recognizedText)
                                            if (events.isNotEmpty()) {
                                                agendaVM.addEvents(events)
                                            }
                                            recognizedText = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Ajouter")
                                    }
                                }
                            }
                        }
                    }
                }

                if (showPasteDialog) {
                    Dialog(onDismissRequest = { showPasteDialog = false }) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(0.9f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Coller votre emploi du temps",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                OutlinedTextField(
                                    value = pasteText,
                                    onValueChange = { pasteText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    label = { Text("Texte d'ADE") },
                                    placeholder = { Text("Collez votre texte ici...") }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            showPasteDialog = false
                                            pasteText = ""
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Annuler")
                                    }
                                    Button(
                                        onClick = {
                                            val events = ScheduleParser.parseScheduleText(pasteText)
                                            if (events.isNotEmpty()) {
                                                agendaVM.addEvents(events)
                                            }
                                            showPasteDialog = false
                                            pasteText = ""
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = pasteText.isNotEmpty()
                                    ) {
                                        Text("Ajouter")
                                    }
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
private fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "À venir...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
