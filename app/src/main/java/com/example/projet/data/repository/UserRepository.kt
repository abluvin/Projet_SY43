package com.example.projet.data.repository

import com.example.projet.data.User
import com.example.projet.data.dao.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {

    fun getAll(): Flow<List<User>> = dao.getAll()

    suspend fun getById(id: Int): User? = dao.getById(id)

    suspend fun getByEmail(email: String): User? = dao.getByEmail(email)

    suspend fun login(email: String, password: String): User? = dao.login(email, password)

    suspend fun insert(user: User): Long = dao.insert(user)

    suspend fun update(user: User) = dao.update(user)

    suspend fun delete(user: User) = dao.delete(user)
}
