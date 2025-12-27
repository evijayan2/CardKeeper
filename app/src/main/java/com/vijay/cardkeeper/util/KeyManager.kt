package com.vijay.cardkeeper.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

object KeyManager {

    private const val PREF_FILE_NAME = "secure_prefs"
    private const val KEY_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
    private const val KEY_IV = "iv_db_passphrase"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "cardkeeper_master_key"

    @Volatile
    var cachedPassphrase: ByteArray? = null
        private set

    fun isKeyCreated(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_meta", Context.MODE_PRIVATE)
        return prefs.getBoolean("security_setup_complete", false)
    }

    /**
     * Retrieves the database passphrase. MUST be called only after successful User Authentication
     * (Biometric/PIN) because the Keystore key requires it.
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        synchronized(this) {
            cachedPassphrase?.let {
                return it
            }

            val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
            val encryptedKeyBase64 = prefs.getString(KEY_ENCRYPTED_PASSPHRASE, null)
            val ivBase64 = prefs.getString(KEY_IV, null)

            if (encryptedKeyBase64 != null && ivBase64 != null) {
                // Decrypt existing key
                val encryptedKey = Base64.decode(encryptedKeyBase64, Base64.DEFAULT)
                val iv = Base64.decode(ivBase64, Base64.DEFAULT)
                val key = decryptKey(encryptedKey, iv)
                cachedPassphrase = key
                return key
            } else {
                // Generate and encrypt new key
                val random = SecureRandom()
                val keyBytes = ByteArray(32) // 256-bit DB key
                random.nextBytes(keyBytes)

                val (encryptedBytes, iv) = encryptKey(keyBytes)

                prefs.edit()
                        .putString(
                                KEY_ENCRYPTED_PASSPHRASE,
                                Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
                        )
                        .putString(KEY_IV, Base64.encodeToString(iv, Base64.DEFAULT))
                        .apply()

                context.getSharedPreferences("app_meta", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("security_setup_complete", true)
                        .apply()

                cachedPassphrase = keyBytes
                return keyBytes
            }
        }
    }

    private fun getMasterKey(): java.security.Key {
        val keyStore = java.security.KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                    KeyGenerator.getInstance(
                            android.security.keystore.KeyProperties.KEY_ALGORITHM_AES,
                            KEYSTORE_PROVIDER
                    )
            val keyGenParameterSpec =
                    android.security.keystore.KeyGenParameterSpec.Builder(
                                    KEY_ALIAS,
                                    android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                                            android.security.keystore.KeyProperties.PURPOSE_DECRYPT
                            )
                            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(
                                    android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE
                            )
                            .setUserAuthenticationRequired(true)
                            .apply {
                                if (android.os.Build.VERSION.SDK_INT >=
                                                android.os.Build.VERSION_CODES.R
                                ) {
                                    setUserAuthenticationParameters(
                                            30,
                                            android.security.keystore.KeyProperties
                                                    .AUTH_BIOMETRIC_STRONG or
                                                    android.security.keystore.KeyProperties
                                                            .AUTH_DEVICE_CREDENTIAL
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    setUserAuthenticationValidityDurationSeconds(30)
                                }
                            }
                            .setKeySize(256)
                            .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_ALIAS, null)
    }

    private fun encryptKey(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return Pair(encryptedData, iv)
    }

    private fun decryptKey(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec)
        return cipher.doFinal(encryptedData)
    }
}
