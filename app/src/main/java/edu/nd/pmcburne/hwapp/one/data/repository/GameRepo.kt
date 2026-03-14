package edu.nd.pmcburne.hwapp.one.data.repository

import android.util.Log
import edu.nd.pmcburne.hwapp.one.data.api.RetrofitInstance
import edu.nd.pmcburne.hwapp.one.data.database.GameDao
import edu.nd.pmcburne.hwapp.one.data.database.GameEntity

class GameRepo(private val dao: GameDao) {

    fun getGames(date: String, gender: String) = dao.getGames(date, gender)

    suspend fun refreshGames(gender: String, year: String, month: String, day: String) {
        try {
            val response = RetrofitInstance.api.getGameInfo(gender, year, month, day)

            val entities = response.games.map { wrapper ->
                val g = wrapper.game
                val dateKey = "$year-$month-$day"

                GameEntity(
                    gameId = g.gameId ?: "unknown_${g.home.names.short}_${g.away.names.short}",                    gender = gender,
                    date = dateKey,
                    homeTeam = g.home.names.short,
                    awayTeam = g.away.names.short,
                    status = g.gameState,
                    startTime = g.startTime,
                    homeScore = g.home.score?.toIntOrNull(),
                    awayScore = g.away.score?.toIntOrNull(),
                    period = g.currentPeriod.toIntOrNull(),
                    clock = g.contestClock,
                    winner = if (g.gameState == "final") {
                        if (g.home.winner) g.home.names.short else g.away.names.short
                    } else null
                )
            }

            dao.insertGames(entities)

        } catch (e: Exception) {
            Log.e("Repo", "Network failed or parsing error", e)
        }
    }
}