package edu.nd.pmcburne.hwapp.one.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.nd.pmcburne.hwapp.one.data.database.GameEntity

@Database(entities = [GameEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gameDao(): GameDao
}