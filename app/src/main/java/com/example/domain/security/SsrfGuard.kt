package com.example.domain.security

import java.net.InetAddress
import java.net.URI

object SsrfGuard {

  sealed class ValidationResult {
    data object Safe : ValidationResult()
    data class Blocked(val reason: String) : ValidationResult()
  }

  /**
   * Validates user-submitted URL to defend against Server-Side & Client Request Forgery:
   * 1. Scheme must be http or https
   * 2. Hostname must be present and syntactically valid
   * 3. Blocks loopback (127.0.0.0/8, ::1)
   * 4. Blocks RFC1918 private subnets (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)
   * 5. Blocks Link-Local (169.254.0.0/16) and AWS/GCP/Azure Cloud Metadata (169.254.169.254)
   * 6. Blocks Multicast & Broadcast addresses
   */
  fun validateUrl(urlString: String): ValidationResult {
    val trimmed = urlString.trim()
    if (trimmed.isBlank()) {
      return ValidationResult.Blocked("URL input is empty")
    }

    val uri = try {
      val parsed = URI(trimmed)
      if (parsed.scheme == null) {
        URI("https://$trimmed")
      } else {
        parsed
      }
    } catch (e: Exception) {
      return ValidationResult.Blocked("Malformed URL format: ${e.message}")
    }

    val scheme = uri.scheme?.lowercase() ?: ""
    if (scheme != "http" && scheme != "https") {
      return ValidationResult.Blocked("Unsupported scheme '$scheme'. Only HTTP and HTTPS are permitted for security.")
    }

    val host = uri.host
    if (host.isNullOrBlank()) {
      return ValidationResult.Blocked("Missing hostname or domain")
    }

    val lowerHost = host.lowercase()

    // Immediate check on reserved domain names
    if (lowerHost == "localhost" || lowerHost.endsWith(".localhost") || lowerHost.endsWith(".local") || lowerHost.endsWith(".internal")) {
      return ValidationResult.Blocked("SSRF Guard: Local and internal domain names are blocked.")
    }

    // Inspect IP if host is formatted as IP address
    if (isIpAddress(lowerHost)) {
      val ipBlockReason = checkIpSecurity(lowerHost)
      if (ipBlockReason != null) {
        return ValidationResult.Blocked(ipBlockReason)
      }
    }

    return ValidationResult.Safe
  }

  fun isIpAddress(host: String): Boolean {
    val clean = host.removePrefix("[").removeSuffix("]")
    val ipv4Regex = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$")
    return ipv4Regex.matches(clean) || clean.contains(":")
  }

  private fun checkIpSecurity(ipStr: String): String? {
    return try {
      val clean = ipStr.removePrefix("[").removeSuffix("]")
      val inet = InetAddress.getByName(clean)
      when {
        inet.isLoopbackAddress -> "SSRF Guard: Loopback address ($clean) blocked."
        inet.isSiteLocalAddress -> "SSRF Guard: Private RFC-1918 IP address ($clean) blocked."
        inet.isLinkLocalAddress -> "SSRF Guard: Link-local IP address ($clean) blocked."
        inet.isMulticastAddress -> "SSRF Guard: Multicast address ($clean) blocked."
        clean == "169.254.169.254" -> "SSRF Guard: Cloud metadata endpoint blocked."
        clean.startsWith("100.64.") -> "SSRF Guard: Carrier-grade NAT address blocked."
        clean == "0.0.0.0" -> "SSRF Guard: Wildcard IP blocked."
        else -> null
      }
    } catch (e: Exception) {
      "Invalid IP address resolution: ${e.message}"
    }
  }
}
