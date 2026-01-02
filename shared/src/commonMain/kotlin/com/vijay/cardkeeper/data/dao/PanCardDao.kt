package com.vijay.cardkeeper.data.dao

import androidx.room.*
import com.vijay.cardkeeper.data.entity.PanCard
import kotlinx.coroutines.flow.Flow

@Dao
interface PanCardDao {
    @Query("SELECT * FROM pan_cards ORDER BY id DESC")
    fun getAll(): Flow<List<PanCard>>

    @Query("SELECT * FROM pan_cards WHERE id = :id")
    fun getById(id: Int): Flow<PanCard?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(panCard: PanCard): Long

    @Update
    suspend fun update(panCard: PanCard)

    @Delete
    suspend fun delete(panCard: PanCard)

    @Query("DELETE FROM pan_cards WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM pan_cards")
    suspend fun getCount(): Int
}
