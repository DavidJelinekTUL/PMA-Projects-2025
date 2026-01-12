package com.example.semestralka.data

import com.google.firebase.Timestamp

data class ScoreEntry(
    val playerName: String = "",
    val moves: Int = 0,
    val dateId: String = "", // den pro seed tj. např. "2025-01-12"
    val timestamp: Timestamp = Timestamp.now()
)