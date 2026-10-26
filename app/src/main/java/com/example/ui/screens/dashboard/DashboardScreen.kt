package com.example.ui.screens.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SecurityAlertEntity
import com.example.domain.model.DeviceHealthStats
import com.example.domain.model.DeviceSecurityStatus
import com.example.domain.model.RiskLevel
import com.example.ui.components.CyberTopBar
import com.example.ui.components.GlowingShield
import com.example.ui.components.MetricTilesRow
import com.example.ui.components.RiskBadge
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusPill
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DashboardScanEntry(
  val id: String,
  val domain: String,
  val url: String,
  val riskLevel: RiskLevel,
  val riskScore: Int,
  val timestampMillis: Long,
  val sourceLabel: String
)

private data class ActivePermissionItem(
  val name: String,
  val permissionCode: String,
  val purpose: String,
  val isGranted: Boolean,
  val isRuntime: Boolean,
  val icon: ImageVector
)

@Composable
fun DashboardScreen(
  viewModel: MainViewModel,
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  val deviceSecurity by viewModel.deviceSecurity.collectAsStateWithLifecycle()
  val deviceHealth by viewModel.deviceHealth.collectAsStateWithLifecycle()
  val allScans by viewModel.allScans.collectAsStateWithLifecycle()
  val cloudScans by viewModel.cloudScans.collectAsStateWithLifecycle()
  val activeAlerts by viewModel.activeAlerts.collectAsStateWithLifecycle()
  val isQuickScanning by viewModel.isQuickScanning.collectAsStateWithLifecycle()

  // Merge recent URL scans from local Room and Cloud Firestore
  val recentScanEntries = remember(allScans, cloudScans) {
    val localMapped = allScans.map { entity ->
      DashboardScanEntry(
        id = "local_${entity.id}",
        domain = entity.domain.ifBlank { entity.url },
        url = entity.url,
        riskLevel = parseRiskLevel(entity.riskLevel),
        riskScore = entity.riskScore,
        timestampMillis = entity.timestamp,
        sourceLabel = "On-Device"
      )
    }
    val cloudMapped = cloudScans.map { doc ->
      DashboardScanEntry(
        id = "cloud_${doc.id}",
        domain = doc.domain.ifBlank { doc.url },
        url = doc.url,
        riskLevel = parseRiskLevel(doc.riskLevel),
        riskScore = doc.riskScore,
        timestampMillis = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
        sourceLabel = "Cloud Sync"
      )
    }
    (localMapped + cloudMapped)
      .distinctBy { "${it.url}_${it.riskScore}" }
      .sortedByDescending { it.timestampMillis }
      .take(3)
  }

  // Live permission checks
  val isMicGranted = ContextCompat.checkSelfPermission(
    context,
    Manifest.permission.RECORD_AUDIO
  ) == PackageManager.PERMISSION_GRANTED

  val permissionItems = remember(isMicGranted) {
    listOf(
      ActivePermissionItem(
        name = "Network & Threat Telemetry",
        permissionCode = "android.permission.INTERNET",
        purpose = "Live URL reputation, CoinGecko markets & Cloud Firestore sync",
        isGranted = true,
        isRuntime = false,
        icon = Icons.Default.Public
      ),
      ActivePermissionItem(
        name = "Wi-Fi & VPN Transport Audit",
        permissionCode = "android.permission.ACCESS_NETWORK_STATE",
        purpose = "Detects active VPN tunnels and unencrypted Wi-Fi links",
        isGranted = true,
        isRuntime = false,
        icon = Icons.Default.VpnLock
      ),
      ActivePermissionItem(
        name = "Voice AI Microphone Access",
        permissionCode = "android.permission.RECORD_AUDIO",
        purpose = "Used only during Gemini AI Live Voice Conversation mode",
        isGranted = isMicGranted,
        isRuntime = true,
        icon = Icons.Default.Mic
      )
    )
  }

  val isDeviceProtected = deviceSecurity.overallScore >= 80 && !deviceSecurity.isRooted
  val highRiskScanCount = allScans.count { it.riskLevel == "HIGH_RISK" || it.riskLevel == "SUSPICIOUS" }
  val healthLabel = when {
    deviceHealth.healthPercentage >= 85 -> "Good"
    deviceHealth.healthPercentage >= 65 -> "Fair"
    else -> "Check"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .verticalScroll(scrollState)
      .padding(bottom = 88.dp)
      .testTag("home_dashboard_screen")
  ) {
    // Top Bar
    CyberTopBar(
      unreadAlertCount = activeAlerts.size,
      onNotificationClick = { onNavigate(AppScreen.ACTIVITY) },
      onSettingsClick = { onNavigate(AppScreen.PROFILE) }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Central Hero: Glowing Shield & Live Audit Trigger
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable { viewModel.triggerQuickScan() }
      ) {
        GlowingShield(
          size = 184.dp,
          isScanning = isQuickScanning,
          isProtected = isDeviceProtected,
          modifier = Modifier.testTag("dashboard_glowing_shield")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      StatusPill(
        isProtected = isDeviceProtected,
        lastScanText = "Integrity Score: ${deviceSecurity.overallScore}/100"
      )

      Spacer(modifier = Modifier.height(14.dp))

      ElevatedButton(
        onClick = { viewModel.triggerQuickScan() },
        colors = ButtonDefaults.elevatedButtonColors(
          containerColor = Color(0xFF0F2C2C),
          contentColor = NeonCyan
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
          .testTag("dashboard_scan_now_button")
          .border(1.dp, NeonEmerald, RoundedCornerShape(24.dp))
      ) {
        Text(
          text = "⚡ Full System Audit",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan
        )
      }
    }

    Spacer(modifier = Modifier.height(22.dp))

    // Metric Tiles Row
    MetricTilesRow(
      threatsBlocked = 24 + highRiskScanCount,
      spamCalls = 18,
      phishingLinks = 12 + highRiskScanCount,
      systemHealth = healthLabel,
      modifier = Modifier.padding(horizontal = 16.dp),
      onTileClick = { tile ->
        when (tile) {
          "threats" -> onNavigate(AppScreen.DEVICE_SECURITY)
          "calls" -> onNavigate(AppScreen.COMMUNICATION_SHIELD)
          "links" -> onNavigate(AppScreen.URL_SCANNER)
          "health" -> onNavigate(AppScreen.DEVICE_HEALTH)
        }
      }
    )

    Spacer(modifier = Modifier.height(22.dp))

    // =========================================================================
    // ROOM-BACKED SECURITY ALERTS NOTIFICATION SECTION
    // =========================================================================
    SectionHeader(
      title = "Security Alerts (${activeAlerts.size})",
      actionText = "All Activity",
      onActionClick = { onNavigate(AppScreen.ACTIVITY) }
    )

    SecurityAlertsSectionCard(
      alerts = activeAlerts,
      onDismissAlert = { id -> viewModel.dismissAlert(id) },
      onDismissAll = { viewModel.dismissAllAlerts() },
      onInspectAlert = { alert ->
        when (alert.category.uppercase(Locale.US)) {
          "URL" -> onNavigate(AppScreen.URL_SCANNER)
          "PRIVACY" -> onNavigate(AppScreen.DEVICE_SECURITY)
          "NETWORK", "SYSTEM" -> onNavigate(AppScreen.DEVICE_SECURITY)
          else -> onNavigate(AppScreen.ACTIVITY)
        }
      },
      onRunAudit = { viewModel.triggerQuickScan() },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .testTag("dashboard_security_alerts_section")
    )

    Spacer(modifier = Modifier.height(20.dp))

    // =========================================================================
    // SUMMARY CARD 1: DEVICE SECURITY STATUS
    // =========================================================================
    SectionHeader(
      title = "Device Security Status",
      actionText = "Full Report",
      onActionClick = { onNavigate(AppScreen.DEVICE_SECURITY) }
    )

    DeviceSecuritySummaryCard(
      securityStatus = deviceSecurity,
      healthStats = deviceHealth,
      onRefresh = { viewModel.refreshTelemetry() },
      onOpenDetails = { onNavigate(AppScreen.DEVICE_SECURITY) },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .testTag("dashboard_device_security_card")
    )

    Spacer(modifier = Modifier.height(20.dp))

    // =========================================================================
    // SUMMARY CARD 2: RECENT URL SCAN RESULTS
    // =========================================================================
    SectionHeader(
      title = "Recent URL Scan Results",
      actionText = "Open Scanner",
      onActionClick = { onNavigate(AppScreen.URL_SCANNER) }
    )

    RecentUrlScansSummaryCard(
      recentScans = recentScanEntries,
      onOpenScanner = { onNavigate(AppScreen.URL_SCANNER) },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .testTag("dashboard_recent_scans_card")
    )

    Spacer(modifier = Modifier.height(20.dp))

    // =========================================================================
    // SUMMARY CARD 3: ACTIVE PERMISSIONS & PRIVACY POSTURE
    // =========================================================================
    SectionHeader(
      title = "Active Permissions",
      actionText = "Audit All",
      onActionClick = { onNavigate(AppScreen.DEVICE_SECURITY) }
    )

    ActivePermissionsSummaryCard(
      permissions = permissionItems,
      systemDangerousCount = deviceSecurity.totalDangerousPermissions,
      onOpenPermissionAudit = { onNavigate(AppScreen.DEVICE_SECURITY) },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .testTag("dashboard_active_permissions_card")
    )

    Spacer(modifier = Modifier.height(20.dp))

    // =========================================================================
    // QUICK ACCESS MODULES GRID
    // =========================================================================
    SectionHeader(
      title = "Quick Access",
      actionText = "View All",
      onActionClick = { onNavigate(AppScreen.FEATURES) }
    )

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickAccessCard(
          title = "Live Crypto Markets",
          subtitle = "Charts, Watchlist & INR/USD",
          icon = Icons.Default.CurrencyBitcoin,
          accent = NeonCyan,
          modifier = Modifier.weight(1f),
          testTag = "quick_access_live_markets",
          onClick = { onNavigate(AppScreen.CRYPTO_MARKETS) }
        )
        QuickAccessCard(
          title = "Gemini AI Analyst",
          subtitle = "Search Grounding & Voice",
          icon = Icons.Default.Security,
          accent = NeonEmerald,
          modifier = Modifier.weight(1f),
          testTag = "quick_access_ai_assistant",
          onClick = { onNavigate(AppScreen.AI_ASSISTANT) }
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickAccessCard(
          title = "Communication Shield",
          subtitle = "Calls, SMS & Spam",
          icon = Icons.Default.PhoneInTalk,
          accent = Color(0xFF00B4D8),
          modifier = Modifier.weight(1f),
          testTag = "quick_access_communication",
          onClick = { onNavigate(AppScreen.COMMUNICATION_SHIELD) }
        )
        QuickAccessCard(
          title = "Device Security",
          subtitle = "Permissions & Integrity",
          icon = Icons.Default.Security,
          accent = NeonEmerald,
          modifier = Modifier.weight(1f),
          testTag = "quick_access_security",
          onClick = { onNavigate(AppScreen.DEVICE_SECURITY) }
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickAccessCard(
          title = "Financial Protection",
          subtitle = "Vault & Privacy",
          icon = Icons.Default.AccountBalance,
          accent = Color(0xFF38BDF8),
          modifier = Modifier.weight(1f),
          testTag = "quick_access_financial",
          onClick = { onNavigate(AppScreen.PRIVACY_VAULT) }
        )
        QuickAccessCard(
          title = "Crypto Wallet Audit",
          subtitle = "Verify Contract Addresses",
          icon = Icons.Default.CurrencyBitcoin,
          accent = Color(0xFFF59E0B),
          modifier = Modifier.weight(1f),
          testTag = "quick_access_crypto",
          onClick = { onNavigate(AppScreen.CRYPTO_SECURITY) }
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        QuickAccessCard(
          title = "URL Threat Scanner",
          subtitle = "SSRF & Phishing Guard",
          icon = Icons.Default.Lock,
          accent = Color(0xFFA855F7),
          modifier = Modifier.weight(1f),
          testTag = "quick_access_privacy",
          onClick = { onNavigate(AppScreen.URL_SCANNER) }
        )
        QuickAccessCard(
          title = "Device Utilities",
          subtitle = "Storage & Battery",
          icon = Icons.Default.Smartphone,
          accent = Color(0xFF10B981),
          modifier = Modifier.weight(1f),
          testTag = "quick_access_utilities",
          onClick = { onNavigate(AppScreen.DEVICE_HEALTH) }
        )
      }
    }
  }
}

@Composable
private fun SecurityAlertsSectionCard(
  alerts: List<SecurityAlertEntity>,
  onDismissAlert: (Long) -> Unit,
  onDismissAll: () -> Unit,
  onInspectAlert: (SecurityAlertEntity) -> Unit,
  onRunAudit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.US) }

  val criticalCount = alerts.count { it.severity.equals("CRITICAL", ignoreCase = true) }
  val warningCount = alerts.count { it.severity.equals("WARNING", ignoreCase = true) }
  val headerBorderColor = when {
    criticalCount > 0 -> AlertRed.copy(alpha = 0.55f)
    warningCount > 0 -> AlertAmber.copy(alpha = 0.45f)
    else -> CyberBorder
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, headerBorderColor, shape)
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Top Summary Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          val iconTint = when {
            criticalCount > 0 -> AlertRed
            warningCount > 0 -> AlertAmber
            else -> NeonEmerald
          }
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.NotificationsActive,
              contentDescription = "Security Alerts",
              tint = iconTint,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (alerts.isEmpty()) {
                "All Clear — No Active Issues"
              } else {
                "${alerts.size} Active Issue${if (alerts.size == 1) "" else "s"} Needing Attention"
              },
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Live feed from on-device Room security_alerts database",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        if (alerts.isNotEmpty()) {
          TextButton(
            onClick = onDismissAll,
            modifier = Modifier.testTag("dashboard_dismiss_all_alerts_button")
          ) {
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = "Dismiss all alerts",
              tint = NeonCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Dismiss All",
              color = NeonCyan,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      if (alerts.isEmpty()) {
        // Empty / Resolved State
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBackground)
            .clickable { onRunAudit() }
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = NeonEmerald,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Zero unresolved security events",
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "All recent Room security alerts have been reviewed. Tap to run a fresh system audit.",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }
          }
        }
      } else {
        alerts.take(4).forEach { alert ->
          val (sevColor, sevLabel, sevIcon) = when (alert.severity.uppercase(Locale.US)) {
            "CRITICAL" -> Triple(AlertRed, "CRITICAL", Icons.Default.ErrorOutline)
            "WARNING" -> Triple(AlertAmber, "WARNING", Icons.Default.WarningAmber)
            else -> Triple(NeonCyan, "INFO", Icons.Default.Info)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(CyberBackground)
              .border(1.dp, sevColor.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
              .clickable { onInspectAlert(alert) }
              .padding(12.dp)
              .testTag("dashboard_alert_item_${alert.id}")
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = sevIcon,
                    contentDescription = sevLabel,
                    tint = sevColor,
                    modifier = Modifier.size(16.dp)
                  )
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = sevColor.copy(alpha = 0.16f)
                  ) {
                    Text(
                      text = sevLabel,
                      color = sevColor,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyberSurfaceElevated
                  ) {
                    Text(
                      text = alert.category,
                      color = TextSecondary,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Medium,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                  Text(
                    text = timeFormatter.format(Date(alert.timestamp)),
                    color = TextMuted,
                    fontSize = 10.sp
                  )
                }

                IconButton(
                  onClick = { onDismissAlert(alert.id) },
                  modifier = Modifier
                    .size(28.dp)
                    .testTag("dismiss_alert_button_${alert.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss alert",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }

              Text(
                text = alert.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = alert.message,
                color = TextSecondary,
                fontSize = 12.sp
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Inspect & Resolve →",
                  color = sevColor,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DeviceSecuritySummaryCard(
  securityStatus: DeviceSecurityStatus,
  healthStats: DeviceHealthStats,
  onRefresh: () -> Unit,
  onOpenDetails: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  val scoreColor = when {
    securityStatus.overallScore >= 85 -> NeonEmerald
    securityStatus.overallScore >= 70 -> AlertAmber
    else -> AlertRed
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(CyberSurfaceElevated, CyberSurface)
        )
      )
      .border(1.dp, CyberBorder, shape)
      .clickable { onOpenDetails() }
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(scoreColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (securityStatus.overallScore >= 85) Icons.Default.GppGood else Icons.Default.GppMaybe,
              contentDescription = "Device Security Score",
              tint = scoreColor,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Integrity Score: ${securityStatus.overallScore}/100",
              color = TextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = securityStatus.statusSummary,
              color = TextSecondary,
              fontSize = 12.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        IconButton(
          onClick = onRefresh,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CyberBackground)
            .testTag("refresh_device_security_button")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh security telemetry",
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      LinearProgressIndicator(
        progress = { (securityStatus.overallScore / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = scoreColor,
        trackColor = CyberBackground
      )

      // 3-Pillar Security Indicator Row (Root Sandbox, USB Debugging, VPN Egress)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SecurityPillarChip(
          label = "Root Sandbox",
          value = if (securityStatus.isRooted) "Compromised" else "Intact",
          isHealthy = !securityStatus.isRooted,
          modifier = Modifier.weight(1f)
        )
        SecurityPillarChip(
          label = "USB Debug",
          value = if (securityStatus.isUsbDebuggingEnabled) "Enabled" else "Off",
          isHealthy = !securityStatus.isUsbDebuggingEnabled,
          modifier = Modifier.weight(1f)
        )
        SecurityPillarChip(
          label = "Network",
          value = if (securityStatus.isVpnActive) "VPN Active" else "Standard",
          isHealthy = true,
          modifier = Modifier.weight(1f)
        )
      }

      HorizontalDivider(color = CyberBorderSubtle)

      // Hardware Telemetry Footer
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "RAM: ${healthStats.ramUsedMb}/${healthStats.ramTotalMb} MB • Battery: ${healthStats.batteryPercentage}% (${healthStats.batteryTempCelsius}°C)",
          color = TextMuted,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Open Device Security",
          tint = NeonEmerald,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
private fun SecurityPillarChip(
  label: String,
  value: String,
  isHealthy: Boolean,
  modifier: Modifier = Modifier
) {
  val statusColor = if (isHealthy) NeonEmerald else AlertAmber
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(CyberBackground)
      .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    Text(
      text = label,
      color = TextMuted,
      fontSize = 10.sp,
      maxLines = 1
    )
    Spacer(modifier = Modifier.height(2.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(statusColor)
      )
      Spacer(modifier = Modifier.width(5.dp))
      Text(
        text = value,
        color = TextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun RecentUrlScansSummaryCard(
  recentScans: List<DashboardScanEntry>,
  onOpenScanner: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  val timeFormatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.US) }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(CyberBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Link,
              contentDescription = "Recent URL Scans",
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "SSRF & Phishing Threat Log",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${recentScans.size} recent URL reputation checks",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }

        TextButton(
          onClick = onOpenScanner,
          modifier = Modifier.testTag("dashboard_scan_url_cta")
        ) {
          Text(
            text = "Scan URL",
            color = NeonCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (recentScans.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBackground)
            .clickable { onOpenScanner() }
            .padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = NeonEmerald,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "No URL scans recorded yet",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Tap here to analyze suspicious links for phishing, homograph attacks, and SSRF risks.",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }
        }
      } else {
        recentScans.forEach { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CyberBackground)
              .clickable { onOpenScanner() }
              .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = item.domain,
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = CyberSurfaceElevated
                ) {
                  Text(
                    text = item.sourceLabel,
                    color = TextMuted,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Risk Score: ${item.riskScore}/100 • ${timeFormatter.format(Date(item.timestampMillis))}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }

            Spacer(modifier = Modifier.width(8.dp))
            RiskBadge(level = item.riskLevel)
          }
        }
      }
    }
  }
}

@Composable
private fun ActivePermissionsSummaryCard(
  permissions: List<ActivePermissionItem>,
  systemDangerousCount: Int,
  onOpenPermissionAudit: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(NeonEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = "Active Permissions",
              tint = NeonEmerald,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Least-Privilege Permission Guard",
              color = TextPrimary,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "$systemDangerousCount sensitive permissions detected across installed apps",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
        }
      }

      permissions.forEach { perm ->
        val badgeColor = if (perm.isGranted) AlertGreen else AlertAmber
        val badgeText = when {
          perm.isGranted && !perm.isRuntime -> "Active"
          perm.isGranted && perm.isRuntime -> "Granted"
          else -> "On-Demand"
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CyberBackground)
            .clickable { onOpenPermissionAudit() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = perm.icon,
              contentDescription = perm.name,
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = perm.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = perm.purpose,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(badgeColor.copy(alpha = 0.15f))
              .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (perm.isGranted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = badgeText,
                color = badgeColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}

private fun parseRiskLevel(raw: String): RiskLevel {
  return when (raw.uppercase(Locale.US)) {
    "SAFE" -> RiskLevel.SAFE
    "SUSPICIOUS" -> RiskLevel.SUSPICIOUS
    "HIGH_RISK" -> RiskLevel.HIGH_RISK
    else -> RiskLevel.UNKNOWN
  }
}

@Composable
fun QuickAccessCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accent: Color,
  modifier: Modifier = Modifier,
  testTag: String = "quick_access_card",
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(16.dp)
  Box(
    modifier = modifier
      .testTag(testTag)
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .clickable { onClick() }
      .padding(14.dp)
  ) {
    Column {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accent,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        color = TextMuted,
        fontSize = 11.sp,
        maxLines = 1
      )
    }
  }
}
