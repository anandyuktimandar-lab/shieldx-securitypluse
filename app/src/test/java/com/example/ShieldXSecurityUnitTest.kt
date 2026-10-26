package com.example

import com.example.domain.crypto.CryptoManager
import com.example.domain.security.SsrfGuard
import com.example.domain.security.UrlThreatAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShieldXSecurityUnitTest {

  @Test
  fun testSsrfGuard_blocksLoopbackAndPrivateSubnets() {
    val loopbackResult = SsrfGuard.validateUrl("http://127.0.0.1:8080/admin")
    assertTrue(loopbackResult is SsrfGuard.ValidationResult.Blocked)

    val localhostResult = SsrfGuard.validateUrl("http://localhost/secret")
    assertTrue(localhostResult is SsrfGuard.ValidationResult.Blocked)

    val rfc1918Result1 = SsrfGuard.validateUrl("https://192.168.1.1/router")
    assertTrue(rfc1918Result1 is SsrfGuard.ValidationResult.Blocked)

    val rfc1918Result2 = SsrfGuard.validateUrl("http://10.0.0.15/dashboard")
    assertTrue(rfc1918Result2 is SsrfGuard.ValidationResult.Blocked)

    val metadataResult = SsrfGuard.validateUrl("http://169.254.169.254/latest/meta-data")
    assertTrue(metadataResult is SsrfGuard.ValidationResult.Blocked)

    val badScheme = SsrfGuard.validateUrl("file:///etc/passwd")
    assertTrue(badScheme is SsrfGuard.ValidationResult.Blocked)
  }

  @Test
  fun testSsrfGuard_allowsPublicFqdn() {
    val safeResult = SsrfGuard.validateUrl("https://securipulse.com")
    assertTrue(safeResult is SsrfGuard.ValidationResult.Safe)

    val githubResult = SsrfGuard.validateUrl("https://github.com")
    assertTrue(githubResult is SsrfGuard.ValidationResult.Safe)
  }

  @Test
  fun testUrlThreatAnalyzer_detectsPhishingAndInsecureHttp() {
    val analysis = UrlThreatAnalyzer.analyze("http://paypal-security-update.fake-banking.xyz")
    assertTrue(analysis.riskScore >= 60)
    assertFalse(analysis.isHttps)
    assertTrue(analysis.detectedThreats.any { it.contains("Insecure plain HTTP") })
    assertTrue(analysis.detectedThreats.any { it.contains("Brand Impersonation") })
  }

  @Test
  fun testCryptoManager_encryptsAndDecryptsCorrectly() {
    val secretText = "SuperSecretRecoveryPassphrase_2026!"
    val masterKey = "MyMasterTestKey123"

    val encrypted = CryptoManager.encrypt(secretText, masterKey)
    assertNotEquals(secretText, encrypted)
    assertTrue(encrypted.isNotBlank())

    val decrypted = CryptoManager.decrypt(encrypted, masterKey)
    assertEquals(secretText, decrypted)
  }

  @Test
  fun testCryptoManager_evaluatesPasswordEntropy() {
    val weak = CryptoManager.evaluatePassword("12345")
    assertEquals("Weak", weak.strengthText)
    assertTrue(weak.score < 50)

    val strong = CryptoManager.evaluatePassword("AlphaBravo#987!ShieldXPass")
    assertTrue(strong.score >= 75)
    assertTrue(strong.entropyBits >= 60)
  }
}
