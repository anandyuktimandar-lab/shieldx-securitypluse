package com.example.domain.crypto

import java.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.log2

object CryptoManager {

  private const val ALGORITHM = "AES"
  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val GCM_TAG_LENGTH = 128
  private const val GCM_IV_LENGTH = 12
  private const val ITERATIONS = 10000
  private const val KEY_LENGTH = 256

  private val secureRandom = SecureRandom()

  /**
   * Encrypts plaintext using AES-256-GCM.
   * Returns a base64 encoded string containing: [IV(12 bytes) + Salt(16 bytes) + Ciphertext].
   */
  fun encrypt(plainText: String, masterPass: String = "ShieldXDefaultKey"): String {
    val salt = ByteArray(16).apply { secureRandom.nextBytes(this) }
    val iv = ByteArray(GCM_IV_LENGTH).apply { secureRandom.nextBytes(this) }

    val keySpec = PBEKeySpec(masterPass.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, ALGORITHM)

    val cipher = Cipher.getInstance(TRANSFORMATION)
    val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)

    val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

    val combined = ByteArray(iv.size + salt.size + cipherText.size)
    System.arraycopy(iv, 0, combined, 0, iv.size)
    System.arraycopy(salt, 0, combined, iv.size, salt.size)
    System.arraycopy(cipherText, 0, combined, iv.size + salt.size, cipherText.size)

    return Base64.getEncoder().encodeToString(combined)
  }

  /**
   * Decrypts an AES-256-GCM payload.
   */
  fun decrypt(encryptedBase64: String, masterPass: String = "ShieldXDefaultKey"): String {
    return try {
      val combined = Base64.getDecoder().decode(encryptedBase64)
      val iv = ByteArray(GCM_IV_LENGTH)
      val salt = ByteArray(16)
      val cipherTextSize = combined.size - iv.size - salt.size

      System.arraycopy(combined, 0, iv, 0, iv.size)
      System.arraycopy(combined, iv.size, salt, 0, salt.size)
      val cipherText = ByteArray(cipherTextSize)
      System.arraycopy(combined, iv.size + salt.size, cipherText, 0, cipherTextSize)

      val keySpec = PBEKeySpec(masterPass.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
      val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
      val secretKey = SecretKeySpec(factory.generateSecret(keySpec).encoded, ALGORITHM)

      val cipher = Cipher.getInstance(TRANSFORMATION)
      val parameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)

      val decrypted = cipher.doFinal(cipherText)
      String(decrypted, Charsets.UTF_8)
    } catch (e: Exception) {
      "[Decryption Failed - Check Master Password]"
    }
  }

  data class PasswordEvaluation(
    val score: Int, // 0 - 100
    val strengthText: String,
    val entropyBits: Int,
    val feedback: List<String>
  )

  fun evaluatePassword(password: String): PasswordEvaluation {
    if (password.isEmpty()) {
      return PasswordEvaluation(0, "Empty", 0, listOf("Enter a password to evaluate."))
    }

    var poolSize = 0
    if (password.any { it.isLowerCase() }) poolSize += 26
    if (password.any { it.isUpperCase() }) poolSize += 26
    if (password.any { it.isDigit() }) poolSize += 10
    if (password.any { !it.isLetterOrDigit() }) poolSize += 33

    val entropy = if (poolSize > 0) {
      (password.length * log2(poolSize.toDouble())).toInt()
    } else 0

    val feedback = mutableListOf<String>()
    if (password.length < 12) {
      feedback.add("Increase length to at least 14 characters for modern brute-force resilience.")
    }
    if (!password.any { it.isUpperCase() }) {
      feedback.add("Add uppercase letters.")
    }
    if (!password.any { it.isDigit() }) {
      feedback.add("Add numbers.")
    }
    if (!password.any { !it.isLetterOrDigit() }) {
      feedback.add("Add special symbols (e.g. !@#\$%^&*).")
    }

    val score = when {
      entropy >= 70 && password.length >= 14 -> 100
      entropy >= 50 && password.length >= 10 -> 75
      entropy >= 35 -> 50
      else -> 25
    }

    val text = when {
      score >= 90 -> "Very Strong"
      score >= 70 -> "Strong"
      score >= 45 -> "Moderate"
      else -> "Weak"
    }

    if (feedback.isEmpty()) {
      feedback.add("Excellent password complexity and entropy.")
    }

    return PasswordEvaluation(score, text, entropy, feedback)
  }
}
