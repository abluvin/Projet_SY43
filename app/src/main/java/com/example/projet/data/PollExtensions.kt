package com.example.projet.data

import androidx.compose.runtime.Composable
import com.example.projet.data.dao.PollOptionDao
import kotlinx.coroutines.flow.Flow

// Extension pour obtenir le nombre total de votes pour un sondage
suspend fun PollOptionDao.getTotalVotesForPoll(pollId: Int): Int {
    return 0 // À implémenter selon les besoins
}

// Data class pour afficher les statistiques d'un sondage
data class PollWithOptions(
    val poll: Poll,
    val options: List<PollOption>,
    val userVotes: Map<Int, Boolean> = emptyMap()
)

// Data class pour afficher les statistiques simplifiées
data class PollStatistics(
    val pollId: Int,
    val totalVotes: Int,
    val optionVotesMap: Map<Int, Int> = emptyMap()
)

