package com.example.projet

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testRegistrationFlow() {
        // 1. Navigation de Connexion vers Register
        // On clique sur le lien "S'inscrire" identifié par le tag "go_to_register"
        composeTestRule.onNodeWithTag("go_to_register").performClick()

        // 2. Remplir le formulaire d'inscription
        // Utilisation d'un nom et d'un email unique pour éviter les collisions en base de données
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        val name = "Test User $uniqueId"
        val email = "test.$uniqueId@utbm.fr"
        val password = "Password123!"

        composeTestRule.onNodeWithTag("register_name").performTextInput(name)
        composeTestRule.onNodeWithTag("register_email").performTextInput(email)
        composeTestRule.onNodeWithTag("register_password").performTextInput(password)

        // 3. Soumettre le formulaire
        composeTestRule.onNodeWithTag("register_submit").performClick()

        // 4. Vérifier la transition vers l'écran de bienvenue
        // On attend que le texte "Bienvenue," apparaisse (timeout de 10s pour laisser le temps à la DB)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Bienvenue,", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Vérifier que le nom de l'utilisateur est affiché
        composeTestRule.onNodeWithText(name).assertExists()
        
        // Cliquer sur "Commencer" pour terminer le flux
        composeTestRule.onNodeWithText("Commencer").performClick()
        
        // Vérifier qu'on est arrivé sur l'écran principal (Accueil)
        composeTestRule.onNodeWithText("Accueil").assertExists()
    }
}
