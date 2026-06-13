package com.example.projet.ui.Register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet.ui.components.UtbmLogo

@Composable
fun Register(
    modifier: Modifier = Modifier,
    vm: UserViewModel = viewModel(),
    onRegistered: (String, Int) -> Unit = { _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val utbmBlue = Color(0xFF0055A4)
    val backgroundColor = Color(0xFFF8F9FA)

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var adminCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val emailError = email.isNotBlank() && !email.matches(Regex("^[^@]+@utbm\\.fr$"))

    val state by vm.registerState.collectAsState()
    LaunchedEffect(state) {
        when (val s = state) {
            is UserViewModel.RegisterState.Success -> onRegistered(s.name, s.id)
            is UserViewModel.RegisterState.EmailExists -> errorMessage = "Cet email est déjà utilisé."
            is UserViewModel.RegisterState.InvalidEmail -> errorMessage = "L'email doit être au format prénom.nom@utbm.fr"
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        UtbmLogo(iconSize = 56.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Créer votre compte",
            color = Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nom") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = utbmBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = { Text("Email (@utbm.fr)") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = utbmBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = emailError,
                    supportingText = if (emailError) {
                        { Text("Format attendu : prénom.nom@utbm.fr", color = Color.Red, fontSize = 12.sp) }
                    } else null
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Mot de passe") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = utbmBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = adminCode,
                    onValueChange = { adminCode = it },
                    label = { Text("Code d'accès (optionnel)") },
                    leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = utbmBlue) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val isProf = adminCode.contains("PROF", ignoreCase = true)
                val isAdminCode = adminCode.contains("ADMIN", ignoreCase = true)
                val roleColor = when {
                    isAdminCode -> Color(0xFFFF9500)
                    isProf -> Color(0xFF34A853)
                    else -> utbmBlue
                }
                val roleIcon = when {
                    isAdminCode -> Icons.Default.AdminPanelSettings
                    isProf -> Icons.Default.School
                    else -> Icons.Default.Person
                }
                val roleLabel = when {
                    isAdminCode && isProf -> "Professeur · Administrateur"
                    isAdminCode -> "Étudiant · Administrateur"
                    isProf -> "Professeur"
                    else -> "Étudiant"
                }
                Surface(
                    color = roleColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(roleIcon, contentDescription = null, tint = roleColor, modifier = Modifier.size(18.dp))
                        Text(roleLabel, color = roleColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = { vm.register(username, email, password, adminCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && !emailError,
            colors = ButtonDefaults.buttonColors(containerColor = utbmBlue)
        ) {
            Text(
                text = "S'inscrire",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                append("Déjà un compte ? ")
                withStyle(SpanStyle(color = utbmBlue, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append("Se connecter")
                }
            },
            fontSize = 14.sp,
            color = Gray,
            modifier = Modifier.clickable { onNavigateToLogin() }
        )
    }
}
