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

    @Query("UPDATE chat_item SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("SELECT * FROM chat_item WHERE name = :name AND isGroup = 0 LIMIT 1")
    suspend fun getByName(name: String): ChatItem?

    @Query("UPDATE chat_item SET lastMessage = :lastMessage, time = :time WHERE id = :id")
    suspend fun updateLastMessage(id: Int, lastMessage: String, time: String)
}
