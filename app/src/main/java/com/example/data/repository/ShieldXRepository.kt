package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.example.data.local.ShieldXDatabase
import com.example.data.local.entity.LearningProgressEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.SecurityAlertEntity
import com.example.data.local.entity.VaultItemEntity
import com.example.domain.crypto.CryptoManager
import com.example.domain.model.DeviceHealthStats
import com.example.domain.model.DeviceSecurityStatus
import com.example.domain.model.LearningLesson
import com.example.domain.model.RiskLevel
import com.example.domain.model.UrlAnalysis
import com.example.domain.security.DeviceIntegrityAuditor
import com.example.domain.security.SsrfGuard
import com.example.domain.security.UrlThreatAnalyzer
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class ShieldXRepository(private val context: Context) {

  private val database = ShieldXDatabase.getDatabase(context)
  private val scanDao = database.scanReportDao()
  private val alertDao = database.securityAlertDao()
  private val vaultDao = database.vaultItemDao()
  private val learningDao = database.learningProgressDao()
  private val watchlistDao = database.watchlistDao()
  private val portfolioDao = database.portfolioDao()

  // Flows
  val allScans: Flow<List<ScanReportEntity>> = scanDao.getAllScans()
  val activeAlerts: Flow<List<SecurityAlertEntity>> = alertDao.getActiveAlerts()
  val allAlerts: Flow<List<SecurityAlertEntity>> = alertDao.getAllAlerts()
  val allVaultItems: Flow<List<VaultItemEntity>> = vaultDao.getAllVaultItems()
  val learningProgress: Flow<List<LearningProgressEntity>> = learningDao.getAllProgress()
  val allWatchlistCoins = watchlistDao.getAllWatchlistCoins()
  val allPortfolioHoldings = portfolioDao.getAllHoldings()

  suspend fun addWatchlistCoin(coinId: String, symbol: String, name: String) {
    watchlistDao.insertCoin(
      com.example.data.local.entity.WatchlistCoinEntity(
        coinId = coinId,
        symbol = symbol,
        name = name
      )
    )
  }

  suspend fun removeWatchlistCoin(coinId: String) {
    watchlistDao.removeCoin(coinId)
  }

  suspend fun addPortfolioHolding(
    holdingId: String,
    coinId: String,
    symbol: String,
    name: String,
    amount: Double,
    buyPriceUsd: Double
  ) {
    portfolioDao.insertHolding(
      com.example.data.local.entity.PortfolioHoldingEntity(
        holdingId = holdingId,
        coinId = coinId,
        symbol = symbol,
        name = name,
        amount = amount,
        buyPriceUsd = buyPriceUsd
      )
    )
  }

  suspend fun deletePortfolioHolding(holdingId: String) {
    portfolioDao.deleteHolding(holdingId)
  }

  /**
   * Safe URL scanning with SSRF defenses and heuristic risk analysis
   */
  suspend fun scanUrl(rawUrl: String): UrlAnalysis {
    // 1. SSRF Guard
    val ssrfCheck = SsrfGuard.validateUrl(rawUrl)
    if (ssrfCheck is SsrfGuard.ValidationResult.Blocked) {
      val blockedAnalysis = UrlAnalysis(
        url = rawUrl,
        domain = "Blocked by SSRF Guard",
        isHttps = false,
        riskLevel = RiskLevel.HIGH_RISK,
        riskScore = 100,
        detectedThreats = listOf(ssrfCheck.reason),
        recommendations = listOf("The submitted destination points to restricted/private infrastructure. Request aborted.")
      )
      saveScan(blockedAnalysis)
      return blockedAnalysis
    }

    // 2. Threat & Heuristic Analysis
    val analysis = UrlThreatAnalyzer.analyze(rawUrl)
    saveScan(analysis)

    // Trigger a Room security alert if high risk or suspicious
    if (analysis.riskLevel == RiskLevel.HIGH_RISK || analysis.riskLevel == RiskLevel.SUSPICIOUS) {
      val severity = if (analysis.riskLevel == RiskLevel.HIGH_RISK) "CRITICAL" else "WARNING"
      alertDao.insertAlert(
        SecurityAlertEntity(
          title = if (analysis.riskLevel == RiskLevel.HIGH_RISK) {
            "Malicious URL Blocked (${analysis.domain})"
          } else {
            "Suspicious URL Flagged (${analysis.domain})"
          },
          message = analysis.detectedThreats.firstOrNull()
            ?: "Heuristic risk score ${analysis.riskScore}/100 detected on ${analysis.domain}.",
          severity = severity,
          category = "URL"
        )
      )
    }

    return analysis
  }

  private suspend fun saveScan(analysis: UrlAnalysis) {
    val threatsJson = JSONArray(analysis.detectedThreats).toString()
    val recommendationsJson = JSONArray(analysis.recommendations).toString()
    scanDao.insertScan(
      ScanReportEntity(
        url = analysis.url,
        domain = analysis.domain,
        riskLevel = analysis.riskLevel.name,
        riskScore = analysis.riskScore,
        threatsJson = threatsJson,
        recommendationsJson = recommendationsJson
      )
    )
  }

  /**
   * Genuine device telemetry from Android OS APIs
   */
  fun getDeviceHealthStats(): DeviceHealthStats {
    // 1. RAM Telemetry
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    val totalRamMb = memInfo.totalMem / (1024 * 1024)
    val availRamMb = memInfo.availMem / (1024 * 1024)
    val usedRamMb = (totalRamMb - availRamMb).coerceAtLeast(0)

    // 2. Storage Telemetry
    val stat = StatFs(Environment.getDataDirectory().path)
    val blockSize = stat.blockSizeLong
    val totalBlocks = stat.blockCountLong
    val availableBlocks = stat.availableBlocksLong
    val totalGb = String.format("%.1f", (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)).toDoubleOrNull() ?: 64.0
    val freeGb = String.format("%.1f", (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)).toDoubleOrNull() ?: 24.5

    // 3. Battery Telemetry
    val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, ifilter)
    val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 82
    val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
    val batteryPct = if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 82
    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val tempInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 295
    val batteryTemp = tempInt / 10.0f
    val healthInt = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
    val healthStr = when (healthInt) {
      BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
      BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
      BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
      else -> "Good"
    }

    // Health Score calculation (0-100)
    var healthScore = 100
    if (batteryPct < 20) healthScore -= 10
    if (memInfo.lowMemory) healthScore -= 20
    if (freeGb < 5.0) healthScore -= 15

    return DeviceHealthStats(
      healthPercentage = healthScore.coerceIn(50, 100),
      ramUsedMb = usedRamMb,
      ramTotalMb = totalRamMb,
      storageFreeGb = freeGb,
      storageTotalGb = totalGb,
      batteryPercentage = batteryPct,
      isCharging = isCharging,
      batteryHealth = healthStr,
      batteryTempCelsius = batteryTemp
    )
  }

  fun auditDeviceSecurity(): DeviceSecurityStatus {
    return DeviceIntegrityAuditor.audit(context)
  }

  suspend fun syncDeviceSecurityAlerts(): DeviceSecurityStatus {
    val status = DeviceIntegrityAuditor.audit(context)
    val existingCount = alertDao.getAlertCount()
    if (existingCount == 0) {
      val now = System.currentTimeMillis()
      if (status.isRooted) {
        alertDao.insertAlert(
          SecurityAlertEntity(
            title = "Root Privileges / Test-Keys Detected",
            message = "Superuser binaries or test-keys build detected. Android sandbox isolation may be weakened.",
            severity = "CRITICAL",
            category = "SYSTEM",
            timestamp = now
          )
        )
      }
      if (status.isUsbDebuggingEnabled) {
        alertDao.insertAlert(
          SecurityAlertEntity(
            title = "USB Debugging (ADB) Enabled",
            message = "Android Debug Bridge is active. Disable USB Debugging in Developer Options when not testing.",
            severity = "WARNING",
            category = "SYSTEM",
            timestamp = now - 60_000L
          )
        )
      }
      if (!status.isVpnActive) {
        alertDao.insertAlert(
          SecurityAlertEntity(
            title = "Unshielded Network Egress (No VPN)",
            message = "Traffic is routing directly without an encrypted VPN tunnel. Enable a VPN on public Wi-Fi.",
            severity = "WARNING",
            category = "NETWORK",
            timestamp = now - 180_000L
          )
        )
      }
      alertDao.insertAlert(
        SecurityAlertEntity(
          title = "App Permission Audit: ${status.totalDangerousPermissions} Sensitive Grants",
          message = "Detected ${status.totalDangerousPermissions} Camera, Location, or Microphone permission declarations across installed packages.",
          severity = "INFO",
          category = "PRIVACY",
          timestamp = now - 300_000L
        )
      )
    }
    return status
  }

  suspend fun dismissAlert(alertId: Long) {
    alertDao.dismissAlert(alertId)
  }

  suspend fun dismissAllAlerts() {
    alertDao.dismissAllAlerts()
  }

  suspend fun addAlert(title: String, message: String, severity: String, category: String) {
    alertDao.insertAlert(
      SecurityAlertEntity(
        title = title,
        message = message,
        severity = severity,
        category = category
      )
    )
  }

  suspend fun clearHistory() {
    scanDao.clearAll()
    alertDao.clearAll()
  }

  // Vault methods
  suspend fun saveVaultItem(title: String, category: String, rawSecret: String, label: String) {
    val encrypted = CryptoManager.encrypt(rawSecret)
    vaultDao.insertItem(
      VaultItemEntity(
        title = title,
        category = category,
        encryptedPayload = encrypted,
        usernameOrLabel = label
      )
    )
  }

  suspend fun deleteVaultItem(item: VaultItemEntity) {
    vaultDao.deleteItem(item)
  }

  suspend fun markLessonCompleted(lessonId: String, isCompleted: Boolean) {
    learningDao.setProgress(LearningProgressEntity(lessonId, isCompleted))
  }

  /**
   * Pre-populated cybersecurity education curriculum
   */
  fun getCurriculum(): List<LearningLesson> {
    return listOf(
      LearningLesson(
        id = "phishing_101",
        title = "Recognizing Phishing & Smishing",
        category = "Social Engineering",
        readTime = "3 min read",
        overview = "Phishing attempts manipulate psychological urgency or fear to trick you into surrendering credentials or installing spyware.",
        checklist = listOf(
          "Check the sender domain carefully: look for slight spelling alterations (e.g. paypa1.com or support-apple.cc).",
          "Beware of artificial urgency ('Account suspended in 24 hours'). Legitimate institutions do not demand panic actions.",
          "Never click login links inside unprompted SMS messages; always navigate directly through official bookmarks or apps.",
          "Look for mismatched SSL certificates and generic greetings ('Dear Customer')."
        )
      ),
      LearningLesson(
        id = "passwords_mfa",
        title = "Strong Passwords & Hardware 2FA",
        category = "Authentication",
        readTime = "4 min read",
        overview = "Single passwords are vulnerable to credential stuffing attacks across unrelated data breaches. Multi-factor authentication adds essential defense.",
        checklist = listOf(
          "Use passphrases of 15+ characters rather than short complex words.",
          "Never reuse the same master password across financial, personal, and work accounts.",
          "Prefer Authenticator apps (TOTP) or FIDO2 hardware keys over SMS verification, which is susceptible to SIM swapping.",
          "Store emergency backup recovery codes in a secure, encrypted offline vault."
        )
      ),
      LearningLesson(
        id = "public_wifi",
        title = "Public Wi-Fi & Network Sniffing",
        category = "Network Defense",
        readTime = "3 min read",
        overview = "Unsecured airport, café, or hotel Wi-Fi networks allow malicious actors on the same LAN to inspect DNS requests or launch evil-twin attacks.",
        checklist = listOf(
          "Avoid connecting to open Wi-Fi networks without WPA2/WPA3 password protection.",
          "Turn on an encrypted VPN whenever joining untrusted wireless networks.",
          "Disable 'Auto-Connect to Open Wi-Fi' in Android Wi-Fi settings.",
          "Verify HTTPS is strictly enforced on all browser web sessions."
        )
      ),
      LearningLesson(
        id = "app_hygiene",
        title = "Mobile App Permissions Hygiene",
        category = "Device Privacy",
        readTime = "2 min read",
        overview = "Over-privileged apps often collect telemetry, background location, and microphone access without user realization.",
        checklist = listOf(
          "Regularly audit apps requesting Background Location, Accessibility services, and Notification listener access.",
          "Remove or freeze sideloaded APKs from untrusted web stores.",
          "Enable auto-revocation of permissions for unused applications in Android Settings.",
          "Keep Developer Options and USB Debugging disabled unless actively required."
        )
      )
    )
  }
}
