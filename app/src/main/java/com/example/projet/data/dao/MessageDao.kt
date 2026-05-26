package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM message WHERE chatItemId = :chatItemId ORDER BY id ASC")
    fun getByChatItem(chatItemId: Int): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM message WHERE chatItemId = :chatItemId")
    suspend fun deleteByChatItem(chatItemId: Int)
}
