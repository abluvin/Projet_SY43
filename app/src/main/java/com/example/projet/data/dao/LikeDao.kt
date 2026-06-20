package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Like
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {
    @Query("SELECT COUNT(*) FROM post_like WHERE postId = :postId")
    fun getLikeCount(postId: Int): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM post_like WHERE postId = :postId AND userId = :userId)")
    fun isLikedByUser(postId: Int, userId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(like: Like)

    @Query("DELETE FROM post_like WHERE postId = :postId AND userId = :userId")
    suspend fun delete(postId: Int, userId: Int)
}
