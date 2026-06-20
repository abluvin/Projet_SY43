package com.example.projet

import org.junit.Test
import org.junit.Assert.*

class RegistrationLogicTest {

    @Test
    fun testEmailValidation() {
        // Le regex utilisé dans UserViewModel pour valider l'email UTBM
        val emailRegex = Regex("^[^@]+@utbm\\.fr$")
        
        assertTrue("test@utbm.fr".matches(emailRegex))
        assertTrue("prenom.nom@utbm.fr".matches(emailRegex))
        
        assertFalse("test@gmail.com".matches(emailRegex))
        assertFalse("test@utbm.com".matches(emailRegex))
        assertFalse("testutbm.fr".matches(emailRegex))
    }
}
