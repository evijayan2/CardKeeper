package com.vijay.cardkeeper.util

import android.content.Context
import com.vijay.cardkeeper.data.entity.FinancialAccount
import com.vijay.cardkeeper.data.repository.AadharCardRepository
import com.vijay.cardkeeper.data.repository.FinancialRepository
import com.vijay.cardkeeper.data.repository.GiftCardRepository
import com.vijay.cardkeeper.data.repository.GreenCardRepository
import com.vijay.cardkeeper.data.repository.IdentityRepository
import com.vijay.cardkeeper.data.repository.PanCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class ImageMigrationManager(
    private val context: Context,
    private val financialRepository: FinancialRepository,
    private val identityRepository: IdentityRepository,
    private val passportRepository: com.vijay.cardkeeper.data.repository.PassportRepository,
    private val greenCardRepository: GreenCardRepository,
    private val aadharCardRepository: AadharCardRepository,
    private val giftCardRepository: GiftCardRepository,
    private val panCardRepository: PanCardRepository
) {

    suspend fun performMigration() {
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("app_meta", Context.MODE_PRIVATE)
            // Force migration to run again to fix any broken states
            // if (prefs.getBoolean("image_migration_complete", false)) {
            //     return@withContext
            // }
            android.util.Log.i("ImageMigration", "Starting Image Migration/Repair...")

            try {
                // ... (rest of the repository iteration logic - same as before)
                
                // Financial Accounts
                val accounts = financialRepository.allAccounts.first()
                accounts.forEach { account ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(account.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(account.backImagePath, filesToDelete)?.also { changed = true }
                    val newLogo = migratePath(account.logoImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        financialRepository.updateAccount(account.copy(
                            frontImagePath = newFront ?: account.frontImagePath,
                            backImagePath = newBack ?: account.backImagePath,
                            logoImagePath = newLogo ?: account.logoImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }
                
                // Identity Documents
                val docs = identityRepository.allDocuments.first()
                docs.forEach { doc ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(doc.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(doc.backImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        identityRepository.updateDocument(doc.copy(
                            frontImagePath = newFront ?: doc.frontImagePath,
                            backImagePath = newBack ?: doc.backImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }

                // Passports
                val passports = passportRepository.allPassports.first()
                passports.forEach { passport ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(passport.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(passport.backImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        passportRepository.update(passport.copy(
                            frontImagePath = newFront ?: passport.frontImagePath,
                            backImagePath = newBack ?: passport.backImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }
                
                // Green Cards
                val greenCards = greenCardRepository.allGreenCards.first()
                greenCards.forEach { gc ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(gc.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(gc.backImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        greenCardRepository.update(gc.copy(
                            frontImagePath = newFront ?: gc.frontImagePath,
                            backImagePath = newBack ?: gc.backImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }

                // Aadhar Cards
                val aadharCards = aadharCardRepository.allAadharCards.first()
                aadharCards.forEach { ac ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(ac.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(ac.backImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        aadharCardRepository.update(ac.copy(
                            frontImagePath = newFront ?: ac.frontImagePath,
                            backImagePath = newBack ?: ac.backImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }

                // Gift Cards
                val giftCards = giftCardRepository.getAllGiftCards().first()
                giftCards.forEach { gc ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(gc.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(gc.backImagePath, filesToDelete)?.also { changed = true }
                    val newLogo = migratePath(gc.logoImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        giftCardRepository.updateGiftCard(gc.copy(
                            frontImagePath = newFront ?: gc.frontImagePath,
                            backImagePath = newBack ?: gc.backImagePath,
                            logoImagePath = newLogo ?: gc.logoImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }

                // PAN Cards
                val panCards = panCardRepository.getAllPanCards().first()
                panCards.forEach { pc ->
                    val filesToDelete = mutableListOf<File>()
                    var changed = false
                    val newFront = migratePath(pc.frontImagePath, filesToDelete)?.also { changed = true }
                    val newBack = migratePath(pc.backImagePath, filesToDelete)?.also { changed = true }
                    
                    if (changed) {
                        panCardRepository.updatePanCard(pc.copy(
                            frontImagePath = newFront ?: pc.frontImagePath,
                            backImagePath = newBack ?: pc.backImagePath
                        ))
                        filesToDelete.forEach { try { it.delete() } catch(e: Exception) { e.printStackTrace() } }
                    }
                }

                // Mark complete
                prefs.edit().putBoolean("image_migration_complete", true).apply()
                android.util.Log.i("ImageMigration", "Migration/Repair completed successfully")

            } catch (e: Exception) {
                android.util.Log.e("ImageMigration", "Migration failed", e)
                e.printStackTrace()
            }
        }
    }

    private fun migratePath(path: String?, filesToDelete: MutableList<File>): String? {
        if (path.isNullOrEmpty()) return null
        // Skip if already encrypted
        if (path.endsWith(".enc")) return null
        
        val file = File(path)
        
        // Determine expected encrypted path
        val newPath = if (path.endsWith(".jpg")) {
            path.removeSuffix(".jpg") + ".enc"
        } else {
            "$path.enc"
        }
        val newFile = File(newPath)

        // Case 1: .jpg exists -> Normal Migration
        if (file.exists()) {
            try {
                encryptFile(file, newFile)
                if (newFile.exists() && newFile.length() > 0) {
                    filesToDelete.add(file)
                    return newPath
                }
            } catch (e: Exception) {
                android.util.Log.e("ImageMigration", "Failed to migrate file: $path", e)
                if (newFile.exists()) newFile.delete() // Cleanup partial
            }
        } 
        // Case 2: .jpg INVALID/MISSING but .enc EXISTS -> Repair DB
        else if (newFile.exists() && newFile.length() > 0) {
             android.util.Log.i("ImageMigration", "Found orphaned encrypted file, updating DB: $newPath")
             return newPath
        }

        return null
    }
}
