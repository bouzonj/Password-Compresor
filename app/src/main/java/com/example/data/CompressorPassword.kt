package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "compressor_passwords")
data class CompressorPassword(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val machineBrand: String,
    val machineModel: String,
    val controlBoardModel: String,
    val clientName: String,
    val accessCode: String,
    val category: String = "Servicio", // e.g., Servicio, Usuario, Fábrica, Especial
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)
