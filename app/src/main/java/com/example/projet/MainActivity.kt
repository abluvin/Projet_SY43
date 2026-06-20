package com.example.projet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AdminPanelSettings
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.projet.data.ScheduleParser
import com.example.projet.ui.profile.ProfileViewModel
import com.example.projet.ui.agenda.AgendaScreen
import com.example.projet.ui.agenda.AgendaViewModel
import com.example.projet.ui.camera.CameraScreen
import com.example.projet.ui.Register.Connexion
import com.example.projet.ui.Register.Register
import com.example.projet.ui.chat.ChatScreen
import com.example.projet.ui.chat.ChatViewModel
import com.example.projet.ui.chat.ConversationScreen
import com.example.projet.ui.chat.CourseScreen
import com.example.projet.ui.chat.NewChatDialog
import com.example.projet.ui.admin.AdminScreen
import com.example.projet.ui.admin.AdminViewModel
import com.example.projet.ui.home.CreatePostScreen
import com.example.projet.ui.home.HomeScreen
import com.example.projet.ui.home.PostViewModel
import com.example.projet.ui.restaurant.MenuScreen
import com.example.projet.ui.sessions.CollaborationViewModel
import com.example.projet.ui.profile.ProfileScreen
import com.example.projet.ui.settings.SettingsScreen
import com.example.projet.ui.theme.ProjetTheme
import com.example.projet.ui.components.UtbmLogo
import androidx.compose.foundation.background
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val AGENDA = "agenda"
    const val CHAT = "chat"
    const val GROUPS = "groups"
    const val MENU = "menu"
    const val ADMIN = "admin"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val CREATE_POST = "create_post"
    const val CONVERSATION = "conversation/{chatItemId}"
    const val COURSE_HUB = "course_hub/{chatItemId}"
    fun courseHub(id: Int) = "course_hub/$id"
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
            val systemDarkTheme = isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDarkTheme) }
            ProjetTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { isDarkTheme = it }
                    )
                }
            }
        }
    }
}

private enum class AppState { SPLASH, LOGIN, REGISTER, WELCOME, MAIN }

@Composable
fun AppRoot(
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {}
) {
    var appState by remember { mutableStateOf(AppState.SPLASH) }
    var username by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf(0) }
    var isAdmin by remember { mutableStateOf(false) }
    var userRole by remember { mutableStateOf("STUDENT") }

    Crossfade(targetState = appState, label = "auth_state") { state ->
        when (state) {
            AppState.SPLASH -> SplashScreen(onFinish = { appState = AppState.LOGIN })
            AppState.LOGIN -> Connexion(
                onLoggedIn = { name, id, admin, role ->
                    username = name; userId = id; isAdmin = admin; userRole = role
                    appState = AppState.MAIN
                },
                onNavigateToRegister = { appState = AppState.REGISTER }
            )
            AppState.REGISTER -> Register(
                onRegistered = { name, id, admin, role ->
                    username = name; userId = id; isAdmin = admin; userRole = role
                    appState = AppState.WELCOME
                },
                onNavigateToLogin = { appState = AppState.LOGIN }
            )
            AppState.WELCOME -> WelcomeScreen(
                username = username,
                userId = userId,
                onContinue = { appState = AppState.MAIN }
            )
            AppState.MAIN -> MainApp(
                username = username,
                userId = userId,
                isAdmin = isAdmin,
                userRole = userRole,
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                onLogout = {
                    username = ""; userId = 0; isAdmin = false; userRole = "STUDENT"
                    appState = AppState.LOGIN
                }
            )
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onFinish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            UtbmLogo(iconSize = 100.dp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    username: String,
    userId: Int,
    vm: ProfileViewModel = viewModel(),
    onContinue: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    val branches = listOf("TC", "Info", "Méca", "Industrie", "Méca Ergo", "Énergie")

    LaunchedEffect(userId) { vm.load(userId) }

    val allUEs by vm.allUEs.collectAsState()
    val userUEs by vm.userUEs.collectAsState()
    val user by vm.user.collectAsState()
    val selectedBranch = user?.branch ?: ""

    val filteredUEs = if (selectedBranch.isBlank()) emptyList()
    else allUEs.filter { it.branch == selectedBranch || it.branch == "Transversal" }

    val selectedUEIds = userUEs.map { it.ue.id }.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👋", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text("Bienvenue,", fontSize = 18.sp, color = Color.Gray)
        Text(username, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = utbmBlue)
        Spacer(Modifier.height(6.dp))
        Text(
            "Configurez votre profil pour commencer.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        // Branche
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Votre branche", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(branches) { branch ->
                        FilterChip(
                            selected = selectedBranch == branch,
                            onClick = { vm.updateBranch(if (selectedBranch == branch) "" else branch) },
                            label = { Text(branch) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = utbmBlue.copy(alpha = 0.15f),
                                selectedLabelColor = utbmBlue
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // UEs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vos UEs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (selectedBranch.isNotBlank()) {
                        Text(
                            "${userUEs.size}/10",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (userUEs.size >= 10) Color(0xFFD32F2F) else utbmBlue
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (selectedBranch.isBlank()) {
                    Text(
                        "Sélectionnez d'abord votre branche pour voir les UEs disponibles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    filteredUEs.forEachIndexed { index, ue ->
                        val isSelected = ue.id in selectedUEIds
                        val canSelect = !isSelected && userUEs.size < 10
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isSelected || canSelect) {
                                    if (isSelected) vm.removeUE(ue.id) else vm.addUE(ue.id, false)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (isSelected) vm.removeUE(ue.id)
                                    else if (canSelect) vm.addUE(ue.id, false)
                                },
                                enabled = isSelected || canSelect,
                                colors = CheckboxDefaults.colors(checkedColor = utbmBlue)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    ue.code,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSelected && !canSelect) Color.Gray else utbmBlue,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    ue.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (!isSelected && !canSelect) Color.Gray
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (index < filteredUEs.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(start = 48.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
        ) {
            Text("Commencer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (selectedBranch.isBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Vous pouvez aussi configurer ça plus tard depuis votre profil.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MainApp(
    username: String = "",
    userId: Int = 0,
    isAdmin: Boolean = false,
    userRole: String = "STUDENT",
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val isProf = userRole == "PROFESSOR"
    var showCamera by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var pasteText by remember { mutableStateOf("") }

    val agendaVM: AgendaViewModel = viewModel()
    val collaborationVM: CollaborationViewModel = viewModel()
    val postVM: PostViewModel = viewModel()
    val adminVM: AdminViewModel = viewModel()
    val chatVM: ChatViewModel = viewModel()

    LaunchedEffect(userId) {
        if (userId != 0) chatVM.initUser(userId)
    }
    val collaborationUserId = "user_${username.lowercase().replace(" ", "_")}"

    val postUserUECodes by postVM.userUECodes.collectAsState()
    val postUserBranch by postVM.userBranch.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = buildSet {
        addAll(setOf(Routes.HOME, Routes.AGENDA, Routes.CHAT, Routes.GROUPS, Routes.MENU))
        if (isAdmin) add(Routes.ADMIN)
    }
    val navItems = buildList {
        add(BottomNavItem(Routes.HOME, "Accueil", Icons.Filled.Home))
        add(BottomNavItem(Routes.AGENDA, "Agenda", Icons.Filled.CalendarMonth))
        add(BottomNavItem(Routes.CHAT, "Chat", Icons.Filled.Forum))
        add(BottomNavItem(Routes.GROUPS, "Collab", Icons.Filled.Groups))
        add(BottomNavItem(Routes.MENU, "Restaurant", Icons.Filled.Restaurant))
        if (isAdmin) add(BottomNavItem(Routes.ADMIN, "Admin", Icons.Filled.AdminPanelSettings))
    }

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
                        isAdmin = isAdmin,
                        postVm = postVM,
                        onCreatePostClick = { navController.navigate(Routes.CREATE_POST) },
                        onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                        onProfileClick = { navController.navigate(Routes.PROFILE) }
                    )
                }
                composable(Routes.AGENDA) {
                    AgendaScreen(
                        onCameraClick = { showCamera = true },
                        onPasteClick = { showPasteDialog = true },
                        isProf = isProf,
                        isAdmin = isAdmin,
                        userId = userId,
                        vm = agendaVM
                    )
                }
                composable(Routes.CHAT) {
                    ChatScreen(
                        onConversationClick = { chatItem ->
                            if (chatItem.isCourse) {
                                navController.navigate(Routes.courseHub(chatItem.id))
                            } else {
                                navController.navigate(Routes.conversation(chatItem.id))
                            }
                        },
                        onCourseHubClick = { navController.navigate(Routes.courseHub(6)) },
                        onNewChatClick = { showNewChatDialog = true },
                        isProf = isProf,
                        profName = username,
                        vm = chatVM
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
                composable(Routes.ADMIN) {
                    AdminScreen(currentUserId = userId, vm = adminVM)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onBack = { navController.popBackStack() },
                        onLogout = onLogout
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        userId = userId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.CREATE_POST) {
                    CreatePostScreen(
                        isProf = isProf,
                        userUECodes = postUserUECodes,
                        userBranch = postUserBranch,
                        onPostCreated = { text, uri, ue, branch ->
                            postVM.createPost(text, uri?.toString(), userId, ue, branch)
                            navController.popBackStack()
                        },
                        onVoicePostCreated = { filePath, duration, ue, branch ->
                            postVM.createVoicePost(filePath, duration, userId, ue, branch)
                            navController.popBackStack()
                        },
                        onPollCreated = { question, options, ue, branch ->
                            postVM.createPostWithPollOptions(question, userId, options, ue, branch)
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
                        currentUserId = userId,
                        onBack = { navController.popBackStack() },
                        vm = chatVM
                    )
                }
                composable(
                    route = Routes.COURSE_HUB,
                    arguments = listOf(navArgument("chatItemId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val chatItemId = backStackEntry.arguments?.getInt("chatItemId") ?: return@composable
                    CourseScreen(
                        chatItemId = chatItemId,
                        isProf = isProf,
                        currentUserId = userId,
                        onBack = { navController.popBackStack() },
                        vm = chatVM
                    )
                }
            }
        }

        if (showNewChatDialog) {
            val allUsers by chatVM.allUsers.collectAsState()
            NewChatDialog(
                allUsers = allUsers,
                currentUserId = userId,
                onDismiss = { showNewChatDialog = false },
                onStartDirectChat = { user ->
                    showNewChatDialog = false
                    chatVM.createDirectConversation(user.name, listOf(userId, user.id)) { id ->
                        navController.navigate(Routes.conversation(id))
                    }
                },
                onCreateGroup = { users ->
                    showNewChatDialog = false
                    chatVM.createGroupConversation(
                        users.map { it.name },
                        users.map { it.id } + userId
                    ) { id ->
                        navController.navigate(Routes.conversation(id))
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
