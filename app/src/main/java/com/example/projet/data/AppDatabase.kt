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
    version = 7,
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
                database.execSQL("ALTER TABLE user ADD COLUMN isAdmin INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate user table: drop isProf (never persisted), add role
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_new (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `password` TEXT NOT NULL,
                        `isAdmin` INTEGER NOT NULL DEFAULT 0,
                        `role` TEXT NOT NULL DEFAULT 'STUDENT'
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO user_new (id, name, email, password, isAdmin, role)
                    SELECT id, name, email, password, isAdmin, 'STUDENT' FROM user
                """.trimIndent())
                database.execSQL("DROP TABLE user")
                database.execSQL("ALTER TABLE user_new RENAME TO user")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE post ADD COLUMN ue TEXT")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE message ADD COLUMN isAnnouncement INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `post` ADD COLUMN `voiceFilePath` TEXT")
                database.execSQL("ALTER TABLE `post` ADD COLUMN `voiceDuration` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE `post` ADD COLUMN `isPoll` INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `voice_message` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `postId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `filePath` TEXT NOT NULL, `duration` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`postId`) REFERENCES `post`(`id`) ON DELETE CASCADE, FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON DELETE CASCADE)""")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_voice_message_postId` ON `voice_message` (`postId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_voice_message_userId` ON `voice_message` (`userId`)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `poll` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `postId` INTEGER NOT NULL, `creatorId` INTEGER NOT NULL, `question` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`postId`) REFERENCES `post`(`id`) ON DELETE CASCADE, FOREIGN KEY(`creatorId`) REFERENCES `user`(`id`) ON DELETE CASCADE)""")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_poll_postId` ON `poll` (`postId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_poll_creatorId` ON `poll` (`creatorId`)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `poll_option` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `pollId` INTEGER NOT NULL, `text` TEXT NOT NULL, `voteCount` INTEGER NOT NULL, FOREIGN KEY(`pollId`) REFERENCES `poll`(`id`) ON DELETE CASCADE)""")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_poll_option_pollId` ON `poll_option` (`pollId`)")
                database.execSQL("""CREATE TABLE IF NOT EXISTS `poll_vote` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `optionId` INTEGER NOT NULL, `userId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`optionId`) REFERENCES `poll_option`(`id`) ON DELETE CASCADE, FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON DELETE CASCADE)""")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_poll_vote_optionId` ON `poll_vote` (`optionId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_poll_vote_userId` ON `poll_vote` (`userId`)")
            }
        }

        private val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val profPassword = PasswordUtils.hash("prof123")
                val adminPassword = PasswordUtils.hash("admin123")
                db.execSQL(
                    "INSERT INTO user (name, email, password, isAdmin, role) VALUES " +
                    "('Professeur Demo', 'prof.demo@utbm.fr', '$profPassword', 0, 'PROFESSOR')," +
                    "('Admin Demo', 'admin.demo@utbm.fr', '$adminPassword', 1, 'STUDENT')"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .addCallback(SEED_CALLBACK)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
