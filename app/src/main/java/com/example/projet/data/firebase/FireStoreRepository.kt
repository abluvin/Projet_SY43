package com.example.projet.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FireStoreRepository {
        private val db = FirebaseFirestore.getInstance()

        suspend fun <T : Any> add(path: String, id: String, data: T) {
            db.document("$path/$id")
                .set(data)
                .await()
        }

        suspend fun <T> getById(path: String, id: String, clazz: Class<T>): T? {
            val snapshot = db.document("$path/$id")
                .get()
                .await()

            return snapshot.toObject(clazz)
        }

        suspend fun <T> getAll(path: String, clazz: Class<T>): List<T> {
            val snapshot = db.collection(path)
                .get()
                .await()

            return snapshot.toObjects(clazz)
        }

        suspend fun delete(path: String, id: String) {
            db.document("$path/$id")
                .delete()
                .await()
        }

}