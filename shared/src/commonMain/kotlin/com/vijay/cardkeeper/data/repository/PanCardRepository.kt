package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.PanCardDao
import com.vijay.cardkeeper.data.entity.PanCard
import kotlinx.coroutines.flow.Flow

class PanCardRepository(private val panCardDao: PanCardDao) {
    
    fun getAllPanCards(): Flow<List<PanCard>> = panCardDao.getAll()

    fun getPanCardById(id: Int): Flow<PanCard?> = panCardDao.getById(id)

    suspend fun insertPanCard(panCard: PanCard): Long = panCardDao.insert(panCard)

    suspend fun updatePanCard(panCard: PanCard) = panCardDao.update(panCard)

    suspend fun deletePanCard(panCard: PanCard) = panCardDao.delete(panCard)

    suspend fun deletePanCardById(id: Int) = panCardDao.deleteById(id)

    suspend fun getPanCardCount(): Int = panCardDao.getCount()
}
