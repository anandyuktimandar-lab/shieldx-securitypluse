package com.example

import com.example.domain.crypto.CryptoManager
import com.example.domain.model.RiskLevel
import com.example.domain.security.SsrfGuard
import com.example.domain.security.UrlThreatAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun ssrfGuard_blocksPrivateAndLoopbackAddresses() {
    val loopback = SsrfGuard.validateUrl("http://127.0.0.1:8080/admin")
    assertTrue(loopback is SsrfGuard.ValidationResult.Blocked)

    val metadata = SsrfGuard.validateUrl("http://169.254.169.254/latest/meta-data/")
    assertTrue(metadata is SsrfGuard.ValidationResult.Blocked)
  }

  @Test
  fun ssrfGuard_allowsPublicHttpsUrl() {
    val pub = SsrfGuard.validateUrl("https://api.coingecko.com/api/v3/ping")
    assertTrue(pub is SsrfGuard.ValidationResult.Safe)
  }

  @Test
  fun urlThreatAnalyzer_detectsHighRiskPhishingUrlAndSafeHttps() {
    val phishing = UrlThreatAnalyzer.analyze("http://paypa1-secure-verify-account.tk/login")
    assertEquals(RiskLevel.HIGH_RISK, phishing.riskLevel)

    val safe = UrlThreatAnalyzer.analyze("https://developer.android.com/jetpack/compose")
    assertEquals(RiskLevel.SAFE, safe.riskLevel)
  }

  @Test
  fun cryptoManager_encryptsAndDecryptsRoundTrip() {
    val secret = "ShieldX-Vault-Recovery-Seed-2026"
    val encrypted = CryptoManager.encrypt(secret)
    val decrypted = CryptoManager.decrypt(encrypted)
    assertEquals(secret, decrypted)
  }
}
