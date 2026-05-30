package com.example.projet.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.projet.data.*

@Composable
fun NewChatDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val recipients = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val utbmBlue = Color(0xFF0055A4)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (recipients.size >= 2) "Nouveau Groupe" else "Nouvelle Discussion",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = utbmBlue
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Destinataire(s)") },
                    placeholder = { Text("Ex: Lucas, Sarah") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text("Séparez les noms par une virgule pour un groupe.")
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(recipients) },
                        enabled = recipients.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
                    ) {
                        Text(if (recipients.size >= 2) "Créer Groupe" else "Démarrer")
                    }
                }
            }
        }
    }
}
