package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user ORDER BY name ASC")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<User>>

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
