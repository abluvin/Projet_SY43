package com.example.projet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.projet.data.dao.*

@Database(
    entities = [
        Event::class,
        ChatItem::class,
        Message::class,
        CourseMaterial::class,
        User::class,
        Post::class,
        Comment::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun chatItemDao(): ChatItemDao
    abstract fun messageDao(): MessageDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun courseMaterialDao(): CourseMaterialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
