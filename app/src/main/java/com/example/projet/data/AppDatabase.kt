package com.example.projet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.projet.data.ChatMember
import com.example.projet.data.PostLike
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
        PollVote::class,
        UE::class,
        UserUE::class,
        ChatPoll::class,
        ChatPollOption::class,
        ChatPollVote::class,
        ChatMember::class,
        PostLike::class
    ],
    version = 16,
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
    abstract fun ueDao(): UEDao
    abstract fun userUEDao(): UserUEDao
    abstract fun chatPollDao(): ChatPollDao
    abstract fun chatPollOptionDao(): ChatPollOptionDao
    abstract fun chatPollVoteDao(): ChatPollVoteDao
    abstract fun chatMemberDao(): ChatMemberDao
    abstract fun postLikeDao(): PostLikeDao

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

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE post ADD COLUMN branch TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE event ADD COLUMN targetUECodes TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE event ADD COLUMN targetBranches TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE event ADD COLUMN isGlobal INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE user ADD COLUMN branch TEXT NOT NULL DEFAULT ''")
                try {
                    database.execSQL("ALTER TABLE ue ADD COLUMN branch TEXT NOT NULL DEFAULT 'Transversal'")
                    database.execSQL("UPDATE ue SET branch = 'Info' WHERE code IN ('HM40','IT41','IT44','IT45','RE46','RS40','SI40','PA5A','SY43','WE4A','WE4B')")
                } catch (_: android.database.sqlite.SQLiteException) {
                }
                seedUEs(database)
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `ue` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `code` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `branch` TEXT NOT NULL DEFAULT 'Transversal'
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_ue` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `ueId` INTEGER NOT NULL,
                        `enseigne` INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`ueId`) REFERENCES `ue`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_ue_userId` ON `user_ue` (`userId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_user_ue_ueId` ON `user_ue` (`ueId`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_user_ue_userId_ueId` ON `user_ue` (`userId`, `ueId`)")
                seedUEs(database)
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `post_like` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `postId` INTEGER NOT NULL,
                        `userId` INTEGER NOT NULL,
                        FOREIGN KEY(`postId`) REFERENCES `post`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_post_like_postId` ON `post_like` (`postId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_post_like_userId` ON `post_like` (`userId`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_post_like_postId_userId` ON `post_like` (`postId`, `userId`)")
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_member` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `chatItemId` INTEGER NOT NULL,
                        `userId` INTEGER NOT NULL,
                        FOREIGN KEY(`chatItemId`) REFERENCES `chat_item`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`userId`) REFERENCES `user`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_member_chatItemId` ON `chat_member` (`chatItemId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_member_userId` ON `chat_member` (`userId`)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_chat_member_chatItemId_userId` ON `chat_member` (`chatItemId`, `userId`)")
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `message_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `chatItemId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `senderId` INTEGER NOT NULL DEFAULT 0,
                        `time` TEXT NOT NULL,
                        `isAnnouncement` INTEGER NOT NULL DEFAULT 0,
                        `messageType` TEXT NOT NULL DEFAULT 'TEXT',
                        `audioPath` TEXT,
                        `audioDuration` INTEGER NOT NULL DEFAULT 0,
                        `pollId` INTEGER,
                        FOREIGN KEY(`chatItemId`) REFERENCES `chat_item`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO message_new (id, chatItemId, text, senderId, time, isAnnouncement, messageType, audioPath, audioDuration, pollId)
                    SELECT id, chatItemId, text, 0, time, isAnnouncement, messageType, audioPath, audioDuration, pollId FROM message
                """.trimIndent())
                database.execSQL("DROP TABLE message")
                database.execSQL("ALTER TABLE message_new RENAME TO message")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_message_chatItemId` ON `message` (`chatItemId`)")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Nouveaux champs sur message
                database.execSQL("ALTER TABLE message ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'")
                database.execSQL("ALTER TABLE message ADD COLUMN audioPath TEXT")
                database.execSQL("ALTER TABLE message ADD COLUMN audioDuration INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE message ADD COLUMN pollId INTEGER")
                // Nouveau champ sur chat_item
                database.execSQL("ALTER TABLE chat_item ADD COLUMN ueCode TEXT")
                // Tables pour les sondages de chat
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_poll` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `chatItemId` INTEGER NOT NULL,
                        `question` TEXT NOT NULL,
                        `creatorId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_poll_option` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pollId` INTEGER NOT NULL,
                        `text` TEXT NOT NULL,
                        `voteCount` INTEGER NOT NULL,
                        FOREIGN KEY(`pollId`) REFERENCES `chat_poll`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_poll_option_pollId` ON `chat_poll_option` (`pollId`)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `chat_poll_vote` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `optionId` INTEGER NOT NULL,
                        `userId` INTEGER NOT NULL,
                        FOREIGN KEY(`optionId`) REFERENCES `chat_poll_option`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_poll_vote_optionId` ON `chat_poll_vote` (`optionId`)")
            }
        }

        private fun seedUEs(database: SupportSQLiteDatabase) {
            val ues = listOf(
                Triple("AM02", "Pratique et création artistique", "Transversal"),
                Triple("CC01", "Comportement culturel et relations humaines au niveau international", "Transversal"),
                Triple("CC04", "British studies", "Transversal"),
                Triple("CC05", "Conscience interculturelle : se forger un mindset global", "Transversal"),
                Triple("CI02", "Maîtriser son identité scientifique", "Transversal"),
                Triple("EC1E", "Fundamentals of economics for the engineer", "Transversal"),
                Triple("EC1F", "Fondements de l''économie pour l''ingénieur", "Transversal"),
                Triple("EE01", "Europe", "Transversal"),
                Triple("EI02", "The Rest of the English-Speaking World", "Transversal"),
                Triple("EI03", "International Security and Global Challenges", "Transversal"),
                Triple("ESP4", "Tutored project", "Transversal"),
                Triple("ESP5", "Tutored Project", "Transversal"),
                Triple("EV01", "Imaginer et concevoir les nouvelles mobilités", "Transversal"),
                Triple("GE01", "Fondements de la gestion", "Transversal"),
                Triple("GE06", "Fondements du marketing", "Transversal"),
                Triple("GE08", "Contrôle de gestion", "Transversal"),
                Triple("GE41", "Technologie et management de l''innovation", "Transversal"),
                Triple("GE50", "Mise en oeuvre des opérations internationales", "Transversal"),
                Triple("GS01", "Etudes de genre", "Transversal"),
                Triple("HE09", "Histoire des sciences et du monde scientifique", "Transversal"),
                Triple("HM40", "Interface Homme Machine", "Info"),
                Triple("HN01", "Techn''hom Time Machine : outils et méthodes", "Transversal"),
                Triple("HN2A", "Techn''hom Time Machine : projet (QC)", "Transversal"),
                Triple("HN2B", "Techn''hom Time Machine : projet (OM)", "Transversal"),
                Triple("HT01", "Initiation à l''histoire des techniques", "Transversal"),
                Triple("IT41", "Classical and Quantum Algorithms", "Info"),
                Triple("IT44", "Analyse numérique", "Info"),
                Triple("IT45", "Optimisation et recherche opérationnelle", "Info"),
                Triple("LF74", "Français pour ingénieurs niveau B2 et entraînement TCF", "Transversal"),
                Triple("MD40", "Organiser des évènements scientifiques et/ou pédagogiques autour d''enjeux sociétaux", "Transversal"),
                Triple("MG04", "Management de l''environnement", "Transversal"),
                Triple("MG13", "Management des hommes et de la production", "Transversal"),
                Triple("MG6P", "Les brevets au service de l''ingénieur - Projet", "Transversal"),
                Triple("MG6T", "Les brevets au service de l''ingénieur - Théorie", "Transversal"),
                Triple("MR01", "Méthodologie de recherche (à distance)", "Transversal"),
                Triple("MR52", "Initiation à la recherche en sciences humaines et sociales (FISE-INFO)", "Transversal"),
                Triple("PA0A", "S''engager au service d''une communauté, d''individus", "Transversal"),
                Triple("PA5A", "Transmettre et partager les connaissances scientifiques", "Info"),
                Triple("PE01", "Projet entrepreneurial", "Transversal"),
                Triple("RE46", "Réseaux locaux", "Info"),
                Triple("RS40", "Réseaux et Cybersécurité niveau 1", "Info"),
                Triple("SI02", "Sémiologie de l''image et du son", "Transversal"),
                Triple("SI40", "Systèmes d''information", "Info"),
                Triple("SO01", "Sociologie du travail", "Transversal"),
                Triple("SO03", "La transition par les sciences sociales", "Transversal"),
                Triple("SO04", "Anthropologie et sociologie des techniques", "Transversal"),
                Triple("SO07", "Sociologie des organisations", "Transversal"),
                Triple("SO09", "Santé et sécurité au travail", "Transversal"),
                Triple("SP03", "Entraînement et performances", "Transversal"),
                Triple("SP07", "Education physique et sportive option entretien physique", "Transversal"),
                Triple("SY43", "Android development", "Info"),
                Triple("TI05", "Communication globale du manager", "Transversal"),
                Triple("TI09", "Communication orale en groupe", "Transversal"),
                Triple("WE4A", "Technologies et programmation WEB", "Info"),
                Triple("WE4B", "Technologies WEB avancées", "Info")
            )
            ues.forEach { (code, name, branch) ->
                database.execSQL("INSERT OR IGNORE INTO ue (code, name, branch) VALUES ('$code', '$name', '$branch')")
            }
        }

        private val SEED_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val profPassword = PasswordUtils.hash("prof123")
                val adminPassword = PasswordUtils.hash("admin123")
                db.execSQL(
                    "INSERT INTO user (name, email, password, isAdmin, role, branch) VALUES " +
                    "('Professeur Demo', 'prof.demo@utbm.fr', '$profPassword', 0, 'PROFESSOR', '')," +
                    "('Admin Demo', 'admin.demo@utbm.fr', '$adminPassword', 1, 'STUDENT', '')"
                )
                seedUEs(db)
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                        MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                        MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16
                    )
                    .addCallback(SEED_CALLBACK)
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
