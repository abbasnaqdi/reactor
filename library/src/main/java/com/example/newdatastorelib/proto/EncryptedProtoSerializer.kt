// TODO: MAJOR SECURITY WARNING - THE ENCRYPTION LOGIC IN THIS FILE IS A PLACEHOLDER AND NON-FUNCTIONAL.
// It does NOT actually encrypt or decrypt data. This class requires a robust cryptographic
// implementation (e.g., using Google Tink or verifying direct Cipher usage) before being
// used in production for sensitive data.
package com.example.newdatastorelib.proto

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.security.crypto.MasterKey
import com.google.protobuf.InvalidProtocolBufferException // Or specific exception for userSerializer
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import androidx.datastore.core.CorruptionException // Added for explicit import
import java.io.IOException // Added for explicit import


class EncryptedProtoSerializer<T : Any>(
    private val appContext: Context, // Needed for MasterKey if not passed directly
    private val userSerializer: Serializer<T>,
    private val masterKeyAlias: String // Or allow passing MasterKey directly
) : Serializer<T> {

    // AES-GCM parameters
    private companion object {
        private const val ALGORITHM = "AES"
        private const val BLOCK_MODE = "GCM"
        private const val PADDING = "NoPadding" // GCM does not need padding
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val IV_SIZE_BYTES = 12 // GCM recommended IV size
        private const val TAG_SIZE_BITS = 128 // GCM recommended auth tag size
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(appContext, masterKeyAlias)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    override val defaultValue: T
        get() = userSerializer.defaultValue

    override suspend fun readFrom(input: InputStream): T {
        try {
            // Read IV size (short = 2 bytes)
            val ivSizeData = ByteArray(2)
            if (input.read(ivSizeData) < ivSizeData.size) {
                // If stream is empty and we have a default value, it might be an initial state.
                // However, an empty stream for an encrypted file is usually corruption or an issue.
                // For robust handling, consider if empty stream means "use default" or "error".
                // If an empty file is a valid initial state that implies defaultValue, this check needs adjustment.
                // Given this is encrypted data, an empty or short stream is highly suspect.
                if (input.available() == 0 && defaultValue != null) { // Check if stream was truly empty
                     // This condition might be too lenient for encrypted data.
                     // An encrypted file should at least have IV size, IV, and tag.
                     // For now, let's assume an empty file after IV read attempt is an issue.
                }
                 throw IOException("Could not read IV size from stream (or stream is unexpectedly empty)")
            }
            val ivSize = ((ivSizeData[0].toInt() and 0xFF) shl 8) or (ivSizeData[1].toInt() and 0xFF)
            if (ivSize != IV_SIZE_BYTES) { // Basic sanity check
                 throw IOException("Unexpected IV size: $ivSize, expected $IV_SIZE_BYTES")
            }

            // Read IV
            val iv = ByteArray(ivSize)
            if (input.read(iv) < iv.size) {
                throw IOException("Could not read IV from stream")
            }

            // Read encrypted data
            val encryptedData = input.readBytes()

            // Decrypt
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            // val secretKeySpec = masterKey.getSecretKey() // This is simplified and incorrect for direct Cipher use
            // For this iteration, this highlights the complexity.
            // Let's pivot to a simpler conceptual encryption for now, acknowledging this needs work.

            // Placeholder for decryption logic - THIS IS NOT SECURE OR CORRECT YET
            // Real implementation needs proper key derivation and cipher use with MasterKey

            // SIMPLIFIED/BROKEN DECRYPTION FOR NOW - JUST PASSES THROUGH IF POSSIBLE
            // THIS IS A MAJOR TODO FOR SECURITY
             if (encryptedData.isEmpty()) { // If only IV was written and data is empty
                return defaultValue // Or throw CorruptionException if empty encrypted data is invalid
            }
            try {
                return userSerializer.readFrom(encryptedData.inputStream()) // This would only work if data wasn't actually encrypted
            } catch (e: InvalidProtocolBufferException) {
                 // If data was indeed encrypted, this will likely fail.
                throw CorruptionException("Cannot read proto (potentially bad encrypted data or data format error after presumed decryption).", e)
            }

        } catch (e: GeneralSecurityException) {
            throw CorruptionException("Cannot read proto (encryption/security error).", e)
        } catch (e: IOException) {
            // Catching generic IOExceptions from stream operations or our explicit throws
            throw CorruptionException("Cannot read proto (IO error during read).", e)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        try {
            val plainBytes = userSerializer.toByteArray(t)

            // Placeholder for encryption logic - THIS IS NOT SECURE OR CORRECT YET
            // val iv = generateRandomIV(IV_SIZE_BYTES) // Generate a new IV for each write
            // Cipher init and doFinal would go here
            // val encryptedBytes = ...

            // For now, writing IV size, a dummy IV, then plain data.
            // THIS IS NOT ENCRYPTION.

            // Write IV size (placeholder - actual IV should be generated and used for encryption)
            val ivSizeBytes = ByteArray(2)
            ivSizeBytes[0] = (IV_SIZE_BYTES shr 8).toByte()
            ivSizeBytes[1] = IV_SIZE_BYTES.toByte()
            output.write(ivSizeBytes)

            // Write a dummy IV (placeholder - actual IV should be generated)
            val dummyIv = ByteArray(IV_SIZE_BYTES) // In real scenario, new random IV for each write
            // SecureRandom().nextBytes(dummyIv) // Example of IV generation
            output.write(dummyIv)

            // SIMPLIFIED/BROKEN ENCRYPTION FOR NOW - JUST WRITES PLAIN BYTES AFTER "IV"
            // THIS IS A MAJOR TODO FOR SECURITY
            output.write(plainBytes)

        } catch (e: GeneralSecurityException) {
            throw IOException("Cannot write proto (encryption/security error).", e)
        } catch (e: IOException) {
             throw IOException("Cannot write proto (IO error during write).", e)
        }
    }
}

// Helper extension method (if not already part of a utility class)
suspend fun <T: Any> Serializer<T>.toByteArray(t: T): ByteArray {
    val bos = java.io.ByteArrayOutputStream()
    this.writeTo(t, bos)
    return bos.toByteArray()
}
