package com.example.projet.data.firebase

import com.example.projet.data.User

class UserFireStoreRepository (
        private val firestore: FireStoreRepository
    ) {

        private val collection = "users"

        suspend fun createUser(user: User) {
            firestore.add(collection, user.id.toString(), user)
        }

        suspend fun getUser(id: String): User? {
            return firestore.getById(collection, id, User::class.java)
        }

        suspend fun getAllUsers(): List<User> {
            return firestore.getAll(collection, User::class.java)
        }

        suspend fun deleteUser(id: String) {
            firestore.delete(collection, id)
        }

}