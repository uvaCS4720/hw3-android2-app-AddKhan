package edu.nd.pmcburne.hwapp.one.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.nd.pmcburne.hwapp.one.data.database.GameEntity

@Dao
interface GameDao {

    @Query("SELECT * FROM games WHERE date = :date AND gender = :gender")
    suspend fun getGames(date: String, gender: String): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Query("DELETE FROM games")
    suspend fun clearGames()
}