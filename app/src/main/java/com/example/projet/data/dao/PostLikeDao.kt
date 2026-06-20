package com.example.projet.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projet.data.PostLike
import kotlinx.coroutines.flow.Flow

@Dao
interface PostLikeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(like: PostLike): Long

    @Delete
    suspend fun delete(like: PostLike)

    @Query("SELECT COUNT(*) FROM post_like WHERE postId = :postId")
    fun getLikeCount(postId: Int): Flow<Int>

    @Query("SELECT * FROM post_like WHERE postId = :postId AND userId = :userId LIMIT 1")
    suspend fun getUserLike(postId: Int, userId: Int): PostLike?

    @Query("SELECT COUNT(*) > 0 FROM post_like WHERE postId = :postId AND userId = :userId")
    fun hasUserLiked(postId: Int, userId: Int): Flow<Boolean>
}
