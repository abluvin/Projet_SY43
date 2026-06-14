package com.example.projet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projet.data.ScheduleParser
import com.example.projet.ui.agenda.AgendaScreen
import com.example.projet.ui.agenda.AgendaViewModel
import com.example.projet.ui.camera.CameraScreen
import com.example.projet.ui.Register.Connexion
import com.example.projet.ui.Register.Register
import com.example.projet.ui.chat.ChatScreen
import com.example.projet.ui.chat.ConversationScreen
import com.example.projet.ui.chat.CourseScreen
import com.example.projet.ui.chat.NewChatDialog
import com.example.projet.ui.home.CreatePostScreen
import com.example.projet.ui.home.HomeScreen
import com.example.projet.ui.home.PostViewModel
import com.example.projet.ui.restaurant.MenuScreen
import com.example.projet.ui.sessions.CollaborationViewModel
import com.example.projet.ui.theme.ProjetTheme

object Routes {
    const val HOME = "home"
    const val AGENDA = "agenda"
    const val CHAT = "chat"
    const val GROUPS = "groups"
    const val MENU = "menu"
    const val CREATE_POST = "create_post"
    const val CONVERSATION = "conversation/{chatItemId}"
    const val COURSE_HUB = "course_hub"
    fun conversation(id: Int) = "conversation/$id"
}

data class BottomNavItem(
    val route: String,
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

private enum class AppState { LOGIN, REGISTER, WELCOME, MAIN }

@Composable
fun AppRoot() {
    var appState by remember { mutableStateOf(AppState.LOGIN) }
    var username by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf(0) }

    Crossfade(targetState = appState, label = "auth_state") { state ->
        when (state) {
            AppState.LOGIN -> Connexion(
                onLoggedIn = { name, id ->
                    username = name
                    userId = id
                    appState = AppState.MAIN
                },
                onNavigateToRegister = { appState = AppState.REGISTER }
            )
            AppState.REGISTER -> Register(
                onRegistered = { name, id ->
                    username = name
                    userId = id
                    appState = AppState.WELCOME
                },
                onNavigateToLogin = { appState = AppState.LOGIN }
            )
            AppState.WELCOME -> WelcomeScreen(
                username = username,
                onContinue = { appState = AppState.MAIN }
            )
            AppState.MAIN -> MainApp(username = username, userId = userId)
        }
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
fun MainApp(username: String = "", userId: Int = 0) {
    var showCamera by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }

    val agendaVM: AgendaViewModel = viewModel()
    val collaborationVM: CollaborationViewModel = viewModel()
    val postVM: PostViewModel = viewModel()
    val collaborationUserId = "user_${username.lowercase().replace(" ", "_")}"

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = setOf(Routes.HOME, Routes.AGENDA, Routes.CHAT, Routes.GROUPS, Routes.MENU)
    val navItems = listOf(
        BottomNavItem(Routes.HOME, "Accueil", Icons.Filled.Home),
        BottomNavItem(Routes.AGENDA, "Agenda", Icons.Filled.CalendarMonth),
        BottomNavItem(Routes.CHAT, "Chat", Icons.Filled.Forum),
        BottomNavItem(Routes.GROUPS, "Collab", Icons.Filled.Groups),
        BottomNavItem(Routes.MENU, "Restaurant", Icons.Filled.Restaurant)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute in bottomNavRoutes && !showCamera) {
                NavigationBar(modifier = Modifier.fillMaxWidth()) {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
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
                },
                onBack = { showCamera = false }
            )
        } else {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        username = username,
                        currentUserId = userId,
                        postVm = postVM,
                        onCreatePostClick = { navController.navigate(Routes.CREATE_POST) }
                    )
                }
                composable(Routes.AGENDA) {
                    AgendaScreen(
                        onCameraClick = { showCamera = true },
                        onPasteClick = { showPasteDialog = true },
                        vm = agendaVM
                    )
                }
                composable(Routes.CHAT) {
                    ChatScreen(
                        onConversationClick = { chatItem ->
                            navController.navigate(Routes.conversation(chatItem.id))
                        },
                        onCourseHubClick = { navController.navigate(Routes.COURSE_HUB) },
                        onNewChatClick = { showNewChatDialog = true }
                    )
                }
                composable(Routes.GROUPS) {
                    CollaborationScreen(
                        username = username,
                        userId = collaborationUserId,
                        vm = collaborationVM
                    )
                }
                composable(Routes.MENU) {
                    MenuScreen()
                }
                composable(Routes.CREATE_POST) {
                    CreatePostScreen(
                        onPostCreated = { text, uri, voiceFilePath, voiceDuration, isPoll ->
                            if (isPoll) {
                                // Pour les sondages: text contient la question
                                postVM.createPostWithPollOptions(text, uri?.toString(), userId, emptyList())
                            } else if (voiceFilePath != null) {
                                // Pour les posts vocaux
                                postVM.createPost(text, uri?.toString(), userId, voiceFilePath, voiceDuration, false)
                            } else {
                                // Pour les posts texte normaux
                                postVM.createPost(text, uri?.toString(), userId)
                            }
                            navController.popBackStack()
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = Routes.CONVERSATION,
                    arguments = listOf(navArgument("chatItemId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val chatItemId = backStackEntry.arguments?.getInt("chatItemId") ?: return@composable
                    ConversationScreen(
                        chatItemId = chatItemId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.COURSE_HUB) {
                    CourseScreen(
                        courseName = "SY43 - Plateformes Mobiles",
                        onBack = { navController.popBackStack() }
                    )
                }
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
