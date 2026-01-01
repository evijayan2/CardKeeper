package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.AadharCardDao
import com.vijay.cardkeeper.data.entity.AadharCard
import kotlinx.coroutines.flow.Flow

class AadharCardRepository(private val aadharCardDao: AadharCardDao) {
    val allAadharCards: Flow<List<AadharCard>> = aadharCardDao.getAllAadharCards()

    fun getAadharCard(id: Int): Flow<AadharCard?> = aadharCardDao.getAadharCard(id)

    suspend fun insert(aadharCard: AadharCard) {
        aadharCardDao.insert(aadharCard)
    }

    suspend fun update(aadharCard: AadharCard) {
        aadharCardDao.update(aadharCard)
    }

    suspend fun delete(aadharCard: AadharCard) {
        aadharCardDao.delete(aadharCard)
    }

    fun searchAadharCards(query: String): Flow<List<AadharCard>> {
        return aadharCardDao.searchAadharCards(query)
    }
}
