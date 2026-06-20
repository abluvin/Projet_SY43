package com.example.projet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projet.data.ChatMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(member: ChatMember): Long

    @Query("SELECT userId FROM chat_member WHERE chatItemId = :chatItemId")
    suspend fun getUserIds(chatItemId: Int): List<Int>

    @Query("""
        SELECT u.name FROM user u
        INNER JOIN chat_member cm ON u.id = cm.userId
        WHERE cm.chatItemId = :chatItemId AND cm.userId != :currentUserId
        LIMIT 1
    """)
    fun getOtherMemberName(chatItemId: Int, currentUserId: Int): Flow<String?>
}
