package com.vijay.cardkeeper.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vijay.cardkeeper.data.entity.IdentityDocument
import kotlinx.coroutines.flow.Flow

@Dao
interface IdentityDocumentDao {
    @Query("SELECT * FROM identity_documents ORDER BY type ASC")
    fun getAllDocuments(): Flow<List<IdentityDocument>>

    @Query("SELECT * FROM identity_documents WHERE id = :id")
    suspend fun getDocumentById(id: Int): IdentityDocument?
    
    // Helper to find documents expiring soon (e.g., next 30 days)
    // timestamp is in millis
    @Query("SELECT * FROM identity_documents WHERE expiryDate BETWEEN :now AND :futureDate ORDER BY expiryDate ASC")
    fun getExpiringDocuments(now: Long, futureDate: Long): Flow<List<IdentityDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: IdentityDocument)

    @Update
    suspend fun updateDocument(doc: IdentityDocument)

    @Delete
    suspend fun deleteDocument(doc: IdentityDocument)
}
