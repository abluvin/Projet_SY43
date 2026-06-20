package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE chatItemId = :chatItemId ORDER BY id ASC")
    fun getByChatItem(chatItemId: Int): Flow<List<Message>>

    @Query("SELECT text FROM message WHERE chatItemId = :chatItemId ORDER BY id DESC LIMIT 1")
    fun getLastMessage(chatItemId: Int): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM message WHERE chatItemId = :chatItemId")
    suspend fun deleteByChatItem(chatItemId: Int)
}
