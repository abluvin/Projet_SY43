package com.example.projet.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CourseMaterial(
    val title: String,
    val description: String,
    val date: String,
    val type: String = "PDF"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    courseName: String,
    onBack: () -> Unit
) {
    val utbmBlue = Color(0xFF0055A4)
    
    val materials = remember {
        listOf(
            CourseMaterial("Support de Cours - Chapitre 1", "Introduction aux systèmes mobiles", "10 Oct"),
            CourseMaterial("TD 1 - Layouts", "Exercices sur Jetpack Compose", "12 Oct"),
            CourseMaterial("Support de Cours - Chapitre 2", "Gestion de l'état et Navigation", "17 Oct"),
            CourseMaterial("TP 1 - CameraX", "Sujet du premier TP noté", "Hier"),
            CourseMaterial("Annales 2023", "Examen final de l'année dernière", "Aujourd'hui")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(courseName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Professeur : Uniquement lecture", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Info */ }) {
                        Icon(Icons.Default.Info, contentDescription = "Informations", tint = utbmBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Seul le professeur peut envoyer des documents",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                items(materials) { material ->
                    MaterialCard(material)
                }
            }
            
            // Bottom Bar indicating read-only
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Mode lecture seule activé pour ce canal",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialCard(material: CourseMaterial) {
    val utbmBlue = Color(0xFF0055A4)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = utbmBlue,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = material.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = material.description,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = material.date,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
