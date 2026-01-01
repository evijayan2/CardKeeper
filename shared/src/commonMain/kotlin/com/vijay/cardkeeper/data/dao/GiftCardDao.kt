package com.vijay.cardkeeper.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vijay.cardkeeper.data.entity.GiftCard
import kotlinx.coroutines.flow.Flow

@Dao
interface GiftCardDao {
    @Query("SELECT * FROM gift_cards ORDER BY providerName ASC")
    fun getAllGiftCards(): Flow<List<GiftCard>>

    @Query("SELECT * FROM gift_cards WHERE id = :id")
    suspend fun getGiftCardById(id: Int): GiftCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftCard(giftCard: GiftCard)

    @Update
    suspend fun updateGiftCard(giftCard: GiftCard)

    @Delete
    suspend fun deleteGiftCard(giftCard: GiftCard)

    @Query("""
        SELECT * FROM gift_cards 
        WHERE providerName LIKE '%' || :query || '%' 
        OR cardNumber LIKE '%' || :query || '%'
    """)
    fun searchGiftCards(query: String): Flow<List<GiftCard>>
}
