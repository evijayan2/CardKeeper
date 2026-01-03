package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Pan_cards
import com.vijay.cardkeeper.data.entity.PanCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class PanCardRepository(database: SqlDelightDatabase) {
    private val queries = database.panCardQueries

    val allPanCards: Flow<List<PanCard>> = queries.getAllPanCards()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getPanCard(id: Int): Flow<PanCard?> = queries.getPanCard(id.toLong())
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .map { it?.toEntity() }

    suspend fun insert(panCard: PanCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.insert(
            id = if (panCard.id == 0) null else panCard.id.toLong(),
            panNumber = panCard.panNumber,
            holderName = panCard.holderName,
            fatherName = panCard.fatherName,
            dob = panCard.dob,
            frontImagePath = panCard.frontImagePath,
            backImagePath = panCard.backImagePath,
            issueDate = panCard.issueDate,
            acknowledgementNumber = panCard.acknowledgementNumber
        )
    }

    suspend fun update(panCard: PanCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.update(
            panNumber = panCard.panNumber,
            holderName = panCard.holderName,
            fatherName = panCard.fatherName,
            dob = panCard.dob,
            frontImagePath = panCard.frontImagePath,
            backImagePath = panCard.backImagePath,
            issueDate = panCard.issueDate,
            acknowledgementNumber = panCard.acknowledgementNumber,
            id = panCard.id.toLong()
        )
    }

    suspend fun delete(panCard: PanCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.delete(panCard.id.toLong())
    }

    fun searchPanCards(query: String): Flow<List<PanCard>> {
        return queries.searchPanCards(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Pan_cards.toEntity(): PanCard {
        return PanCard(
            id = id.toInt(),
            panNumber = panNumber,
            holderName = holderName,
            fatherName = fatherName,
            dob = dob,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            issueDate = issueDate,
            acknowledgementNumber = acknowledgementNumber
        )
    }
}
