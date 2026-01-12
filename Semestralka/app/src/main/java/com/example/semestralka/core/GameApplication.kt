package com.example.semestralka.core

import android.app.Application
import com.example.semestralka.data.GameDatabase
import com.example.semestralka.data.GameRepository
import com.google.firebase.firestore.FirebaseFirestore

class GameApplication : Application() {
    val database by lazy { GameDatabase.getDatabase(this) }

    val repository by lazy {
        GameRepository(
            dao = database.gameDao(),
            firestore = FirebaseFirestore.getInstance()
        )
    }
}