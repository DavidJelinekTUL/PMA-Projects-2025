package com.example.elementalpath.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM elements")
    fun getAllElements(): Flow<List<ElementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertElements(elements: List<ElementEntity>)

    @Query("SELECT COUNT(*) FROM elements")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertLog(log: GameLog)
}