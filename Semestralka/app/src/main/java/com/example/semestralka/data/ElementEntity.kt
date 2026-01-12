package com.example.semestralka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "elements")
data class ElementEntity(
    @PrimaryKey val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val czechName: String,
    val period: Int,       // Řádek
    val group: Int         // Sloupec
)