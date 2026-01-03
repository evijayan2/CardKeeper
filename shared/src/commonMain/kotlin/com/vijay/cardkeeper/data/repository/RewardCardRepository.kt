package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Reward_cards
import com.vijay.cardkeeper.data.entity.RewardCard
import com.vijay.cardkeeper.data.entity.AccountType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

interface RewardCardRepository {
    fun getAllRewardCards(): Flow<List<RewardCard>>
    suspend fun getRewardCardById(id: Int): RewardCard?
    suspend fun insertRewardCard(rewardCard: RewardCard)
    suspend fun updateRewardCard(rewardCard: RewardCard)
    suspend fun deleteRewardCard(rewardCard: RewardCard)
    fun searchRewardCards(query: String): Flow<List<RewardCard>>
}

class RewardCardRepositoryImpl(private val database: SqlDelightDatabase) : RewardCardRepository {
    private val queries = database.rewardCardQueries

    override fun getAllRewardCards(): Flow<List<RewardCard>> {
        return queries.getAllRewardCards()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    override suspend fun getRewardCardById(id: Int): RewardCard? {
        return queries.getRewardCardById(id.toLong()).executeAsOneOrNull()?.toEntity()
    }

    override suspend fun insertRewardCard(rewardCard: RewardCard) {
        queries.insertRewardCard(
            id = if (rewardCard.id == 0) null else rewardCard.id.toLong(),
            name = rewardCard.name,
            type = rewardCard.type.name,
            barcode = rewardCard.barcode,
            barcodeFormat = rewardCard.barcodeFormat?.toLong(),
            linkedPhoneNumber = rewardCard.linkedPhoneNumber,
            frontImagePath = rewardCard.frontImagePath,
            backImagePath = rewardCard.backImagePath,
            logoImagePath = rewardCard.logoImagePath,
            notes = rewardCard.notes
        )
    }

    override suspend fun updateRewardCard(rewardCard: RewardCard) {
        queries.updateRewardCard(
            name = rewardCard.name,
            type = rewardCard.type.name,
            barcode = rewardCard.barcode,
            barcodeFormat = rewardCard.barcodeFormat?.toLong(),
            linkedPhoneNumber = rewardCard.linkedPhoneNumber,
            frontImagePath = rewardCard.frontImagePath,
            backImagePath = rewardCard.backImagePath,
            logoImagePath = rewardCard.logoImagePath,
            notes = rewardCard.notes,
            id = rewardCard.id.toLong()
        )
    }

    override suspend fun deleteRewardCard(rewardCard: RewardCard) {
        queries.deleteRewardCard(rewardCard.id.toLong())
    }

    override fun searchRewardCards(query: String): Flow<List<RewardCard>> {
        return queries.searchRewardCards(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Reward_cards.toEntity(): RewardCard {
        return RewardCard(
            id = id.toInt(),
            name = name,
            type = AccountType.valueOf(type),
            barcode = barcode,
            barcodeFormat = barcodeFormat?.toInt(),
            linkedPhoneNumber = linkedPhoneNumber,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            logoImagePath = logoImagePath,
            notes = notes
        )
    }
}
