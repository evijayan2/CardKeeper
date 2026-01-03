package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Aadhar_cards
import com.vijay.cardkeeper.data.entity.AadharCard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class AadharCardRepository(database: SqlDelightDatabase) {
    private val queries = database.aadharCardQueries

    val allAadharCards: Flow<List<AadharCard>> = queries.getAllAadharCards()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getAadharCard(id: Int): Flow<AadharCard?> = queries.getAadharCard(id.toLong())
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .map { it?.toEntity() }

    suspend fun insert(aadharCard: AadharCard) {
        queries.insert(
            id = if (aadharCard.id == 0) null else aadharCard.id.toLong(),
            referenceId = aadharCard.referenceId,
            holderName = aadharCard.holderName,
            dob = aadharCard.dob,
            gender = aadharCard.gender,
            address = aadharCard.address,
            pincode = aadharCard.pincode,
            maskedAadhaarNumber = aadharCard.maskedAadhaarNumber,
            uid = aadharCard.uid,
            vid = aadharCard.vid,
            photoBase64 = aadharCard.photoBase64,
            timestamp = aadharCard.timestamp,
            digitalSignature = aadharCard.digitalSignature,
            certificateId = aadharCard.certificateId,
            enrollmentNumber = aadharCard.enrollmentNumber,
            email = aadharCard.email,
            mobile = aadharCard.mobile,
            frontImagePath = aadharCard.frontImagePath,
            backImagePath = aadharCard.backImagePath,
            qrData = aadharCard.qrData
        )
    }

    suspend fun update(aadharCard: AadharCard) {
        queries.update(
            referenceId = aadharCard.referenceId,
            holderName = aadharCard.holderName,
            dob = aadharCard.dob,
            gender = aadharCard.gender,
            address = aadharCard.address,
            pincode = aadharCard.pincode,
            maskedAadhaarNumber = aadharCard.maskedAadhaarNumber,
            uid = aadharCard.uid,
            vid = aadharCard.vid,
            photoBase64 = aadharCard.photoBase64,
            timestamp = aadharCard.timestamp,
            digitalSignature = aadharCard.digitalSignature,
            certificateId = aadharCard.certificateId,
            enrollmentNumber = aadharCard.enrollmentNumber,
            email = aadharCard.email,
            mobile = aadharCard.mobile,
            frontImagePath = aadharCard.frontImagePath,
            backImagePath = aadharCard.backImagePath,
            qrData = aadharCard.qrData,
            id = aadharCard.id.toLong()
        )
    }

    suspend fun delete(aadharCard: AadharCard) {
        queries.delete(aadharCard.id.toLong())
    }

    fun searchAadharCards(query: String): Flow<List<AadharCard>> {
        return queries.searchAadharCards(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Aadhar_cards.toEntity(): AadharCard {
        return AadharCard(
            id = id.toInt(),
            referenceId = referenceId,
            holderName = holderName,
            dob = dob,
            gender = gender,
            address = address,
            pincode = pincode,
            maskedAadhaarNumber = maskedAadhaarNumber,
            uid = uid,
            vid = vid,
            photoBase64 = photoBase64,
            timestamp = timestamp,
            digitalSignature = digitalSignature,
            certificateId = certificateId,
            enrollmentNumber = enrollmentNumber,
            email = email,
            mobile = mobile,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            qrData = qrData
        )
    }
}
