package com.example.projet.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip

enum class PostType {
    TEXT, VOICE, POLL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onPostCreated: (String, Uri?, String?, Long, Boolean, List<String>) -> Unit,
    onBack: () -> Unit
) {
    var postType by remember { mutableStateOf(PostType.TEXT) }
    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0L) }
    var pollQuestion by remember { mutableStateOf("") }
    var pollOptions by remember { mutableStateOf(listOf("", "")) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Timer pour l'enregistrement audio
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                kotlinx.coroutines.delay(1000L)
                recordingTime += 1000
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Créer un post") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Onglets
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostType.values().forEach { type ->
                    ElevatedButton(
                        onClick = {
                            postType = type
                            isRecording = false
                            recordingTime = 0L
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = if (postType == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            when (type) {
                                PostType.TEXT -> "Texte"
                                PostType.VOICE -> "Vocal"
                                PostType.POLL -> "Sondage"
                            },
                            color = if (postType == type) Color.White else Color.Black
                        )
                    }
                }
            }

            // Contenu selon le type de post
            when (postType) {
                PostType.TEXT -> TextPostContent(postText, { postText = it }, selectedImageUri, { selectedImageUri = it }, launcher)
                PostType.VOICE -> VoicePostContent(isRecording, { isRecording = it }, recordingTime, { recordingTime = it })
                PostType.POLL -> PollPostContent(pollQuestion, { pollQuestion = it }, pollOptions, { pollOptions = it })
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bouton Publier
            ElevatedButton(
                onClick = {
                    when (postType) {
                        PostType.TEXT -> {
                            if (postText.isNotBlank() || selectedImageUri != null) {
                                onPostCreated(postText, selectedImageUri, null, 0, false, emptyList())
                            }
                        }
                        PostType.VOICE -> {
                            if (recordingTime > 0) {
                                onPostCreated("Message vocal", null, "voice_post_${System.currentTimeMillis()}.3gp", recordingTime, false, emptyList())
                            }
                        }
                        PostType.POLL -> {
                            if (pollQuestion.isNotBlank() && pollOptions.count { it.isNotBlank() } >= 2) {
                                onPostCreated(pollQuestion, null, null, 0, true, pollOptions.filter { it.isNotBlank() })
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = when (postType) {
                    PostType.TEXT -> postText.isNotBlank() || selectedImageUri != null
                    PostType.VOICE -> recordingTime > 0
                    PostType.POLL -> pollQuestion.isNotBlank() && pollOptions.count { it.isNotBlank() } >= 2
                }
            ) {
                Text("Publier", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TextPostContent(
    postText: String,
    onTextChange: (String) -> Unit,
    selectedImageUri: Uri?,
    onImageChange: (Uri?) -> Unit,
    launcher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Column {
        OutlinedTextField(
            value = postText,
            onValueChange = onTextChange,
            label = { Text("Quoi de neuf ?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("Écrivez votre post ici...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Image sélectionnée",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { onImageChange(null) }) {
                Text("Supprimer l'image", color = Color.Red)
            }
        } else {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Insérer une image")
            }
        }
    }
}

@Composable
private fun VoicePostContent(
    isRecording: Boolean,
    onRecordingChange: (Boolean) -> Unit,
    recordingTime: Long,
    onTimeChange: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF003061).copy(alpha = 0.1f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enregistrer votre message vocal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isRecording) {
            Text(
                text = "Appuyez pour enregistrer",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ElevatedButton(
                onClick = { onRecordingChange(true); onTimeChange(0L) },
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Démarrer", modifier = Modifier.size(50.dp))
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = "Enregistrement",
                    tint = Color.Red,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.padding(start = 12.dp))
                Text(
                    text = "Durée: ${recordingTime / 1000}s",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(
                onClick = { onRecordingChange(false) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color.Red.copy(alpha = 0.8f)
                )
            ) {
                Icon(Icons.Filled.Stop, contentDescription = "Arrêter", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text("Arrêter l'enregistrement")
            }
        }
    }
}

@Composable
private fun PollPostContent(
    pollQuestion: String,
    onQuestionChange: (String) -> Unit,
    pollOptions: List<String>,
    onOptionsChange: (List<String>) -> Unit
) {
    Column {
        OutlinedTextField(
            value = pollQuestion,
            onValueChange = onQuestionChange,
            label = { Text("Question du sondage") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Posez votre question...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Options",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        pollOptions.forEachIndexed { index, option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = option,
                    onValueChange = { newValue ->
                        onOptionsChange(pollOptions.toMutableList().also { it[index] = newValue })
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Option ${index + 1}...") },
                    label = { Text("Option ${index + 1}") }
                )
                if (pollOptions.size > 2) {
                    IconButton(onClick = { onOptionsChange(pollOptions.filterIndexed { i, _ -> i != index }) }) {
                        Text("✕", color = Color.Red, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ElevatedButton(
                onClick = { onOptionsChange(pollOptions + "") },
                modifier = Modifier.weight(1f)
            ) {
                Text("+ Option")
            }
        }
    }
}
