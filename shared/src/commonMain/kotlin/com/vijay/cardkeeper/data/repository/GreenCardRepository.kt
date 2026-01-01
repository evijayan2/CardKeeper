package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.GreenCardDao
import com.vijay.cardkeeper.data.entity.GreenCard
import kotlinx.coroutines.flow.Flow

class GreenCardRepository(private val greenCardDao: GreenCardDao) {
    val allGreenCards: Flow<List<GreenCard>> = greenCardDao.getAllGreenCards()

    fun getGreenCard(id: Int): Flow<GreenCard?> = greenCardDao.getGreenCard(id)

    suspend fun insert(greenCard: GreenCard) {
        greenCardDao.insert(greenCard)
    }

    suspend fun update(greenCard: GreenCard) {
        greenCardDao.update(greenCard)
    }

    suspend fun delete(greenCard: GreenCard) {
        greenCardDao.delete(greenCard)
    }

    fun searchGreenCards(query: String): Flow<List<GreenCard>> {
        return greenCardDao.searchGreenCards(query)
    }
}
