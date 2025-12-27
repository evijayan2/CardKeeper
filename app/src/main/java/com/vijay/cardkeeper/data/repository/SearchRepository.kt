package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SearchRepository(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: PassportRepository,
        private val greenCardRepository: GreenCardRepository
) {
        fun search(query: String): Flow<List<SearchResult>> {
                val lowerQuery = query.lowercase().trim()

                // Check if user is searching by type
                val searchByType =
                        when {
                                lowerQuery.contains("green") && lowerQuery.contains("card") ->
                                        "greencard"
                                lowerQuery == "greencard" || lowerQuery == "gc" -> "greencard"
                                lowerQuery == "passport" || lowerQuery == "passports" -> "passport"
                                lowerQuery == "identity" ||
                                        lowerQuery == "id" ||
                                        lowerQuery == "document" ||
                                        lowerQuery == "documents" -> "identity"
                                lowerQuery == "finance" ||
                                        lowerQuery == "financial" ||
                                        lowerQuery == "bank" ||
                                        lowerQuery == "card" ||
                                        lowerQuery == "cards" -> "finance"
                                lowerQuery == "rewards" ||
                                        lowerQuery == "reward" ||
                                        lowerQuery == "library" -> "rewards"
                                else -> null
                        }

                val financialSearch = financialRepository.searchAccounts(query)
                val identitySearch = identityRepository.searchDocuments(query)
                val passportSearch = passportRepository.searchPassports(query)
                val greenCardSearch = greenCardRepository.searchGreenCards(query)

                // Also get all items for type-based search
                val allFinancial = financialRepository.allAccounts
                val allIdentity = identityRepository.allDocuments
                val allPassports = passportRepository.allPassports
                val allGreenCards = greenCardRepository.allGreenCards

                return combine(
                        financialSearch,
                        identitySearch,
                        passportSearch,
                        greenCardSearch,
                        allFinancial,
                        allIdentity,
                        allPassports,
                        allGreenCards
                ) { results ->
                        val accounts =
                                results[0] as
                                        List<com.vijay.cardkeeper.data.entity.FinancialAccount>
                        val identities =
                                results[1] as
                                        List<com.vijay.cardkeeper.data.entity.IdentityDocument>
                        val passports =
                                results[2] as List<com.vijay.cardkeeper.data.entity.Passport>
                        val greenCards =
                                results[3] as List<com.vijay.cardkeeper.data.entity.GreenCard>

                        val allAccounts =
                                results[4] as
                                        List<com.vijay.cardkeeper.data.entity.FinancialAccount>
                        val allIdentities =
                                results[5] as
                                        List<com.vijay.cardkeeper.data.entity.IdentityDocument>
                        val allPassportsList =
                                results[6] as List<com.vijay.cardkeeper.data.entity.Passport>
                        val allGreenCardsList =
                                results[7] as List<com.vijay.cardkeeper.data.entity.GreenCard>

                        val searchResults = mutableListOf<SearchResult>()
                        val addedIds =
                                mutableSetOf<String>() // Track added items to avoid duplicates

                        // Helper to add financial account
                        fun addFinancialResult(
                                account: com.vijay.cardkeeper.data.entity.FinancialAccount
                        ) {
                                val key = "finance_${account.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = account.id,
                                                        title = account.institutionName,
                                                        subtitle = account.accountName,
                                                        type =
                                                                if (account.type ==
                                                                                com.vijay.cardkeeper
                                                                                        .data.entity
                                                                                        .AccountType
                                                                                        .REWARDS_CARD ||
                                                                                account.type ==
                                                                                        com.vijay
                                                                                                .cardkeeper
                                                                                                .data
                                                                                                .entity
                                                                                                .AccountType
                                                                                                .LIBRARY_CARD
                                                                )
                                                                        "Rewards"
                                                                else "Finance",
                                                        originalType = account.type.name,
                                                        logoUrl = account.logoImagePath
                                                                        ?: account.frontImagePath
                                                )
                                        )
                                }
                        }

                        // Helper to add identity document
                        fun addIdentityResult(
                                identity: com.vijay.cardkeeper.data.entity.IdentityDocument
                        ) {
                                val key = "identity_${identity.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = identity.id,
                                                        title =
                                                                identity.type.name.replace(
                                                                        "_",
                                                                        " "
                                                                ),
                                                        subtitle = identity.holderName,
                                                        type = "Identity",
                                                        originalType = identity.type.name
                                                )
                                        )
                                }
                        }

                        // Helper to add passport
                        fun addPassportResult(passport: com.vijay.cardkeeper.data.entity.Passport) {
                                val key = "passport_${passport.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = passport.id,
                                                        title = "Passport",
                                                        subtitle =
                                                                "${passport.givenNames} ${passport.surname}",
                                                        type = "Passport",
                                                        originalType = "PASSPORT"
                                                )
                                        )
                                }
                        }

                        // Helper to add green card
                        fun addGreenCardResult(gc: com.vijay.cardkeeper.data.entity.GreenCard) {
                                val key = "greencard_${gc.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = gc.id,
                                                        title = "Green Card",
                                                        subtitle = "${gc.givenName} ${gc.surname}",
                                                        type = "Green Card",
                                                        originalType = "GREEN_CARD"
                                                )
                                        )
                                }
                        }

                        // If searching by type, return all items of that type
                        when (searchByType) {
                                "greencard" -> allGreenCardsList.forEach { addGreenCardResult(it) }
                                "passport" -> allPassportsList.forEach { addPassportResult(it) }
                                "identity" -> allIdentities.forEach { addIdentityResult(it) }
                                "finance" ->
                                        allAccounts
                                                .filter {
                                                        it.type !=
                                                                com.vijay.cardkeeper.data.entity
                                                                        .AccountType.REWARDS_CARD &&
                                                                it.type !=
                                                                        com.vijay.cardkeeper.data
                                                                                .entity.AccountType
                                                                                .LIBRARY_CARD
                                                }
                                                .forEach { addFinancialResult(it) }
                                "rewards" ->
                                        allAccounts
                                                .filter {
                                                        it.type ==
                                                                com.vijay.cardkeeper.data.entity
                                                                        .AccountType.REWARDS_CARD ||
                                                                it.type ==
                                                                        com.vijay.cardkeeper.data
                                                                                .entity.AccountType
                                                                                .LIBRARY_CARD
                                                }
                                                .forEach { addFinancialResult(it) }
                                else -> {
                                        // Regular field-based search
                                        accounts.forEach { addFinancialResult(it) }
                                        identities.forEach { addIdentityResult(it) }
                                        passports.forEach { addPassportResult(it) }
                                        greenCards.forEach { addGreenCardResult(it) }
                                }
                        }

                        searchResults.sortedBy { it.title }
                }
        }
}
