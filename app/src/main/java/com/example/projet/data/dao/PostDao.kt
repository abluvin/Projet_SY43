package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Query("SELECT * FROM post ORDER BY timestamp DESC")
    fun getAll(): Flow<List<Post>>

    @Query("SELECT * FROM post WHERE idUser = :userId ORDER BY timestamp DESC")
    fun getByUser(userId: Int): Flow<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post): Long

    @Delete
    suspend fun delete(post: Post)
}
