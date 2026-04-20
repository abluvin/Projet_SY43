package com.example.projet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.projet.ui.agenda.AgendaScreen
import com.example.projet.ui.camera.CameraScreen
import com.example.projet.ui.theme.ProjetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetTheme {
                var showCamera by remember { mutableStateOf(false) }
                var recognizedText by remember { mutableStateOf("") }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        if (!showCamera) {
                            FloatingActionButton(onClick = { showCamera = true }) {
                                Text("Scan")
                            }
                        }
                    }
                ) { innerPadding ->
                    if (showCamera) {
                        CameraScreen(
                            onTextRecognized = { text ->
                                recognizedText = text
                                showCamera = false
                                // TODO: Parse text and add events
                            },
                            onBack = { showCamera = false }
                        )
                    } else {
                        AgendaScreen(modifier = Modifier.padding(innerPadding))
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
