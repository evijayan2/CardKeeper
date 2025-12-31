package com.vijay.cardkeeper.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.vijay.cardkeeper.data.AppDatabase
import com.vijay.cardkeeper.data.entity.DocumentType
import com.vijay.cardkeeper.data.entity.IdentityDocument
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class IdentityDocumentDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: IdentityDocumentDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.identityDocumentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertDocument_and_getDocumentById_should_work_correctly() {
        runBlocking {
            val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", issueDate = "0", expiryDate = "1000", holderName = "John Doe", country = "US")
            dao.insertDocument(doc)
            val retrieved = dao.getDocumentById(1)
            assertThat(retrieved).isEqualTo(doc)
        }
    }

    @Test
    fun getAllDocuments_should_return_a_sorted_list_of_documents() {
        runBlocking {
            val doc1 = IdentityDocument(id = 1, type = DocumentType.DRIVER_LICENSE, docNumber = "B", holderName = "Jane Doe", country = "US")
            val doc2 = IdentityDocument(id = 2, type = DocumentType.PASSPORT, docNumber = "A", holderName = "John Doe", country = "US")
            dao.insertDocument(doc1)
            dao.insertDocument(doc2)

            val documents = dao.getAllDocuments().first()
            assertThat(documents).containsExactly(doc1, doc2).inOrder()
        }
    }

    @Test
    fun getExpiringDocuments_should_return_documents_expiring_within_the_given_timeframe() {
        runBlocking {
            val now = System.currentTimeMillis()
            val in20Days = now + TimeUnit.DAYS.toMillis(20)
            val in40Days = now + TimeUnit.DAYS.toMillis(40)
            val expired = now - TimeUnit.DAYS.toMillis(1)

            val expiringDoc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, expiryDate = in20Days.toString(), holderName = "John Doe", country = "US", docNumber = "123")
            val futureDoc = IdentityDocument(id = 2, type = DocumentType.PASSPORT, expiryDate = in40Days.toString(), holderName = "John Doe", country = "US", docNumber = "456")
            val expiredDoc = IdentityDocument(id = 3, type = DocumentType.PASSPORT, expiryDate = expired.toString(), holderName = "John Doe", country = "US", docNumber = "789")

            dao.insertDocument(expiringDoc)
            dao.insertDocument(futureDoc)
            dao.insertDocument(expiredDoc)

            val futureDate = now + TimeUnit.DAYS.toMillis(30)
            val expiring = dao.getExpiringDocuments(now, futureDate).first()

            assertThat(expiring).containsExactly(expiringDoc)
        }
    }

    @Test
    fun updateDocument_should_modify_the_document() {
        runBlocking {
            val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
            dao.insertDocument(doc)
            val updatedDoc = doc.copy(docNumber = "456")
            dao.updateDocument(updatedDoc)
            val retrieved = dao.getDocumentById(1)
            assertThat(retrieved).isEqualTo(updatedDoc)
        }
    }

    @Test
    fun deleteDocument_should_remove_the_document() {
        runBlocking {
            val doc = IdentityDocument(id = 1, type = DocumentType.PASSPORT, docNumber = "123", holderName = "John Doe", country = "US")
            dao.insertDocument(doc)
            dao.deleteDocument(doc)
            val retrieved = dao.getDocumentById(1)
            assertThat(retrieved).isNull()
        }
    }
}
