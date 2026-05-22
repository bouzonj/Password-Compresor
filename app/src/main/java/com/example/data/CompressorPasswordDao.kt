package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CompressorPasswordDao {
    @Query("SELECT * FROM compressor_passwords ORDER BY lastUpdated DESC")
    fun getAllPasswords(): Flow<List<CompressorPassword>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: CompressorPassword): Long

    @Update
    suspend fun updatePassword(password: CompressorPassword)

    @Delete
    suspend fun deletePassword(password: CompressorPassword)

    @Query("SELECT DISTINCT machineBrand FROM compressor_passwords WHERE machineBrand != '' ORDER BY machineBrand ASC")
    fun getUniqueBrands(): Flow<List<String>>

    @Query("SELECT DISTINCT machineModel FROM compressor_passwords WHERE machineModel != '' ORDER BY machineModel ASC")
    fun getUniqueMachineModels(): Flow<List<String>>

    @Query("SELECT DISTINCT controlBoardModel FROM compressor_passwords WHERE controlBoardModel != '' ORDER BY controlBoardModel ASC")
    fun getUniqueControlBoards(): Flow<List<String>>

    @Query("SELECT DISTINCT clientName FROM compressor_passwords WHERE clientName != '' ORDER BY clientName ASC")
    fun getUniqueClients(): Flow<List<String>>
}
