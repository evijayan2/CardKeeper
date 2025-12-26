package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.IdentityDocumentDao
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.IdentityDocument
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdentityRepositoryTest {

    private lateinit var repository: IdentityRepository
    private val dao: IdentityDocumentDao = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        repository = IdentityRepository(dao)
    }

    @Test
    fun `getDocumentById should call dao`() = runBlocking {
        repository.getDocumentById(1)
        coVerify { dao.getDocumentById(1) }
    }

    @Test
    fun `insertDocument should call dao`() = runBlocking {
        val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
        repository.insertDocument(doc)
        coVerify { dao.insertDocument(doc) }
    }

    @Test
    fun `updateDocument should call dao`() = runBlocking {
        val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
        repository.updateDocument(doc)
        coVerify { dao.updateDocument(doc) }
    }

    @Test
    fun `deleteDocument should call dao`() = runBlocking {
        val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
        repository.deleteDocument(doc)
        coVerify { dao.deleteDocument(doc) }
    }

    @Test
    fun `allDocuments should get from dao`() {
        repository.allDocuments
        coVerify { dao.getAllDocuments() }
    }

    @Test
    fun `getExpiringDocuments should call dao`() {
        repository.getExpiringDocuments(1L, 2L)
        coVerify { dao.getExpiringDocuments(1L, 2L) }
    }
}
