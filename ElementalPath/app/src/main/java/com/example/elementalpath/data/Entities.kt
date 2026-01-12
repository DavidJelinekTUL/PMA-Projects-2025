package com.example.elementalpath.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "elements")
data class ElementEntity(
    @PrimaryKey val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val period: Int, // Y
    val group: Int   // X
)

@Entity(tableName = "game_logs")
data class GameLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val start: String,
    val target: String,
    val steps: Int,
    val success: Boolean
)