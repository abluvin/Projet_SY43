package com.example.projet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projet.data.dao.*

@Database(
    entities = [
        Event::class,
        ChatItem::class,
        Message::class,
        CourseMaterial::class,
        User::class,
        Post::class,
        Comment::class,
        Collaboration::class,
        VoiceMessage::class,
        Poll::class,
        PollOption::class,
        PollVote::class
    ],
    version = 4,
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
    abstract fun collaborationDao(): CollaborationDao
    abstract fun voiceMessageDao(): VoiceMessageDao
    abstract fun pollDao(): PollDao
    abstract fun pollOptionDao(): PollOptionDao
    abstract fun pollVoteDao(): PollVoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """CREATE TABLE IF NOT EXISTS `collaborations` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `ue` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `campus` TEXT NOT NULL,
                        `room` TEXT NOT NULL,
                        `creatorId` TEXT NOT NULL,
                        `creatorName` TEXT NOT NULL,
                        `participantIds` TEXT NOT NULL,
                        `participantNames` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `startTime` TEXT NOT NULL,
                        `endTime` TEXT NOT NULL,
                        `maxParticipants` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )"""
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ...existing code...
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Ajouter les colonnes pour les posts vocaux et sondages
                database.execSQL("ALTER TABLE `post` ADD COLUMN `voiceFilePath` TEXT")
                database.execSQL("ALTER TABLE `post` ADD COLUMN `voiceDuration` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `post` ADD COLUMN `isPoll` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
