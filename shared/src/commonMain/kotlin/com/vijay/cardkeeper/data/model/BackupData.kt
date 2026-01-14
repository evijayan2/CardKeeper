package com.vijay.cardkeeper.data.model

import com.vijay.cardkeeper.data.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val aadharCards: List<AadharCard> = emptyList(),
    val financialAccounts: List<FinancialAccount> = emptyList(),
    val giftCards: List<GiftCard> = emptyList(),
    val greenCards: List<GreenCard> = emptyList(),
    val identityDocuments: List<IdentityDocument> = emptyList(),
    val insuranceCards: List<InsuranceCard> = emptyList(),
    val panCards: List<PanCard> = emptyList(),
    val passports: List<Passport> = emptyList(),
    val rewardCards: List<RewardCard> = emptyList()
)
