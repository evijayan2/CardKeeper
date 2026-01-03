package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Green_cards
import com.vijay.cardkeeper.data.entity.GreenCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class GreenCardRepository(database: SqlDelightDatabase) {
    private val queries = database.greenCardQueries

    val allGreenCards: Flow<List<GreenCard>> = queries.getAllGreenCards()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getGreenCard(id: Int): Flow<GreenCard?> = queries.getGreenCard(id.toLong())
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .map { it?.toEntity() }

    suspend fun insert(greenCard: GreenCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.insert(
            id = if (greenCard.id == 0) null else greenCard.id.toLong(),
            surname = greenCard.surname,
            givenName = greenCard.givenName,
            uscisNumber = greenCard.uscisNumber,
            category = greenCard.category,
            countryOfBirth = greenCard.countryOfBirth,
            dob = greenCard.dob,
            sex = greenCard.sex,
            expiryDate = greenCard.expiryDate,
            residentSince = greenCard.residentSince,
            frontImagePath = greenCard.frontImagePath,
            backImagePath = greenCard.backImagePath
        )
    }

    suspend fun update(greenCard: GreenCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.update(
            surname = greenCard.surname,
            givenName = greenCard.givenName,
            uscisNumber = greenCard.uscisNumber,
            category = greenCard.category,
            countryOfBirth = greenCard.countryOfBirth,
            dob = greenCard.dob,
            sex = greenCard.sex,
            expiryDate = greenCard.expiryDate,
            residentSince = greenCard.residentSince,
            frontImagePath = greenCard.frontImagePath,
            backImagePath = greenCard.backImagePath,
            id = greenCard.id.toLong()
        )
    }

    suspend fun delete(greenCard: GreenCard) = kotlinx.coroutines.withContext(Dispatchers.IO) {
        queries.delete(greenCard.id.toLong())
    }

    fun searchGreenCards(query: String): Flow<List<GreenCard>> {
        return queries.searchGreenCards(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Green_cards.toEntity(): GreenCard {
        return GreenCard(
            id = id.toInt(),
            surname = surname,
            givenName = givenName,
            uscisNumber = uscisNumber,
            category = category,
            countryOfBirth = countryOfBirth,
            dob = dob,
            sex = sex,
            expiryDate = expiryDate,
            residentSince = residentSince,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath
        )
    }
}
