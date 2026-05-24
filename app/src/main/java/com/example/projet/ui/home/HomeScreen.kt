package com.example.projet.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    username: String = "",
    onCreatePostClick: () -> Unit = {}
) {
    val utbmDarkColor = Color(0xFF001B3C)
    val addIconColor = Color(0xFF5992E4)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Icon(
                        painter = painterResource(id = R.drawable.myutbmlogo),
                        contentDescription = "UTBM Logo",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Action Settings */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = utbmDarkColor
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Action Recherche */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = utbmDarkColor
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            IconButton(
                onClick = onCreatePostClick,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AddCircle,
                    contentDescription = "Ajouter un post",
                    modifier = Modifier.size(56.dp),
                    tint = addIconColor
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (username.isNotBlank()) "Bonjour, $username 👋" else "Bienvenue! 👋",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Voici le fil d'actualités du jour",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            PostBloc(
                name = "Aujourd'hui",
                description = "Actualités du jour",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Consultez votre agenda pour voir vos cours et importer votre planning",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PostBloc(name: String, description: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var comments by remember { mutableStateOf(listOf("Super poste !", "Merci pour l'info")) }
    var newCommentText by remember { mutableStateOf("") }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF003061), Color(0xFF004689))
    )

    Surface(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = gradientBrush),
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = description,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                ElevatedButton(
                    onClick = { 
                        expanded = !expanded 
                        if (!expanded) showComments = false
                    }
                ) {
                    Text(if (expanded) "Réduire" else "Réagir")
                }
            }
            if (expanded) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { /* Action Like */ }) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Like", tint = Color.White)
                    }
                    IconButton(onClick = { showComments = !showComments }) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Comment", tint = Color.White)
                    }
                    IconButton(onClick = { /* Action Share */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = Color.White)
                    }
                }

                if (showComments) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        comments.forEach { comment ->
                            Text(
                                text = "• $comment",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Ajouter un commentaire...", color = Color.White.copy(alpha = 0.6f)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    cursorColor = Color.White,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                            IconButton(onClick = {
                                if (newCommentText.isNotBlank()) {
                                    comments = comments + newCommentText
                                    newCommentText = ""
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
