package com.example.projet.data.dao

import androidx.room.*
import com.example.projet.data.CourseMaterial
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseMaterialDao {

    @Query("SELECT * FROM course_material ORDER BY date DESC")
    fun getAll(): Flow<List<CourseMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(material: CourseMaterial): Long

    @Delete
    suspend fun delete(material: CourseMaterial)
}
