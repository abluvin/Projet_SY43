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
        // Attendre que le Splash Screen disparaisse (timeout 5s)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag("go_to_register").fetchSemanticsNodes().isNotEmpty()
        }

        // 1. Navigation de Connexion vers Register
        // On scroll vers l'élément au cas où il serait hors écran, puis on clique
        composeTestRule.onNodeWithTag("go_to_register")
            .performScrollTo()
            .performClick()

        // 2. Remplir le formulaire d'inscription
        // Utilisation d'un nom et d'un email unique pour éviter les collisions en base de données
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        val name = "Test User $uniqueId"
        val email = "test.$uniqueId@utbm.fr"
        val password = "Password123!"

        composeTestRule.onNodeWithTag("register_name").performScrollTo().performTextInput(name)
        composeTestRule.onNodeWithTag("register_email").performScrollTo().performTextInput(email)
        composeTestRule.onNodeWithTag("register_password").performScrollTo().performTextInput(password)

        // 3. Soumettre le formulaire
        composeTestRule.onNodeWithTag("register_submit").performScrollTo().performClick()

        // 4. Vérifier la transition vers l'écran de bienvenue
        // On attend que le texte "Bienvenue," apparaisse (timeout de 10s pour laisser le temps à la DB)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithText("Bienvenue,", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        // Vérifier que le nom de l'utilisateur est affiché
        composeTestRule.onNodeWithText(name).assertExists()
        
        // Cliquer sur "Commencer" pour terminer le flux
        composeTestRule.onNodeWithText("Commencer").performScrollTo().performClick()
        
        // Vérifier qu'on est arrivé sur l'écran principal (Accueil)
        // On utilise useUnmergedTree car le label peut être dans un nœud fils
        composeTestRule.onNodeWithText("Accueil", substring = true).assertExists()
    }
}
