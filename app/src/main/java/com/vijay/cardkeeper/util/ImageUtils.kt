package com.vijay.cardkeeper.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream


fun resizeBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
    val originalWidth = bitmap.width
    val originalHeight = bitmap.height
    var newWidth = originalWidth
    var newHeight = originalHeight

    if (originalWidth > maxDimension || originalHeight > maxDimension) {
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        if (originalWidth > originalHeight) {
            newWidth = maxDimension
            newHeight = (newWidth / aspectRatio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (newHeight * aspectRatio).toInt()
        }
    }
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, name: String): String {
    val directory = context.getDir("card_images", Context.MODE_PRIVATE)
    // Use .enc extension for encrypted files
    val file = File(directory, "$name.enc")
    
    // Resize to max 1280px to balance quality and memory
    val resizedBitmap = resizeBitmap(bitmap, 1280)
    
    val key = KeyManager.cachedPassphrase 
        ?: throw IllegalStateException("Key not available for encryption")

    // Generate random IV
    val iv = ByteArray(12)
    java.security.SecureRandom().nextBytes(iv)

    val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
    val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
    val secretKeySpec = javax.crypto.spec.SecretKeySpec(key, "AES")
    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec, spec)

    FileOutputStream(file).use { fos ->
        // Write IV first
        fos.write(iv)
        // Write Encrypted Data
        javax.crypto.CipherOutputStream(fos, cipher).use { cos ->
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, cos)
        }
    }
    return file.absolutePath
}

fun encryptFile(input: File, output: File) {
    val key = KeyManager.cachedPassphrase
        ?: throw IllegalStateException("Key not available for encryption")

    val iv = ByteArray(12)
    java.security.SecureRandom().nextBytes(iv)

    val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
    val spec = javax.crypto.spec.GCMParameterSpec(128, iv)
    val secretKeySpec = javax.crypto.spec.SecretKeySpec(key, "AES")
    cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec, spec)

    FileOutputStream(output).use { fos ->
        fos.write(iv)
        javax.crypto.CipherOutputStream(fos, cipher).use { cos ->
            input.inputStream().use { fis ->
                val buffer = ByteArray(8 * 1024) // 8KB buffer
                var bytesRead: Int
                while (fis.read(buffer).also { bytesRead = it } != -1) {
                    cos.write(buffer, 0, bytesRead)
                }
            }
        }
    }
}
