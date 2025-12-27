package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.IdentityDocument
import kotlinx.coroutines.flow.Flow

class IdentityRepository(private val identityDao: IdentityDocumentDao) {

    val allDocuments: Flow<List<IdentityDocument>> = identityDao.getAllDocuments()

    fun getExpiringDocuments(now: Long, futureDate: Long): Flow<List<IdentityDocument>> {
        return identityDao.getExpiringDocuments(now, futureDate)
    }

    suspend fun getDocumentById(id: Int): IdentityDocument? {
        return identityDao.getDocumentById(id)
    }

    suspend fun insertDocument(doc: IdentityDocument) {
        identityDao.insertDocument(doc)
    }

    suspend fun updateDocument(doc: IdentityDocument) {
        identityDao.updateDocument(doc)
    }

    suspend fun deleteDocument(doc: IdentityDocument) {
        identityDao.deleteDocument(doc)
    }

    fun searchDocuments(query: String): Flow<List<IdentityDocument>> {
        return identityDao.searchDocuments(query)
    }
}
