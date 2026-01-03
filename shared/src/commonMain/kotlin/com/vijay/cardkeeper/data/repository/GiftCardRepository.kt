package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Gift_cards
import com.vijay.cardkeeper.data.entity.GiftCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface GiftCardRepository {
    fun getAllGiftCards(): Flow<List<GiftCard>>
    suspend fun getGiftCardById(id: Int): GiftCard?
    suspend fun insertGiftCard(giftCard: GiftCard)
    suspend fun updateGiftCard(giftCard: GiftCard)
    suspend fun deleteGiftCard(giftCard: GiftCard)
    fun searchGiftCards(query: String): Flow<List<GiftCard>>
}

class GiftCardRepositoryImpl(private val database: SqlDelightDatabase) : GiftCardRepository {
    private val queries = database.giftCardQueries

    override fun getAllGiftCards(): Flow<List<GiftCard>> {
        return queries.getAllGiftCards()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    override suspend fun getGiftCardById(id: Int): GiftCard? {
        return queries.getGiftCardById(id.toLong()).executeAsOneOrNull()?.toEntity()
    }

    override suspend fun insertGiftCard(giftCard: GiftCard) {
        queries.insertGiftCard(
            id = if (giftCard.id == 0) null else giftCard.id.toLong(),
            providerName = giftCard.providerName,
            cardNumber = giftCard.cardNumber,
            pin = giftCard.pin,
            frontImagePath = giftCard.frontImagePath,
            backImagePath = giftCard.backImagePath,
            barcode = giftCard.barcode,
            barcodeFormat = giftCard.barcodeFormat?.toLong(),
            qrCode = giftCard.qrCode,
            logoImagePath = giftCard.logoImagePath,
            notes = giftCard.notes
        )
    }

    override suspend fun updateGiftCard(giftCard: GiftCard) {
        queries.updateGiftCard(
            providerName = giftCard.providerName,
            cardNumber = giftCard.cardNumber,
            pin = giftCard.pin,
            frontImagePath = giftCard.frontImagePath,
            backImagePath = giftCard.backImagePath,
            barcode = giftCard.barcode,
            barcodeFormat = giftCard.barcodeFormat?.toLong(),
            qrCode = giftCard.qrCode,
            logoImagePath = giftCard.logoImagePath,
            notes = giftCard.notes,
            id = giftCard.id.toLong()
        )
    }

    override suspend fun deleteGiftCard(giftCard: GiftCard) {
        queries.deleteGiftCard(giftCard.id.toLong())
    }

    override fun searchGiftCards(query: String): Flow<List<GiftCard>> {
        return queries.searchGiftCards(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Gift_cards.toEntity(): GiftCard {
        return GiftCard(
            id = id.toInt(),
            providerName = providerName,
            cardNumber = cardNumber,
            pin = pin,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            barcode = barcode,
            barcodeFormat = barcodeFormat?.toInt(),
            qrCode = qrCode,
            logoImagePath = logoImagePath,
            notes = notes
        )
    }
}
