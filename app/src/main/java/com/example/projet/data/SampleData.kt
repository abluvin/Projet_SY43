package com.example.projet.data

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object SampleData {

    // Semaine du 20 avril 2026 (lundi)
    private val semaine1 = LocalDate.of(2026, 4, 20)
    private val semaine2 = semaine1.plusWeeks(1)

    val events: List<Event> = listOf(
        // ...existing code...
    )

    // Posts d'exemple
    val posts: List<Post> = listOf(
        // Post 1: Annonce importante SY43
        Post(
            id = 1,
            text = "📢 IMPORTANT : Le TP SY43 de demain est déplacé de 14h à 16h en raison d'une maintenance du serveur. Les groupes A et B sont regroupés en salle A102.",
            idUser = 1,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 3600000, // il y a 1h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 2: Nouveau groupe d'étude
        Post(
            id = 2,
            text = "✅ Nouveau groupe d'étude créé pour SI40 ! On se réunit chaque mardi à 17h à la BU pour préparer l'examen. Tous les niveaux sont les bienvenus. Rejoignez-nous ! 📚",
            idUser = 2,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 7200000, // il y a 2h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 3: Rappel sobre les DL
        Post(
            id = 3,
            text = "⏰ Rappel : Les devoirs sur SY43 et WE4A sont dus demain à minuit sur Moodle. N'oubliez pas de vérifier le format des fichiers demandé !",
            idUser = 1,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 10800000, // il y a 3h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 4: Événement campus
        Post(
            id = 4,
            text = "🎉 Le Festival Campus 2026 aura lieu samedi 25 avril ! Au programme : concerts, jeux, restauration... Inscriptions gratuites ici : lien_festival.fr",
            idUser = 3,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 14400000, // il y a 4h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 5: Partage de ressources
        Post(
            id = 5,
            text = "📖 J'ai compilé tous les corrigés des TDs SY43 des 3 dernières années. Si quelqu'un les contacter, je peux les partager avec vous !",
            idUser = 4,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 18000000, // il y a 5h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 6: Message vocal d'exemple
        Post(
            id = 6,
            text = "Message vocal : Explication rapide sur le TP de la semaine prochaine",
            idUser = 1,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 21600000, // il y a 6h
            voiceFilePath = "voice_post_1713100800000.3gp",
            voiceDuration = 45000, // 45 secondes
            isPoll = false
        ),
        // Post 7: Sondage - Disponibilité groupe d'étude
        Post(
            id = 7,
            text = "À quel jour préférez-vous les séances de révisions collectives ?",
            idUser = 2,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 25200000, // il y a 7h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = true
        ),
        // Post 8: Annonce restaurant
        Post(
            id = 8,
            text = "🍞 Menu de vendredi au RU : Couscous merguez + salade, Poisson pané + frites, Végétalien : pâtes à la tomate. Bon appétit !",
            idUser = 5,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 28800000, // il y a 8h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 9: Question de cours
        Post(
            id = 9,
            text = "☘️ Question : Dans le chapitre 3 de SY43, il y a un point que je ne comprends pas sur la synchronisation. Quelqu'un pour m'aider ? 🙏",
            idUser = 4,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 32400000, // il y a 9h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = false
        ),
        // Post 10: Sondage - Contenu du cours
        Post(
            id = 10,
            text = "Quel sujet vous intéresse le plus pour le prochain projet ?",
            idUser = 1,
            imageUrl = null,
            timestamp = System.currentTimeMillis() - 36000000, // il y a 10h
            voiceFilePath = null,
            voiceDuration = 0,
            isPoll = true
        )
    )

    // Options pour les sondages
    val pollOptions: List<PollOption> = listOf(
        // Sondage 1: Jour pour études (Post 7)
        PollOption(id = 1, pollId = 7, text = "Lundi", voteCount = 0),
        PollOption(id = 2, pollId = 7, text = "Mardi", voteCount = 0),
        PollOption(id = 3, pollId = 7, text = "Mercredi", voteCount = 0),
        PollOption(id = 4, pollId = 7, text = "Jeudi", voteCount = 0),
        PollOption(id = 5, pollId = 7, text = "Vendredi", voteCount = 0),
        
        // Sondage 2: Contenu du projet (Post 10)
        PollOption(id = 6, pollId = 10, text = "Application Mobile", voteCount = 0),
        PollOption(id = 7, pollId = 10, text = "Système Temps Réel", voteCount = 0),
        PollOption(id = 8, pollId = 10, text = "Développement Web", voteCount = 0),
        PollOption(id = 9, pollId = 10, text = "Intelligence Artificielle", voteCount = 0)
    )

    fun getEventsForDate(date: LocalDate): List<Event> =
        events.filter { it.date == date }.sortedBy { it.startTime }

    fun generateScheduleText(weekStart: LocalDate): String {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val sb = StringBuilder()
        for (i in 0..4) {
            val date = weekStart.plusDays(i.toLong())
            val dayEvents = getEventsForDate(date)
            if (dayEvents.isNotEmpty()) {
                sb.appendLine("${dayName(i)} ${date.dayOfMonth} ${monthName(date.monthValue)} ${date.year}:")
                dayEvents.forEach { e ->
                    sb.appendLine("  • ${e.startTime.format(fmt)}-${e.endTime.format(fmt)}: ${e.title} – ${e.location} (${e.instructor}) [${e.type.label}]")
                }
                sb.appendLine()
            }
        }
        return if (sb.isEmpty()) "Aucun cours cette semaine." else sb.toString()
    }

    private fun dayName(i: Int) = arrayOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi")[i]

    private fun monthName(m: Int) = arrayOf(
        "", "janvier", "février", "mars", "avril", "mai", "juin",
        "juillet", "août", "septembre", "octobre", "novembre", "décembre"
    )[m]
}
