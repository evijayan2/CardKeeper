package com.vijay.cardkeeper.data.dao

import androidx.room.*
import com.vijay.cardkeeper.data.entity.Passport
import kotlinx.coroutines.flow.Flow

@Dao
interface PassportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(passport: Passport)

    @Update suspend fun update(passport: Passport)

    @Delete suspend fun delete(passport: Passport)

    @Query("SELECT * FROM passports WHERE id = :id") fun getPassport(id: Int): Flow<Passport>

    @Query("SELECT * FROM passports ORDER BY id DESC") fun getAllPassports(): Flow<List<Passport>>
}
