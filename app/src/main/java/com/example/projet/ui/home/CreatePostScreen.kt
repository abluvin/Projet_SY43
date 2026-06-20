package com.example.projet.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.projet.ui.utils.AudioRecorderManager

private enum class PostType { TEXT, VOICE, POLL }

private sealed class PostDestination {
    object General : PostDestination()
    object Branch : PostDestination()
    data class UE(val code: String) : PostDestination()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    isProf: Boolean = false,
    userUECodes: List<String> = emptyList(),
    userBranch: String = "",
    onPostCreated: (String, Uri?, String?, String?) -> Unit,
    onVoicePostCreated: ((String, Long, String?, String?) -> Unit)? = null,
    onPollCreated: ((String, List<String>, String?, String?) -> Unit)? = null,
    onBack: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    val context = LocalContext.current
    val audioManager = remember { AudioRecorderManager(context) }

    var postType by remember { mutableStateOf(PostType.TEXT) }
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var destination by remember { mutableStateOf<PostDestination>(PostDestination.General) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var recordedFilePath by remember { mutableStateOf<String?>(null) }
    var recordingError by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }

    val (destUE, destBranch) = when (val d = destination) {
        is PostDestination.General -> null to null
        is PostDestination.Branch -> null to userBranch.ifBlank { null }
        is PostDestination.UE -> d.code to null
    }

    fun tryStartRecording() {
        recordingError = false
        if (audioManager.startRecording()) {
            isRecording = true; recordingTime = 0L; recordedFilePath = null
        } else {
            recordingError = true
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) tryStartRecording() else recordingError = true
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> selectedImageUri = uri }

    DisposableEffect(Unit) { onDispose { audioManager.cleanup() } }

    LaunchedEffect(isRecording) {
        if (isRecording) while (isRecording) { kotlinx.coroutines.delay(1000L); recordingTime += 1000 }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un post") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Publier dans",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = destination is PostDestination.General,
                    onClick = { destination = PostDestination.General },
                    label = { Text("Général") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = utbmBlue,
                        selectedLabelColor = Color.White
                    )
                )
                if (userBranch.isNotBlank()) {
                    FilterChip(
                        selected = destination is PostDestination.Branch,
                        onClick = { destination = PostDestination.Branch },
                        label = { Text(userBranch) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF5E35B1),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                userUECodes.forEach { code ->
                    FilterChip(
                        selected = destination is PostDestination.UE && (destination as PostDestination.UE).code == code,
                        onClick = { destination = PostDestination.UE(code) },
                        label = { Text(code) },
                        leadingIcon = { Icon(Icons.Filled.School, null, Modifier.size(14.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF34A853),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PostType.entries.forEach { type ->
                    ElevatedButton(
                        onClick = { postType = type; isRecording = false; recordingTime = 0L },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (postType == type) utbmBlue else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            when (type) { PostType.TEXT -> "Texte"; PostType.VOICE -> "Vocal"; PostType.POLL -> "Sondage" },
                            color = if (postType == type) Color.White else Color.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            when (postType) {
                PostType.TEXT -> {
                    OutlinedTextField(
                        value = postText,
                        onValueChange = { postText = it },
                        label = { Text("Quoi de neuf ?") },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        placeholder = { Text("Écrivez votre post ici...") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedImageUri != null) {
                        Image(painter = rememberAsyncImagePainter(selectedImageUri), contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
                        TextButton(onClick = { selectedImageUri = null }) { Text("Supprimer l'image", color = Color.Red) }
                    } else {
                        Button(onClick = { imageLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) { Text("Insérer une image") }
                    }
                }
                PostType.VOICE -> {
                    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(utbmBlue.copy(alpha = 0.08f)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enregistrer un message vocal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 24.dp))
                        when {
                            !isRecording && recordedFilePath == null -> {
                                Text("Appuyez pour enregistrer", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
                                ElevatedButton(onClick = {
                                    recordingError = false
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        tryStartRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }, modifier = Modifier.size(100.dp), shape = RoundedCornerShape(50)) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(50.dp))
                                }
                                if (recordingError) {
                                    Spacer(Modifier.height(12.dp))
                                    Text("Impossible d'accéder au microphone.", color = Color.Red, fontSize = 13.sp)
                                }
                            }
                            isRecording -> {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.padding(start = 12.dp))
                                    Text("${recordingTime / 1000}s", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(16.dp))
                                ElevatedButton(onClick = { audioManager.stopRecording(); recordedFilePath = audioManager.getRecordingFilePath(); isRecording = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.Red.copy(alpha = 0.8f))) {
                                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(20.dp)); Spacer(Modifier.padding(start = 8.dp)); Text("Arrêter")
                                }
                            }
                            else -> {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Enregistrement prêt — ${recordingTime / 1000}s", fontSize = 14.sp)
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = { recordedFilePath = null; recordingTime = 0L }) { Text("Recommencer", color = Color.Red) }
                            }
                        }
                    }
                }
                PostType.POLL -> {
                    OutlinedTextField(value = pollQuestion, onValueChange = { pollQuestion = it }, label = { Text("Question du sondage") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Posez votre question...") })
                    Spacer(Modifier.height(16.dp))
                    Text("Options", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    pollOptions.forEachIndexed { index, option ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedTextField(value = option, onValueChange = { newVal -> pollOptions = pollOptions.toMutableList().also { it[index] = newVal } }, modifier = Modifier.weight(1f), placeholder = { Text("Option ${index + 1}...") }, label = { Text("Option ${index + 1}") })
                            if (pollOptions.size > 2) { IconButton(onClick = { pollOptions = pollOptions.filterIndexed { i, _ -> i != index } }) { Text("✕", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold) } }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    ElevatedButton(onClick = { pollOptions = pollOptions + "" }, modifier = Modifier.fillMaxWidth()) { Text("+ Option") }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    when (postType) {
                        PostType.TEXT -> onPostCreated(postText, selectedImageUri, destUE, destBranch)
                        PostType.VOICE -> recordedFilePath?.let { onVoicePostCreated?.invoke(it, recordingTime, destUE, destBranch) }
                        PostType.POLL -> {
                            if (pollQuestion.isNotBlank() && pollOptions.count { it.isNotBlank() } >= 2)
                                onPollCreated?.invoke(pollQuestion, pollOptions.filter { it.isNotBlank() }, destUE, destBranch)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = when (postType) {
                    PostType.TEXT -> postText.isNotBlank() || selectedImageUri != null
                    PostType.VOICE -> recordedFilePath != null && !isRecording
                    PostType.POLL -> pollQuestion.isNotBlank() && pollOptions.count { it.isNotBlank() } >= 2
                },
                colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
            ) {
                Text("Publier", fontWeight = FontWeight.Bold)
            }
        }
    }
}
