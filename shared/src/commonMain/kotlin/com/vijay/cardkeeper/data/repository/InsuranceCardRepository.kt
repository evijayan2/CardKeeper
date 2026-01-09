package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Insurance_cards
import com.vijay.cardkeeper.data.entity.InsuranceCard
import com.vijay.cardkeeper.data.entity.InsuranceCardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList

class InsuranceCardRepository(private val database: SqlDelightDatabase) {
    private val queries = database.insuranceCardQueries

    val allInsuranceCards: Flow<List<InsuranceCard>> = queries.getAllInsuranceCards()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getInsuranceCard(id: Int): Flow<InsuranceCard?> {
        return queries.getInsuranceCardById(id.toLong())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.firstOrNull()?.toEntity() }
    }

    suspend fun insert(card: InsuranceCard) = withContext(Dispatchers.IO) {
        queries.insertInsuranceCard(
            id = if (card.id == 0) null else card.id.toLong(),
            providerName = card.providerName,
            planName = card.planName,
            type = card.type.name,
            policyNumber = card.policyNumber,
            groupNumber = card.groupNumber,
            memberId = card.memberId,
            policyHolderName = card.policyHolderName,
            expiryDate = card.expiryDate,
            website = card.website,
            customerServiceNumber = card.customerServiceNumber,
            frontImagePath = card.frontImagePath,
            backImagePath = card.backImagePath,
            colorTheme = card.colorTheme,
            notes = card.notes
        )
    }

    suspend fun update(card: InsuranceCard) = withContext(Dispatchers.IO) {
        queries.updateInsuranceCard(
            providerName = card.providerName,
            planName = card.planName,
            type = card.type.name,
            policyNumber = card.policyNumber,
            groupNumber = card.groupNumber,
            memberId = card.memberId,
            policyHolderName = card.policyHolderName,
            expiryDate = card.expiryDate,
            website = card.website,
            customerServiceNumber = card.customerServiceNumber,
            frontImagePath = card.frontImagePath,
            backImagePath = card.backImagePath,
            colorTheme = card.colorTheme,
            notes = card.notes,
            id = card.id.toLong()
        )
    }

    suspend fun delete(card: InsuranceCard) = withContext(Dispatchers.IO) {
        queries.deleteInsuranceCard(card.id.toLong())
    }

    private fun Insurance_cards.toEntity(): InsuranceCard {
        return InsuranceCard(
            id = id.toInt(),
            providerName = providerName,
            planName = planName,
            type = InsuranceCardType.valueOf(type),
            policyNumber = policyNumber,
            groupNumber = groupNumber,
            memberId = memberId,
            policyHolderName = policyHolderName,
            expiryDate = expiryDate,
            website = website,
            customerServiceNumber = customerServiceNumber,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            colorTheme = colorTheme,
            notes = notes
        )
    }
}
