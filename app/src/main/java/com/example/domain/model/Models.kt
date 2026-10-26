package com.example.domain.model

enum class RiskLevel {
  SAFE,
  SUSPICIOUS,
  HIGH_RISK,
  UNKNOWN
}

data class UrlAnalysis(
  val url: String,
  val domain: String,
  val isHttps: Boolean,
  val riskLevel: RiskLevel,
  val riskScore: Int, // 0 - 100 (higher = riskier)
  val detectedThreats: List<String>,
  val recommendations: List<String>,
  val timestamp: Long = System.currentTimeMillis()
)

data class DeviceSecurityStatus(
  val isRooted: Boolean,
  val isUsbDebuggingEnabled: Boolean,
  val isDeveloperOptionsEnabled: Boolean,
  val isVpnActive: Boolean,
  val isWifiEncrypted: Boolean,
  val totalDangerousPermissions: Int,
  val overallScore: Int, // 0 - 100
  val statusSummary: String,
  val flags: List<SecurityFlag>
)

data class SecurityFlag(
  val title: String,
  val description: String,
  val isWarning: Boolean,
  val recommendation: String
)

data class DeviceHealthStats(
  val healthPercentage: Int,
  val ramUsedMb: Long,
  val ramTotalMb: Long,
  val storageFreeGb: Double,
  val storageTotalGb: Double,
  val batteryPercentage: Int,
  val isCharging: Boolean,
  val batteryHealth: String,
  val batteryTempCelsius: Float
)

data class CryptoAddressCheck(
  val address: String,
  val network: String,
  val isValidFormat: Boolean,
  val isKnownPhishingDrainer: Boolean,
  val safetyAdvice: String
)

data class LearningLesson(
  val id: String,
  val title: String,
  val category: String,
  val readTime: String,
  val overview: String,
  val checklist: List<String>,
  val isCompleted: Boolean = false
)

enum class FeatureCategory {
  ALL,
  SECURITY,
  PRIVACY,
  UTILITIES,
  CRYPTO
}

data class FeatureItem(
  val id: String,
  val title: String,
  val description: String,
  val category: FeatureCategory,
  val iconName: String,
  val isEnabled: Boolean = true
)
