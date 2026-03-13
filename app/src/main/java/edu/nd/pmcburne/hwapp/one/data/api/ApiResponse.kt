package edu.nd.pmcburne.hwapp.one.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    val games: List<GameWrapper>
)

data class GameWrapper(
    val game: Game
)

data class Game(
    @SerializedName("gameID") val gameId: String?,
    val gameState: String,
    val startTime: String,
    val currentPeriod: String,
    val contestClock: String,
    val home: Team,
    val away: Team
)

data class Team(
    val score: String?,
    val winner: Boolean,
    val names: TeamNames
)

data class TeamNames(
    val short: String
)
