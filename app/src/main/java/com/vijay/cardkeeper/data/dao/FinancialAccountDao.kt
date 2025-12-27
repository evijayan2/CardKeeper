package com.vijay.cardkeeper.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vijay.cardkeeper.data.entity.FinancialAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialAccountDao {
    @Query("SELECT * FROM financial_accounts ORDER BY institutionName ASC")
    fun getAllAccounts(): Flow<List<FinancialAccount>>

    @Query("SELECT * FROM financial_accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): FinancialAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinancialAccount)

    @Update
    suspend fun updateAccount(account: FinancialAccount)

    @Delete
    suspend fun deleteAccount(account: FinancialAccount)

    @Query("""
        SELECT * FROM financial_accounts 
        WHERE institutionName LIKE '%' || :query || '%' 
        OR accountName LIKE '%' || :query || '%' 
        OR holderName LIKE '%' || :query || '%' 
        OR number LIKE '%' || :query || '%'
    """)
    fun searchAccounts(query: String): Flow<List<FinancialAccount>>
}
