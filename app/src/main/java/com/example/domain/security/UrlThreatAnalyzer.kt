package com.example.domain.security

import com.example.domain.model.RiskLevel
import com.example.domain.model.UrlAnalysis
import java.net.URI

object UrlThreatAnalyzer {

  private val SUSPICIOUS_KEYWORDS = listOf(
    "login", "verify", "account", "update", "banking", "secure-signin",
    "wallet-connect", "claim-airdrop", "metamask", "seed-phrase", "billing-resolve",
    "security-alert", "password-reset", "suspicious-activity", "2fa-confirm",
    "paypal-resolution", "appleid-support", "netflix-reactivate"
  )

  private val COMMONLY_TARGETED_BRANDS = listOf(
    "paypal", "apple", "google", "microsoft", "amazon", "netflix",
    "chase", "wellsfargo", "binance", "coinbase", "facebook", "instagram"
  )

  private val HIGH_RISK_TLDS = setOf(
    "top", "xyz", "click", "buzz", "gq", "cf", "tk", "ml", "work", "loan", "rest"
  )

  fun analyze(rawUrl: String): UrlAnalysis {
    val cleanUrl = rawUrl.trim()
    val normalized = if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
      "https://$cleanUrl"
    } else {
      cleanUrl
    }

    val uri = try {
      URI(normalized)
    } catch (e: Exception) {
      return UrlAnalysis(
        url = cleanUrl,
        domain = "Invalid",
        isHttps = false,
        riskLevel = RiskLevel.HIGH_RISK,
        riskScore = 95,
        detectedThreats = listOf("Malformed URL syntax prevents secure verification."),
        recommendations = listOf("Do not visit this URL. Malformed strings can exploit parser differentials.")
      )
    }

    val host = uri.host?.lowercase() ?: ""
    val isHttps = uri.scheme?.equals("https", ignoreCase = true) == true
    val detectedThreats = mutableListOf<String>()
    val recommendations = mutableListOf<String>()
    var riskPoints = 0

    // 1. HTTP vs HTTPS
    if (!isHttps) {
      riskPoints += 25
      detectedThreats.add("Insecure plain HTTP connection (data is sent unencrypted over the network)")
      recommendations.add("Avoid entering credentials, payment info, or personal details.")
    }

    // 2. Direct IP Address Hostname
    if (SsrfGuard.isIpAddress(host)) {
      riskPoints += 45
      detectedThreats.add("URL targets raw IP address ($host) rather than a registered domain name")
      recommendations.add("Legitimate commercial services rarely use bare IP addresses.")
    }

    // 3. Punycode / Internationalized Domain Name Spoofing
    if (host.startsWith("xn--") || host.contains(".xn--")) {
      riskPoints += 50
      detectedThreats.add("Punycode detected: Potential homoglyph / visual lookalike character attack")
      recommendations.add("Check the decoded ASCII characters carefully for Cyrillic or Greek character spoofing.")
    }

    // 4. Excessive Subdomain Stacking
    val hostParts = host.split(".")
    if (hostParts.size > 4) {
      riskPoints += 30
      detectedThreats.add("Excessive subdomain depth (${hostParts.size} parts): Often used to disguise true registrar domain")
    }

    // 5. High-Risk TLD with sensitive keywords
    val tld = hostParts.lastOrNull() ?: ""
    if (HIGH_RISK_TLDS.contains(tld)) {
      riskPoints += 20
      detectedThreats.add("Domain uses a top-level domain (.$tld) frequently exploited for disposable phishing campaigns")
    }

    // 6. Brand Impersonation & Phishing Keywords
    val fullUrlLower = normalized.lowercase()
    val foundBrand = COMMONLY_TARGETED_BRANDS.firstOrNull { fullUrlLower.contains(it) }
    val foundKeyword = SUSPICIOUS_KEYWORDS.firstOrNull { fullUrlLower.contains(it) }

    if (foundBrand != null) {
      // Check if it's the actual official domain
      val isLegitimateOfficial = host.endsWith("$foundBrand.com") || host.endsWith("$foundBrand.org") || host == "$foundBrand.com"
      if (!isLegitimateOfficial) {
        riskPoints += 55
        detectedThreats.add("Suspected Brand Impersonation: Mentions '$foundBrand' but registered on unrelated domain '$host'")
        recommendations.add("Navigate directly to the official $foundBrand website rather than following external links.")
      }
    }

    if (foundKeyword != null && (riskPoints > 20 || !isHttps)) {
      riskPoints += 25
      detectedThreats.add("Contains sensitive authentication keyword: '$foundKeyword'")
    }

    // Determine final Risk Level and Normalized Score
    val finalScore = riskPoints.coerceIn(0, 100)
    val riskLevel = when {
      finalScore >= 60 -> RiskLevel.HIGH_RISK
      finalScore >= 30 -> RiskLevel.SUSPICIOUS
      else -> RiskLevel.SAFE
    }

    if (detectedThreats.isEmpty()) {
      detectedThreats.add("No immediate deceptive patterns, punycode spoofing, or known phishing triggers found.")
      recommendations.add("While no known heuristic risks were detected, always verify the site identity and address bar before entering passwords.")
    }

    return UrlAnalysis(
      url = cleanUrl,
      domain = host,
      isHttps = isHttps,
      riskLevel = riskLevel,
      riskScore = finalScore,
      detectedThreats = detectedThreats,
      recommendations = recommendations
    )
  }
}
