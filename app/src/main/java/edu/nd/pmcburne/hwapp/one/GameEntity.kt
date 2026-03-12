package edu.nd.pmcburne.hwapp.one

import androidx.room.*

@Entity(tableName = "games")
data class GameEntity(

    @PrimaryKey
    val gameId: String,
    val gender: String,
    val date: String,

    val homeTeam: String,
    val awayTeam: String,

    val status: String,
    val startTime: String?,

    val homeScore: Int?,
    val awayScore: Int?,

    val period: Int?,
    val clock: String?,

    val winner: String?
)