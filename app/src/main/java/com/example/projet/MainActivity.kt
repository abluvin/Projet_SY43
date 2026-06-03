package com.example.projet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.data.ScheduleParser
import com.example.projet.ui.agenda.AgendaScreen
import com.example.projet.ui.agenda.AgendaViewModel
import com.example.projet.ui.camera.CameraScreen
import com.example.projet.data.ChatItem
import com.example.projet.ui.Register.Register
import com.example.projet.ui.chat.ChatScreen
import com.example.projet.ui.chat.ConversationScreen
import com.example.projet.ui.chat.CourseScreen
import com.example.projet.ui.chat.NewChatDialog
import com.example.projet.ui.home.CreatePostScreen
import com.example.projet.ui.home.HomeScreen
import com.example.projet.ui.sessions.CollaborationViewModel
import com.example.projet.ui.theme.ProjetTheme

enum class Screen {
    HOME, AGENDA, CHAT, GROUPS, MENU, CREATE_POST, CONVERSATION, COURSE_HUB
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                AppRoot()
            }
        }
    }
}

private enum class AppState { REGISTER, WELCOME, MAIN }

@Composable
fun AppRoot() {
    var appState by remember { mutableStateOf(AppState.REGISTER) }
    var username by remember { mutableStateOf("") }

    when (appState) {
        AppState.REGISTER -> Register(onRegistered = { name ->
            username = name
            appState = AppState.WELCOME
        })
        AppState.WELCOME -> WelcomeScreen(
            username = username,
            onContinue = { appState = AppState.MAIN }
        )
        AppState.MAIN -> MainApp(username = username)
    }
}

@Composable
fun WelcomeScreen(username: String, onContinue: () -> Unit) {
    val utbmBlue = Color(0xFF0055A4)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👋", fontSize = 64.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Bienvenue,",
            fontSize = 28.sp,
            color = Color.Gray
        )
        Text(
            text = username,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = utbmBlue
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Votre compte UTBM a été créé avec succès.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
        ) {
            Text("Commencer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MainApp(username: String = "") {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var previousScreen by remember { mutableStateOf(Screen.HOME) }
    var showCamera by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }
    
    var selectedChatItem by remember { mutableStateOf<ChatItem?>(null) }

    val agendaVM: AgendaViewModel = viewModel()
    val collaborationVM: CollaborationViewModel = viewModel()
    val userId = "user_${username.lowercase().replace(" ", "_")}"

    val navItems = listOf(
        BottomNavItem(Screen.HOME, "Accueil", Icons.Filled.Home),
        BottomNavItem(Screen.AGENDA, "Agenda", Icons.Filled.DateRange),
        BottomNavItem(Screen.CHAT, "Chat", Icons.Filled.MoreVert),
        BottomNavItem(Screen.GROUPS, "Collaboration", Icons.Filled.Groups),
        BottomNavItem(Screen.MENU, "Menu", Icons.Filled.Settings)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentScreen != Screen.CREATE_POST && currentScreen != Screen.CONVERSATION && currentScreen != Screen.COURSE_HUB) {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                Icon(item.icon, contentDescription = item.label)
                            },
                            label = {
                                Text(item.label, style = MaterialTheme.typography.labelSmall)
                            },
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
        }
    ) { innerPadding ->
        if (showCamera) {
            CameraScreen(
                onTextRecognized = { text ->
                    recognizedText = text
                    showCamera = false
                    val events = ScheduleParser.parseScheduleText(text)
                },
                onBack = { showCamera = false }
            )
        } else {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                    username = username,
                    onCreatePostClick = {
                        previousScreen = Screen.HOME
                        currentScreen = Screen.CREATE_POST
                    }
                )
                Screen.AGENDA -> AgendaScreen(
                    modifier = Modifier.padding(innerPadding),
                    onCameraClick = { showCamera = true },
                    onPasteClick = { showPasteDialog = true },
                    vm = agendaVM
                )
                Screen.CHAT -> ChatScreen(
                    modifier = Modifier.padding(innerPadding),
                    onConversationClick = { chatItem ->
                        selectedChatItem = chatItem
                        currentScreen = Screen.CONVERSATION
                    },
                    onCourseHubClick = {
                        currentScreen = Screen.COURSE_HUB
                    },
                    onNewChatClick = {
                        showNewChatDialog = true
                    }
                )
                Screen.GROUPS -> CollaborationScreen(
                    modifier = Modifier.padding(innerPadding),
                    username = username,
                    userId = userId,
                    vm = collaborationVM
                )
                Screen.MENU -> PlaceholderScreen(
                    "Restaurant",
                    Modifier.padding(innerPadding)
                )
                Screen.CREATE_POST -> CreatePostScreen(
                    onPostCreated = { text, uri ->
                        currentScreen = previousScreen
                    },
                    onBack = {
                        currentScreen = previousScreen
                    }
                )
                Screen.CONVERSATION -> {
                    selectedChatItem?.let { item ->
                        ConversationScreen(
                            chatItem = item,
                            onBack = { currentScreen = Screen.CHAT }
                        )
                    }
                }
                Screen.COURSE_HUB -> CourseScreen(
                    courseName = "SY43 - Plateformes Mobiles",
                    onBack = { currentScreen = Screen.CHAT }
                )
            }
        }

        if (showNewChatDialog) {
            NewChatDialog(
                onDismiss = { showNewChatDialog = false },
                onConfirm = { recipients ->
                    showNewChatDialog = false
                    // Logic to start chat or group
                    if (recipients.size >= 2) {
                        // Create group logic
                    } else if (recipients.isNotEmpty()) {
                        // Start direct chat logic
                    }
                }
            )
        }

        if (recognizedText.isNotEmpty()) {
            Dialog(onDismissRequest = { recognizedText = "" }) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                    Column(modifier = Modifier.padding(16.dp)) {
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
                            verticalAlignment = Alignment.CenterVertically) {
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

@Composable
fun PlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ProjetTheme {
        AppRoot()
    }
}
