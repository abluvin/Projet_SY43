package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.ChatItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatItemDao {

    @Query("SELECT * FROM chat_item ORDER BY time DESC")
    fun getAll(): Flow<List<ChatItem>>

    @Query("SELECT * FROM chat_item WHERE id = :id")
    fun getById(id: Int): Flow<ChatItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chatItem: ChatItem): Long

    @Update
    suspend fun update(chatItem: ChatItem)

    @Delete
    suspend fun delete(chatItem: ChatItem)
}
