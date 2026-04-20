package com.example.projet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
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
                var recognizedText by remember { mutableStateOf("") }

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
                            },
                            onBack = { showCamera = false }
                        )
                    } else {
                        when (currentScreen) {
                            Screen.HOME -> HomeScreen(
                                modifier = Modifier.padding(innerPadding),
                                onCameraClick = { showCamera = true }
                            )
                            Screen.AGENDA -> AgendaScreen(
                                modifier = Modifier.padding(innerPadding)
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
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Texte reconnu:")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(recognizedText)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { recognizedText = "" },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("OK")
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
