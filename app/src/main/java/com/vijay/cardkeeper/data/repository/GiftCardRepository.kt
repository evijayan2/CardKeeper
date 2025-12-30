package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.GiftCardDao
import com.vijay.cardkeeper.data.entity.GiftCard
import kotlinx.coroutines.flow.Flow

interface GiftCardRepository {
    fun getAllGiftCards(): Flow<List<GiftCard>>
    suspend fun getGiftCardById(id: Int): GiftCard?
    suspend fun insertGiftCard(giftCard: GiftCard)
    suspend fun updateGiftCard(giftCard: GiftCard)
    suspend fun deleteGiftCard(giftCard: GiftCard)
    fun searchGiftCards(query: String): Flow<List<GiftCard>>
}

class GiftCardRepositoryImpl(private val dao: GiftCardDao) : GiftCardRepository {
    override fun getAllGiftCards(): Flow<List<GiftCard>> = dao.getAllGiftCards()
    override suspend fun getGiftCardById(id: Int): GiftCard? = dao.getGiftCardById(id)
    override suspend fun insertGiftCard(giftCard: GiftCard) = dao.insertGiftCard(giftCard)
    override suspend fun updateGiftCard(giftCard: GiftCard) = dao.updateGiftCard(giftCard)
    override suspend fun deleteGiftCard(giftCard: GiftCard) = dao.deleteGiftCard(giftCard)
    override fun searchGiftCards(query: String): Flow<List<GiftCard>> = dao.searchGiftCards(query)
}
