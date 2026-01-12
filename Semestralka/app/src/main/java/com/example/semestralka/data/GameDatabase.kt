package com.example.semestralka.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//ver 5
@Database(entities = [ElementEntity::class], version = 5, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var Instance: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, GameDatabase::class.java, "semestralka_db")
                    .fallbackToDestructiveMigration() // smazání staré DB
                    .build()
                    .also { Instance = it }
            }
        }
    }
}