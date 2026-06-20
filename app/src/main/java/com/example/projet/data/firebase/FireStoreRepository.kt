package com.example.projet.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FireStoreRepository {
        private val db = FirebaseFirestore.getInstance()

        suspend fun <T> add(collection: String, id: String, data: T) {
            db.collection(collection)
                .document(id)
                .set(data!!)
                .await()
        }

        suspend fun <T> getById(collection: String, id: String, clazz: Class<T>): T? {
            val snapshot = db.collection(collection)
                .document(id)
                .get()
                .await()

            return snapshot.toObject(clazz)
        }

        suspend fun <T> getAll(collection: String, clazz: Class<T>): List<T> {
            val snapshot = db.collection(collection)
                .get()
                .await()

            return snapshot.toObjects(clazz)
        }

        suspend fun delete(collection: String, id: String) {
            db.collection(collection)
                .document(id)
                .delete()
                .await()
        }

}