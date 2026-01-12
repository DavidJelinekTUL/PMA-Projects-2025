package com.example.semestralka.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM elements")
    fun getAllElements(): Flow<List<ElementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElements(elements: List<ElementEntity>)

    @Query("SELECT COUNT(*) FROM elements")
    suspend fun getCount(): Int
}