package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.dao.PassportDao
import com.vijay.cardkeeper.data.entity.Passport
import kotlinx.coroutines.flow.Flow

class PassportRepository(private val passportDao: PassportDao) {
    val allPassports: Flow<List<Passport>> = passportDao.getAllPassports()

    fun getPassport(id: Int): Flow<Passport?> = passportDao.getPassport(id)

    suspend fun insert(passport: Passport) {
        passportDao.insert(passport)
    }

    suspend fun update(passport: Passport) {
        passportDao.update(passport)
    }

    suspend fun delete(passport: Passport) {
        passportDao.delete(passport)
    }

    fun searchPassports(query: String): Flow<List<Passport>> {
        return passportDao.searchPassports(query)
    }
}
