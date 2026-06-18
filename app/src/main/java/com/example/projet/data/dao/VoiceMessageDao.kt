package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.VoiceMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceMessageDao {
    @Query("SELECT * FROM voice_message WHERE postId = :postId ORDER BY timestamp DESC")
    fun getByPost(postId: Int): Flow<List<VoiceMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voiceMessage: VoiceMessage): Long

    @Delete
    suspend fun delete(voiceMessage: VoiceMessage)
}
