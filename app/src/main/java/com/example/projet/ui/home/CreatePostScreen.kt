package com.example.projet.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    isProf: Boolean = false,
    onPostCreated: (String, Uri?, String?) -> Unit,
    onBack: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    val profColor = Color(0xFF34A853)

    var postText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var ueText by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // UE field — visible only for professors
            if (isProf) {
                Surface(
                    color = profColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.School, contentDescription = null, tint = profColor, modifier = Modifier.size(18.dp))
                            Text("Post lié à une UE (optionnel)", color = profColor, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ueText,
                            onValueChange = { ueText = it.uppercase() },
                            label = { Text("Code UE (ex: SY43, MT22)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
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
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { selectedImageUri = null }) {
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

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onPostCreated(postText, selectedImageUri, ueText.ifBlank { null }) },
                modifier = Modifier.fillMaxWidth(),
                enabled = postText.isNotBlank() || selectedImageUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
            ) {
                Text("Publier", fontWeight = FontWeight.Bold)
            }
        }
    }
}
