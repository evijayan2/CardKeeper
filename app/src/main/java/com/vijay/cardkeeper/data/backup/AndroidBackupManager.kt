package com.vijay.cardkeeper.data.backup

import android.content.Context
import android.net.Uri
import com.vijay.cardkeeper.data.model.BackupData
import com.vijay.cardkeeper.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class AndroidBackupManager(
    private val context: Context,
    private val container: AppContainer
) {

    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }

    suspend fun exportData(password: String, outputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Gather Data (using correct Repository APIs)
            val backupData = BackupData(
                timestamp = System.currentTimeMillis(),
                aadharCards = container.aadharCardRepository.allAadharCards.first(),
                financialAccounts = container.financialRepository.allAccounts.first(),
                giftCards = container.giftCardRepository.getAllGiftCards().first(),
                greenCards = container.greenCardRepository.allGreenCards.first(),
                identityDocuments = container.identityRepository.allDocuments.first(),
                insuranceCards = container.insuranceCardRepository.allInsuranceCards.first(),
                panCards = container.panCardRepository.allPanCards.first(),
                passports = container.passportRepository.allPassports.first(),
                rewardCards = container.rewardCardRepository.getAllRewardCards().first()
            )

            // 2. Serialize JSON
            val jsonString = json.encodeToString(backupData)

            // 3. Prepare Encryption
            val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
            val iv = ByteArray(12).apply { SecureRandom().nextBytes(this) } // 12 bytes for GCM
            val key = deriveKey(password, salt)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, spec)

            // 4. Open Output Stream
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                // Write Salt (16) and IV (12) plain at the start
                outputStream.write(salt)
                outputStream.write(iv)

                // Create Cipher Stream
                CipherOutputStream(outputStream, cipher).use { cipherStream ->
                    ZipOutputStream(BufferedOutputStream(cipherStream)).use { zipOut ->
                        
                        // Default compression method
                        zipOut.setLevel(java.util.zip.Deflater.BEST_COMPRESSION)

                        // Entry 1: data.json
                        val jsonEntry = ZipEntry("data.json")
                        zipOut.putNextEntry(jsonEntry)
                        zipOut.write(jsonString.toByteArray(Charsets.UTF_8))
                        zipOut.closeEntry()

                        // Entry 2: Images
                        val imagePaths = collectImagePaths(backupData)
                        
                        for (path in imagePaths) {
                            val file = resolveFile(path)
                            if (file != null && file.exists()) {
                                // Decrypt file content to memory
                                val decryptedBytes = decryptImageFile(file)
                                if (decryptedBytes != null) {
                                    // Store with .jpg extension inside zip
                                    val entryName = "images/${file.name.removeSuffix(".enc")}.jpg"
                                    zipOut.putNextEntry(ZipEntry(entryName))
                                    zipOut.write(decryptedBytes)
                                    zipOut.closeEntry()
                                }
                            }
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importData(password: String, inputUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Open Input Stream
             context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                // Read headers
                val salt = ByteArray(16)
                if (inputStream.read(salt) != 16) throw Exception("Invalid backup file (too short)")
                val iv = ByteArray(12)
                if (inputStream.read(iv) != 12) throw Exception("Invalid backup file (too short)")

                // Init Cipher
                val key = deriveKey(password, salt)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)

                CipherInputStream(inputStream, cipher).use { cipherStream ->
                    ZipInputStream(cipherStream).use { zipIn ->
                        
                        var entry = zipIn.nextEntry
                        var backupData: BackupData? = null
                        val tempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}")
                        tempDir.mkdirs()

                        try {
                            while (entry != null) {
                                if (entry.name == "data.json") {
                                    // Read JSON
                                    // Note: zipIn.readBytes() might close stream or behave oddly in some versions,
                                    // safer to use copyTo
                                    val jsonFile = File(tempDir, "data.json")
                                    FileOutputStream(jsonFile).use { zipIn.copyTo(it) }
                                    val jsonString = jsonFile.readText()
                                    backupData = json.decodeFromString(jsonString)
                                } else if (entry.name.startsWith("images/")) {
                                    // Extract Image
                                    val fileName = File(entry.name).name // "xy.jpg"
                                    val tempFile = File(tempDir, fileName)
                                    FileOutputStream(tempFile).use { zipIn.copyTo(it) }
                                }
                                zipIn.closeEntry()
                                entry = zipIn.nextEntry
                            }

                            if (backupData == null) {
                                throw Exception("Invalid Backup: Missing data.json")
                            }

                            // 2. Restore Images (Encrypt and move to app_card_images)
                            val destDir = context.getDir("card_images", Context.MODE_PRIVATE)
                            if (!destDir.exists()) destDir.mkdirs()

                            // Iterate images in tempDir
                            tempDir.listFiles { _, name -> name != "data.json" }?.forEach { tempImg ->
                                // tempImg is "xyz.jpg" (plaintext)
                                // We need "xyz.enc" (encrypted) in destDir
                                val originalName = tempImg.nameWithoutExtension + ".enc"
                                val destFile = File(destDir, originalName)
                                
                                // Encrypt using Utils
                                com.vijay.cardkeeper.util.encryptFile(tempImg, destFile)
                            }

                            // 3. Clear Existing Data & Insert New Data
                            restoreRepositories(backupData!!)

                        } finally {
                            tempDir.deleteRecursively()
                        }
                    }
                }
            } ?: return@withContext Result.failure(Exception("Cannot open file stream"))
            
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun restoreRepositories(data: BackupData) {
        // Aadhar
        with(container.aadharCardRepository) {
            allAadharCards.first().forEach { delete(it) }
            data.aadharCards.forEach { insert(it) }
        }
        // Financial
        with(container.financialRepository) {
            allAccounts.first().forEach { deleteAccount(it) }
            data.financialAccounts.forEach { insertAccount(it) }
        }
        // Gift
        with(container.giftCardRepository) {
            getAllGiftCards().first().forEach { deleteGiftCard(it) }
            data.giftCards.forEach { insertGiftCard(it) }
        }
        // Green
        with(container.greenCardRepository) {
            allGreenCards.first().forEach { delete(it) }
            data.greenCards.forEach { insert(it) }
        }
        // Identity
        with(container.identityRepository) {
            allDocuments.first().forEach { deleteDocument(it) }
            data.identityDocuments.forEach { insertDocument(it) }
        }
        // Insurance
        with(container.insuranceCardRepository) {
            allInsuranceCards.first().forEach { delete(it) }
            data.insuranceCards.forEach { insert(it) }
        }
        // PAN
        with(container.panCardRepository) {
            allPanCards.first().forEach { delete(it) }
            data.panCards.forEach { insert(it) }
        }
        // Passport
        with(container.passportRepository) {
            allPassports.first().forEach { delete(it) }
            data.passports.forEach { insert(it) }
        }
        // Reward
        with(container.rewardCardRepository) {
            getAllRewardCards().first().forEach { deleteRewardCard(it) }
            data.rewardCards.forEach { insertRewardCard(it) }
        }
    }

    private fun collectImagePaths(data: BackupData): Set<String> {
        val paths = mutableSetOf<String>()
        fun add(p: String?) { p?.let { paths.add(it) } }

        data.identityDocuments.forEach { add(it.frontImagePath); add(it.backImagePath) }
        data.financialAccounts.forEach { add(it.frontImagePath); add(it.backImagePath); add(it.logoImagePath) }
        data.aadharCards.forEach { add(it.frontImagePath); add(it.backImagePath) } 
        data.giftCards.forEach { add(it.frontImagePath); add(it.backImagePath); add(it.logoImagePath) }
        data.greenCards.forEach { add(it.frontImagePath); add(it.backImagePath) }
        data.insuranceCards.forEach { add(it.frontImagePath); add(it.backImagePath) }
        data.panCards.forEach { add(it.frontImagePath); add(it.backImagePath) }
        data.passports.forEach { add(it.frontImagePath); add(it.backImagePath) }
        data.rewardCards.forEach { add(it.frontImagePath); add(it.backImagePath); add(it.logoImagePath) }

        return paths
    }

    private fun resolveFile(path: String): File? {
        val file = File(path)
        if (file.isAbsolute && file.exists()) return file
        
        val appDir = context.getDir("card_images", Context.MODE_PRIVATE)
        val localFile = File(appDir, file.name)
        if (localFile.exists()) return localFile
        
        return null
    }

    private fun decryptImageFile(file: File): ByteArray? {
        val key = com.vijay.cardkeeper.util.KeyManager.cachedPassphrase ?: return null

        return try {
            val content = file.readBytes()
            if (content.size < 12) return content // Not encrypted or invalid

            val iv = content.copyOfRange(0, 12)
            val data = content.copyOfRange(12, content.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            val secretKeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec)

            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null // Skip bad files
        }
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
