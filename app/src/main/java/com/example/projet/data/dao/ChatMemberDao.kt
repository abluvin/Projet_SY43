package com.example.projet.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projet.data.ChatMember

@Dao
interface ChatMemberDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(member: ChatMember): Long

    @Query("SELECT userId FROM chat_member WHERE chatItemId = :chatItemId")
    suspend fun getUserIds(chatItemId: Int): List<Int>
}
