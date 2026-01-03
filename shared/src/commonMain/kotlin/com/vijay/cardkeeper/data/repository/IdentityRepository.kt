package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Identity_documents
import com.vijay.cardkeeper.data.entity.IdentityDocument
import com.vijay.cardkeeper.data.entity.DocumentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class IdentityRepository(private val database: SqlDelightDatabase) {
    private val queries = database.identityDocumentQueries

    val allDocuments: Flow<List<IdentityDocument>> = queries.getAllDocuments()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getExpiringDocuments(now: Long, futureDate: Long): Flow<List<IdentityDocument>> {
        return queries.getExpiringDocuments(now.toString(), futureDate.toString())
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    suspend fun getDocumentById(id: Int): IdentityDocument? {
        return queries.getDocumentById(id.toLong()).executeAsOneOrNull()?.toEntity()
    }

    suspend fun insertDocument(doc: IdentityDocument) {
        queries.insertDocument(
            id = if (doc.id == 0) null else doc.id.toLong(),
            type = doc.type.name,
            country = doc.country,
            docNumber = doc.docNumber,
            holderName = doc.holderName,
            issueDate = doc.issueDate,
            expiryDate = doc.expiryDate,
            issuingAuthority = doc.issuingAuthority,
            metadata = doc.metadata,
            frontImagePath = doc.frontImagePath,
            backImagePath = doc.backImagePath,
            state = doc.state,
            address = doc.address,
            dob = doc.dob,
            sex = doc.sex,
            eyeColor = doc.eyeColor,
            height = doc.height,
            licenseClass = doc.licenseClass,
            restrictions = doc.restrictions,
            endorsements = doc.endorsements
        )
    }

    suspend fun updateDocument(doc: IdentityDocument) {
        queries.updateDocument(
            type = doc.type.name,
            country = doc.country,
            docNumber = doc.docNumber,
            holderName = doc.holderName,
            issueDate = doc.issueDate,
            expiryDate = doc.expiryDate,
            issuingAuthority = doc.issuingAuthority,
            metadata = doc.metadata,
            frontImagePath = doc.frontImagePath,
            backImagePath = doc.backImagePath,
            state = doc.state,
            address = doc.address,
            dob = doc.dob,
            sex = doc.sex,
            eyeColor = doc.eyeColor,
            height = doc.height,
            licenseClass = doc.licenseClass,
            restrictions = doc.restrictions,
            endorsements = doc.endorsements,
            id = doc.id.toLong()
        )
    }

    suspend fun deleteDocument(doc: IdentityDocument) {
        queries.deleteDocument(doc.id.toLong())
    }

    fun searchDocuments(query: String): Flow<List<IdentityDocument>> {
        return queries.searchDocuments(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Identity_documents.toEntity(): IdentityDocument {
        return IdentityDocument(
            id = id.toInt(),
            type = DocumentType.valueOf(type),
            country = country,
            docNumber = docNumber,
            holderName = holderName,
            issueDate = issueDate,
            expiryDate = expiryDate,
            issuingAuthority = issuingAuthority,
            metadata = metadata,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            state = state,
            address = address,
            dob = dob,
            sex = sex,
            eyeColor = eyeColor,
            height = height,
            licenseClass = licenseClass,
            restrictions = restrictions,
            endorsements = endorsements
        )
    }
}
