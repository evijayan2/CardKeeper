package com.vijay.cardkeeper.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil3.ImageLoader
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import kotlinx.coroutines.runBlocking
import okio.buffer
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import coil3.asImage

class EncryptedImageDecoder(
    private val source: ImageSource,
    private val context: Context,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        val key = KeyManager.cachedPassphrase
            ?: throw IllegalStateException("Key not available for decryption")

        // Read the entire encrypted source into memory
        val encryptedBytes = source.source().readByteArray()
        if (encryptedBytes.size < 12) {
             throw IllegalStateException("Invalid encrypted data: too short")
        }

        // Extract IV
        val iv = encryptedBytes.copyOfRange(0, 12)
        val data = encryptedBytes.copyOfRange(12, encryptedBytes.size)

        // Decrypt
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        val secretKeySpec = SecretKeySpec(key, "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec)

        val decryptedBytes = try {
            cipher.doFinal(data)
        } catch (e: Exception) {
            android.util.Log.e("EncryptedImageDecoder", "Decryption failed", e)
            throw e
        }



        // Decode directly with BitmapFactory (bypassing ImageDecoder)
        val bitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)
            ?: throw IllegalStateException("BitmapFactory failed to decode decrypted bytes")

        return DecodeResult(
            image = BitmapDrawable(context.resources, bitmap).asImage(),
            isSampled = false
        )
    }

    class Factory(private val context: Context) : Decoder.Factory {
        override fun create(
            result: SourceFetchResult,
            options: Options,
            imageLoader: ImageLoader
        ): Decoder? {
            val isEncryptedMime = result.mimeType == "application/x-encrypted-cardkeeper"
            // Fallback: Check file extension if available in source path
            val isEncryptedExtension = result.source.file().toString().endsWith(".enc", ignoreCase = true)

            if (isEncryptedMime || isEncryptedExtension) {
                return EncryptedImageDecoder(result.source, context, options)
            }
            return null
        }
    }
}
