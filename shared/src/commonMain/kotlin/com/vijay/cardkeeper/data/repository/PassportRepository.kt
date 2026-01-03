package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Passports
import com.vijay.cardkeeper.data.entity.Passport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class PassportRepository(database: SqlDelightDatabase) {
    private val queries = database.passportQueries

    val allPassports: Flow<List<Passport>> = queries.getAllPassports()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    fun getPassport(id: Int): Flow<Passport?> = queries.getPassport(id.toLong())
        .asFlow()
        .mapToOneOrNull(Dispatchers.IO)
        .map { it?.toEntity() }

    suspend fun insert(passport: Passport) {
        queries.insert(
            id = if (passport.id == 0) null else passport.id.toLong(),
            passportNumber = passport.passportNumber,
            countryCode = passport.countryCode,
            surname = passport.surname,
            givenNames = passport.givenNames,
            nationality = passport.nationality,
            dob = passport.dob,
            placeOfBirth = passport.placeOfBirth,
            sex = passport.sex,
            dateOfIssue = passport.dateOfIssue,
            dateOfExpiry = passport.dateOfExpiry,
            authority = passport.authority,
            endorsements = passport.endorsements,
            frontImagePath = passport.frontImagePath,
            backImagePath = passport.backImagePath,
            fatherName = passport.fatherName,
            motherName = passport.motherName,
            spouseName = passport.spouseName,
            address = passport.address,
            placeOfIssue = passport.placeOfIssue,
            fileNumber = passport.fileNumber
        )
    }

    suspend fun update(passport: Passport) {
        queries.update(
            passportNumber = passport.passportNumber,
            countryCode = passport.countryCode,
            surname = passport.surname,
            givenNames = passport.givenNames,
            nationality = passport.nationality,
            dob = passport.dob,
            placeOfBirth = passport.placeOfBirth,
            sex = passport.sex,
            dateOfIssue = passport.dateOfIssue,
            dateOfExpiry = passport.dateOfExpiry,
            authority = passport.authority,
            endorsements = passport.endorsements,
            frontImagePath = passport.frontImagePath,
            backImagePath = passport.backImagePath,
            fatherName = passport.fatherName,
            motherName = passport.motherName,
            spouseName = passport.spouseName,
            address = passport.address,
            placeOfIssue = passport.placeOfIssue,
            fileNumber = passport.fileNumber,
            id = passport.id.toLong()
        )
    }

    suspend fun delete(passport: Passport) {
        queries.delete(passport.id.toLong())
    }

    fun searchPassports(query: String): Flow<List<Passport>> {
        return queries.searchPassports(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Passports.toEntity(): Passport {
        return Passport(
            id = id.toInt(),
            passportNumber = passportNumber,
            countryCode = countryCode,
            surname = surname,
            givenNames = givenNames,
            nationality = nationality,
            dob = dob,
            placeOfBirth = placeOfBirth,
            sex = sex,
            dateOfIssue = dateOfIssue,
            dateOfExpiry = dateOfExpiry,
            authority = authority,
            endorsements = endorsements,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            fatherName = fatherName,
            motherName = motherName,
            spouseName = spouseName,
            address = address,
            placeOfIssue = placeOfIssue,
            fileNumber = fileNumber
        )
    }
}
