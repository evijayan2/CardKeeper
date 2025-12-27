package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SearchRepository(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: PassportRepository
) {
    fun search(query: String): Flow<List<SearchResult>> {
        val financialSearch = financialRepository.searchAccounts(query)
        val identitySearch = identityRepository.searchDocuments(query)
        val passportSearch = passportRepository.searchPassports(query)

        return combine(financialSearch, identitySearch, passportSearch) {
                accounts,
                identities,
                passports ->
            val results = mutableListOf<SearchResult>()

            accounts.forEach { account ->
                results.add(
                        SearchResult(
                                id = account.id,
                                title = account.institutionName,
                                subtitle = account.accountName,
                                type =
                                        if (account.type ==
                                                        com.vijay.cardkeeper.data.entity.AccountType
                                                                .REWARDS_CARD ||
                                                        account.type ==
                                                                com.vijay.cardkeeper.data.entity
                                                                        .AccountType.LIBRARY_CARD
                                        )
                                                "Rewards"
                                        else "Finance",
                                originalType = account.type.name,
                                logoUrl = account.logoImagePath ?: account.frontImagePath
                        )
                )
            }

            identities.forEach { identity ->
                results.add(
                        SearchResult(
                                id = identity.id,
                                title = identity.type.name.replace("_", " "),
                                subtitle = identity.holderName,
                                type = "Identity",
                                originalType = identity.type.name
                        )
                )
            }

            passports.forEach { passport ->
                results.add(
                        SearchResult(
                                id = passport.id,
                                title = "Passport",
                                subtitle = "${passport.givenNames} ${passport.surname}",
                                type = "Passport",
                                originalType = "PASSPORT"
                        )
                )
            }

            results.sortedBy { it.title }
        }
    }
}
