package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Comment
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Query("SELECT * FROM comment WHERE postId = :postId ORDER BY timestamp ASC")
    fun getByPost(postId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment): Long

    @Delete
    suspend fun delete(comment: Comment)

    @Query("DELETE FROM comment WHERE postId = :postId")
    suspend fun deleteByPost(postId: Int)
}
