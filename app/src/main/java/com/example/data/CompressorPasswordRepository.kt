package com.example.data

import kotlinx.coroutines.flow.Flow

class CompressorPasswordRepository(private val dao: CompressorPasswordDao) {
    val allPasswords: Flow<List<CompressorPassword>> = dao.getAllPasswords()
    val uniqueBrands: Flow<List<String>> = dao.getUniqueBrands()
    val uniqueMachineModels: Flow<List<String>> = dao.getUniqueMachineModels()
    val uniqueControlBoards: Flow<List<String>> = dao.getUniqueControlBoards()
    val uniqueClients: Flow<List<String>> = dao.getUniqueClients()

    suspend fun insert(password: CompressorPassword): Long {
        return dao.insertPassword(password)
    }

    suspend fun update(password: CompressorPassword) {
        dao.updatePassword(password)
    }

    suspend fun delete(password: CompressorPassword) {
        dao.deletePassword(password)
    }
}
