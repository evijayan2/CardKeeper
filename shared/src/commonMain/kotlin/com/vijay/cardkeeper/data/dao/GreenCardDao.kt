package com.vijay.cardkeeper.data.dao

import androidx.room.*
import com.vijay.cardkeeper.data.entity.GreenCard
import kotlinx.coroutines.flow.Flow

@Dao
interface GreenCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(greenCard: GreenCard)

    @Update suspend fun update(greenCard: GreenCard)

    @Delete suspend fun delete(greenCard: GreenCard)

    @Query("SELECT * FROM green_cards WHERE id = :id") fun getGreenCard(id: Int): Flow<GreenCard?>

    @Query("SELECT * FROM green_cards ORDER BY id DESC")
    fun getAllGreenCards(): Flow<List<GreenCard>>

    @Query(
            """
        SELECT * FROM green_cards 
        WHERE uscisNumber LIKE '%' || :query || '%' 
        OR surname LIKE '%' || :query || '%' 
        OR givenName LIKE '%' || :query || '%'
    """
    )
    fun searchGreenCards(query: String): Flow<List<GreenCard>>
}
