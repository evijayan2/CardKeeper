package com.vijay.cardkeeper.data.repository

import com.vijay.cardkeeper.data.model.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class SearchRepository(
        private val financialRepository: FinancialRepository,
        private val identityRepository: IdentityRepository,
        private val passportRepository: PassportRepository,
        private val greenCardRepository: GreenCardRepository,
        private val aadharCardRepository: AadharCardRepository,
        private val rewardCardRepository: RewardCardRepository,
        private val giftCardRepository: GiftCardRepository,
        private val panCardRepository: PanCardRepository
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
                                        lowerQuery == "documents" ||
                                        lowerQuery == "driver" ||
                                        lowerQuery == "license" ||
                                        lowerQuery == "dl" ||
                                        lowerQuery.contains("driver") &&
                                                lowerQuery.contains("license") -> "identity"
                                lowerQuery == "aadhar" ||
                                        lowerQuery == "aadhaar" ||
                                        lowerQuery == "adhaar" ||
                                        lowerQuery.contains("aadhar") ||
                                        lowerQuery.contains("आधार") -> "aadhar"
                                lowerQuery == "finance" ||
                                        lowerQuery == "financial" ||
                                        lowerQuery == "bank" ||
                                        lowerQuery == "credit" ||
                                        lowerQuery == "debit" ||
                                        lowerQuery == "atm" ||
                                        lowerQuery.contains("credit") &&
                                                lowerQuery.contains("card") ||
                                        lowerQuery.contains("debit") &&
                                                lowerQuery.contains("card") ||
                                        lowerQuery.contains("atm") && lowerQuery.contains("card") ->
                                        "finance"
                                lowerQuery == "rewards" ||
                                        lowerQuery == "reward" ||
                                        lowerQuery == "library" -> "rewards"
                                lowerQuery == "gift" ||
                                        lowerQuery == "giftcard" ||
                                        lowerQuery == "giftcards" -> "giftcard"
                                lowerQuery == "pan" ||
                                        lowerQuery == "pancard" ||
                                        lowerQuery.contains("pan") && lowerQuery.contains("card") -> "pancard"
                                else -> null
                        }

                val financialSearch = financialRepository.searchAccounts(query)
                val identitySearch = identityRepository.searchDocuments(query)
                val passportSearch = passportRepository.searchPassports(query)
                val greenCardSearch = greenCardRepository.searchGreenCards(query)
                val aadharSearch = aadharCardRepository.searchAadharCards(query)
                val rewardSearch = rewardCardRepository.searchRewardCards(query)
                val giftCardSearch = giftCardRepository.searchGiftCards(query)
                val panCardSearch = panCardRepository.searchPanCards(query)

                // Also get all items for type-based search
                val allFinancial = financialRepository.allAccounts
                val allIdentity = identityRepository.allDocuments
                val allPassports = passportRepository.allPassports
                val allGreenCards = greenCardRepository.allGreenCards
                val allAadharCards = aadharCardRepository.allAadharCards
                val allRewardCards = rewardCardRepository.getAllRewardCards()
                val allGiftCards = giftCardRepository.getAllGiftCards()
                val allPanCards = panCardRepository.allPanCards

                return combine(
                        financialSearch,
                        identitySearch,
                        passportSearch,
                        greenCardSearch,
                        aadharSearch,
                        rewardSearch,
                        giftCardSearch,
                        panCardSearch,
                        allFinancial,
                        allIdentity,
                        allPassports,
                        allGreenCards,
                        allAadharCards,
                        allRewardCards,
                        allGiftCards,
                        allPanCards
                ) { results ->
                        @Suppress("UNCHECKED_CAST")
                        val accounts =
                                results[0] as
                                        List<com.vijay.cardkeeper.data.entity.FinancialAccount>
                        @Suppress("UNCHECKED_CAST")
                        val identities =
                                results[1] as
                                        List<com.vijay.cardkeeper.data.entity.IdentityDocument>
                        @Suppress("UNCHECKED_CAST")
                        val passports =
                                results[2] as List<com.vijay.cardkeeper.data.entity.Passport>
                        @Suppress("UNCHECKED_CAST")
                        val greenCards =
                                results[3] as List<com.vijay.cardkeeper.data.entity.GreenCard>
                        @Suppress("UNCHECKED_CAST")
                        val aadharCards =
                                results[4] as List<com.vijay.cardkeeper.data.entity.AadharCard>
                        @Suppress("UNCHECKED_CAST")
                        val rewardCards =
                                results[5] as List<com.vijay.cardkeeper.data.entity.RewardCard>
                        @Suppress("UNCHECKED_CAST")
                        val giftCards =
                                results[6] as List<com.vijay.cardkeeper.data.entity.GiftCard>
                        @Suppress("UNCHECKED_CAST")
                        val panCards =
                                results[7] as List<com.vijay.cardkeeper.data.entity.PanCard>

                        @Suppress("UNCHECKED_CAST")
                        val allAccounts =
                                results[8] as
                                        List<com.vijay.cardkeeper.data.entity.FinancialAccount>
                        @Suppress("UNCHECKED_CAST")
                        val allIdentities =
                                results[9] as
                                        List<com.vijay.cardkeeper.data.entity.IdentityDocument>
                        @Suppress("UNCHECKED_CAST")
                        val allPassportsList =
                                results[10] as List<com.vijay.cardkeeper.data.entity.Passport>
                        @Suppress("UNCHECKED_CAST")
                        val allGreenCardsList =
                                results[11] as List<com.vijay.cardkeeper.data.entity.GreenCard>
                        @Suppress("UNCHECKED_CAST")
                        val allAadharCardsList =
                                results[12] as List<com.vijay.cardkeeper.data.entity.AadharCard>
                        @Suppress("UNCHECKED_CAST")
                        val allRewardCardsList =
                                results[13] as List<com.vijay.cardkeeper.data.entity.RewardCard>
                        @Suppress("UNCHECKED_CAST")
                        val allGiftCardsList =
                                results[14] as List<com.vijay.cardkeeper.data.entity.GiftCard>
                        @Suppress("UNCHECKED_CAST")
                        val allPanCardsList =
                                results[15] as List<com.vijay.cardkeeper.data.entity.PanCard>

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
                                                        type = "Finance",
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

                        // Helper to add aadhar card
                        fun addAadharResult(aadhar: com.vijay.cardkeeper.data.entity.AadharCard) {
                                val key = "aadhar_${aadhar.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        val last4 = (aadhar.uid ?: aadhar.maskedAadhaarNumber).takeLast(4)
                                        val maskedNumber = if (last4.isNotEmpty()) "XXXX XXXX $last4" else ""
                                        searchResults.add(
                                                SearchResult(
                                                        id = aadhar.id,
                                                        title = "Aadhaar Card",
                                                        subtitle = if (maskedNumber.isNotEmpty()) "${aadhar.holderName} • $maskedNumber" else aadhar.holderName,
                                                        type = "Aadhar",
                                                        originalType = "AADHAR"
                                                )
                                        )
                                }
                        }

                        // Helper to add gift card
                        fun addGiftCardResult(gc: com.vijay.cardkeeper.data.entity.GiftCard) {
                                val key = "gift_${gc.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = gc.id,
                                                        title = gc.providerName,
                                                        subtitle = "GC Code: ${gc.cardNumber?.takeLast(4) ?: "..."}",
                                                        type = "Gift Card",
                                                        originalType = "GIFT_CARD",
                                                        logoUrl = gc.logoImagePath ?: gc.frontImagePath
                                                )
                                        )
                                }
                        }
                        // Helper to add reward card
                        fun addRewardResult(rewardCard: com.vijay.cardkeeper.data.entity.RewardCard) {
                                val key = "reward_${rewardCard.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = rewardCard.id,
                                                        title = rewardCard.name,
                                                        subtitle = rewardCard.type.name.replace("_", " "),
                                                        type = "Rewards",
                                                        originalType = rewardCard.type.name,
                                                        logoUrl = rewardCard.logoImagePath ?: rewardCard.frontImagePath
                                                )
                                        )
                                }
                        }

                        // Helper to add Pan Card
                        fun addPanCardResult(panCard: com.vijay.cardkeeper.data.entity.PanCard) {
                                val key = "pancard_${panCard.id}"
                                if (key !in addedIds) {
                                        addedIds.add(key)
                                        searchResults.add(
                                                SearchResult(
                                                        id = panCard.id,
                                                        title = "Pan Card",
                                                        subtitle = panCard.panNumber,
                                                        type = "Pan Card",
                                                        originalType = "PAN_CARD"
                                                )
                                        )
                                }
                        }

                        // If searching by type, return all items of that type
                        when (searchByType) {
                                "greencard" -> allGreenCardsList.forEach { addGreenCardResult(it) }
                                "passport" -> allPassportsList.forEach { addPassportResult(it) }
                                "identity" -> allIdentities.forEach { addIdentityResult(it) }
                                "aadhar" -> allAadharCardsList.forEach { addAadharResult(it) }
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
                                "rewards" -> allRewardCardsList.forEach { addRewardResult(it) }
                                "giftcard" -> allGiftCardsList.forEach { addGiftCardResult(it) }
                                "pancard" -> allPanCardsList.forEach { addPanCardResult(it) }
                                else -> {
                                        // Regular field-based search
                                        accounts.forEach { addFinancialResult(it) }
                                        identities.forEach { addIdentityResult(it) }
                                        passports.forEach { addPassportResult(it) }
                                        greenCards.forEach { addGreenCardResult(it) }
                                        aadharCards.forEach { addAadharResult(it) }
                                        rewardCards.forEach { addRewardResult(it) }
                                        giftCards.forEach { addGiftCardResult(it) }
                                        panCards.forEach { addPanCardResult(it) }
                                }
                        }

                        searchResults.sortedBy { it.title }
                }
        }
}
