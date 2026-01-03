package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.SqlDelightDatabase
import com.vijay.cardkeeper.Financial_accounts
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.entity.AccountType
import com.vijay.cardkeeper.data.entity.BankAccountSubType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

class FinancialRepository(private val database: SqlDelightDatabase) {
    private val queries = database.financialAccountQueries

    val allAccounts: Flow<List<FinancialAccount>> = queries.getAllAccounts()
        .asFlow()
        .mapToList(Dispatchers.IO)
        .map { list -> list.map { it.toEntity() } }

    suspend fun getAccountById(id: Int): FinancialAccount? {
        return queries.getAccountById(id.toLong()).executeAsOneOrNull()?.toEntity()
    }

    suspend fun insertAccount(account: FinancialAccount) {
        queries.insertAccount(
            id = if (account.id == 0) null else account.id.toLong(),
            type = account.type.name,
            institutionName = account.institutionName,
            accountName = account.accountName,
            holderName = account.holderName,
            number = account.number,
            cvv = account.cvv,
            pinHint = account.pinHint,
            routingNumber = account.routingNumber,
            ifscCode = account.ifscCode,
            swiftCode = account.swiftCode,
            branchCode = account.branchCode,
            wireNumber = account.wireNumber,
            accountSubType = account.accountSubType?.name,
            branchAddress = account.branchAddress,
            branchContactNumber = account.branchContactNumber,
            bankWebUrl = account.bankWebUrl,
            bankBrandColor = account.bankBrandColor,
            holderAddress = account.holderAddress,
            expiryDate = account.expiryDate,
            statementDate = account.statementDate?.toLong(),
            colorTheme = account.colorTheme,
            cardNetwork = account.cardNetwork,
            notes = account.notes,
            cardPin = account.cardPin,
            lostCardContactNumber = account.lostCardContactNumber,
            frontImagePath = account.frontImagePath,
            backImagePath = account.backImagePath,
            barcode = account.barcode,
            barcodeFormat = account.barcodeFormat?.toLong(),
            linkedPhoneNumber = account.linkedPhoneNumber,
            logoImagePath = account.logoImagePath
        )
    }

    suspend fun updateAccount(account: FinancialAccount) {
        queries.updateAccount(
            type = account.type.name,
            institutionName = account.institutionName,
            accountName = account.accountName,
            holderName = account.holderName,
            number = account.number,
            cvv = account.cvv,
            pinHint = account.pinHint,
            routingNumber = account.routingNumber,
            ifscCode = account.ifscCode,
            swiftCode = account.swiftCode,
            branchCode = account.branchCode,
            wireNumber = account.wireNumber,
            accountSubType = account.accountSubType?.name,
            branchAddress = account.branchAddress,
            branchContactNumber = account.branchContactNumber,
            bankWebUrl = account.bankWebUrl,
            bankBrandColor = account.bankBrandColor,
            holderAddress = account.holderAddress,
            expiryDate = account.expiryDate,
            statementDate = account.statementDate?.toLong(),
            colorTheme = account.colorTheme,
            cardNetwork = account.cardNetwork,
            notes = account.notes,
            cardPin = account.cardPin,
            lostCardContactNumber = account.lostCardContactNumber,
            frontImagePath = account.frontImagePath,
            backImagePath = account.backImagePath,
            barcode = account.barcode,
            barcodeFormat = account.barcodeFormat?.toLong(),
            linkedPhoneNumber = account.linkedPhoneNumber,
            logoImagePath = account.logoImagePath,
            id = account.id.toLong()
        )
    }

    suspend fun deleteAccount(account: FinancialAccount) {
        queries.deleteAccount(account.id.toLong())
    }

    fun searchAccounts(query: String): Flow<List<FinancialAccount>> {
        return queries.searchAccounts(query)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toEntity() } }
    }

    private fun Financial_accounts.toEntity(): FinancialAccount {
        return FinancialAccount(
            id = id.toInt(),
            type = AccountType.valueOf(type),
            institutionName = institutionName,
            accountName = accountName,
            holderName = holderName,
            number = number,
            cvv = cvv,
            pinHint = pinHint,
            routingNumber = routingNumber,
            ifscCode = ifscCode,
            swiftCode = swiftCode,
            branchCode = branchCode,
            wireNumber = wireNumber,
            accountSubType = accountSubType?.let { BankAccountSubType.valueOf(it) },
            branchAddress = branchAddress,
            branchContactNumber = branchContactNumber,
            bankWebUrl = bankWebUrl,
            bankBrandColor = bankBrandColor,
            holderAddress = holderAddress,
            expiryDate = expiryDate,
            statementDate = statementDate?.toInt(),
            colorTheme = colorTheme,
            cardNetwork = cardNetwork,
            notes = notes,
            cardPin = cardPin,
            lostCardContactNumber = lostCardContactNumber,
            frontImagePath = frontImagePath,
            backImagePath = backImagePath,
            barcode = barcode,
            barcodeFormat = barcodeFormat?.toInt(),
            linkedPhoneNumber = linkedPhoneNumber,
            logoImagePath = logoImagePath
        )
    }
}
