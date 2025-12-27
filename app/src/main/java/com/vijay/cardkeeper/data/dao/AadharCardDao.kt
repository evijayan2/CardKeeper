package com.vijay.cardkeeper.data.dao

import androidx.room.*
import com.vijay.cardkeeper.data.entity.AadharCard
import kotlinx.coroutines.flow.Flow

@Dao
interface AadharCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(aadharCard: AadharCard)

    @Update suspend fun update(aadharCard: AadharCard)

    @Delete suspend fun delete(aadharCard: AadharCard)

    @Query("SELECT * FROM aadhar_cards WHERE id = :id")
    fun getAadharCard(id: Int): Flow<AadharCard?>

    @Query("SELECT * FROM aadhar_cards ORDER BY id DESC")
    fun getAllAadharCards(): Flow<List<AadharCard>>

    @Query(
            """
        SELECT * FROM aadhar_cards 
        WHERE holderName LIKE '%' || :query || '%' 
        OR maskedAadhaarNumber LIKE '%' || :query || '%'
        OR uid LIKE '%' || :query || '%'
        OR address LIKE '%' || :query || '%'
    """
    )
    fun searchAadharCards(query: String): Flow<List<AadharCard>>
}
