package com.example.elementalpath

import android.app.Application
import com.example.elementalpath.data.*

class GameApplication : Application() {
    // Manual DI Container
    val repository: GameRepository by lazy {
        OfflineGameRepository(GameDatabase.getDatabase(this).gameDao())
    }
}