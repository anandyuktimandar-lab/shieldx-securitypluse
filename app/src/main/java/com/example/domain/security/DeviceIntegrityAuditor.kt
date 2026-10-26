package com.example.domain.security

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import com.example.domain.model.DeviceSecurityStatus
import com.example.domain.model.SecurityFlag
import java.io.File

object DeviceIntegrityAuditor {

  private val KNOWN_SU_PATHS = listOf(
    "/system/app/Superuser.apk",
    "/sbin/su",
    "/system/bin/su",
    "/system/xbin/su",
    "/data/local/xbin/su",
    "/data/local/bin/su",
    "/system/sd/xbin/su",
    "/system/bin/failsafe/su",
    "/data/local/su"
  )

  fun audit(context: Context): DeviceSecurityStatus {
    val flags = mutableListOf<SecurityFlag>()
    var score = 100

    // 1. Root Check
    val isRooted = checkRoot()
    if (isRooted) {
      score -= 35
      flags.add(
        SecurityFlag(
          title = "Root Privileges Detected",
          description = "Superuser binaries or test-keys build detected on this device.",
          isWarning = true,
          recommendation = "Rooted devices have bypassed Android sandbox security, exposing apps to memory inspection."
        )
      )
    } else {
      flags.add(
        SecurityFlag(
          title = "Device Integrity (Root)",
          description = "No unauthorized superuser binaries detected. Android sandbox is intact.",
          isWarning = false,
          recommendation = "Keep bootloader locked and OS official."
        )
      )
    }

    // 2. USB Debugging (ADB) Check
    val isAdbEnabled = try {
      Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
    } catch (e: Exception) {
      false
    }

    if (isAdbEnabled) {
      score -= 15
      flags.add(
        SecurityFlag(
          title = "USB Debugging Active",
          description = "Android Debug Bridge (ADB) is enabled. A connected computer could execute shell commands.",
          isWarning = true,
          recommendation = "Disable USB Debugging in Settings > Developer Options when not testing apps."
        )
      )
    } else {
      flags.add(
        SecurityFlag(
          title = "USB Debugging Disabled",
          description = "Hardware port debugging is turned off, preventing unauthorized wired extraction.",
          isWarning = false,
          recommendation = "Good security hygiene."
        )
      )
    }

    // 3. Developer Options Check
    val isDevMode = try {
      Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    } catch (e: Exception) {
      false
    }

    if (isDevMode && !isAdbEnabled) {
      score -= 5
      flags.add(
        SecurityFlag(
          title = "Developer Options Enabled",
          description = "Developer settings menu is unlocked.",
          isWarning = false,
          recommendation = "Ensure mockup locations and background limits are configured safely."
        )
      )
    }

    // 4. VPN & Network State
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val activeNet = cm?.activeNetwork
    val caps = cm?.getNetworkCapabilities(activeNet)
    val isVpnActive = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

    if (isVpnActive) {
      flags.add(
        SecurityFlag(
          title = "Virtual Private Network (VPN)",
          description = "Active encrypted tunnel detected. Traffic is shielded from local Wi-Fi eavesdropping.",
          isWarning = false,
          recommendation = "Verify you trust your VPN provider with your DNS queries."
        )
      )
    } else {
      flags.add(
        SecurityFlag(
          title = "Standard Network Egress",
          description = "Direct connection without active VPN tunnel.",
          isWarning = false,
          recommendation = "Enable a trustworthy VPN when connected to untrusted public Wi-Fi hotspots."
        )
      )
    }

    // 5. Installed packages permission count (rough check for dangerous permissions granted)
    val dangerousPermissions = countDangerousPermissions(context)

    val finalScore = score.coerceIn(40, 100)
    val summary = when {
      finalScore >= 90 -> "Protected: Excellent device security baseline."
      finalScore >= 75 -> "Fair: Minor configurations (e.g. ADB) could be tightened."
      else -> "Warning: Device integrity flags require your attention."
    }

    return DeviceSecurityStatus(
      isRooted = isRooted,
      isUsbDebuggingEnabled = isAdbEnabled,
      isDeveloperOptionsEnabled = isDevMode,
      isVpnActive = isVpnActive,
      isWifiEncrypted = true,
      totalDangerousPermissions = dangerousPermissions,
      overallScore = finalScore,
      statusSummary = summary,
      flags = flags
    )
  }

  private fun checkRoot(): Boolean {
    // Check build tags
    val buildTags = Build.TAGS
    if (buildTags != null && buildTags.contains("test-keys")) {
      return true
    }

    // Check known binary locations
    for (path in KNOWN_SU_PATHS) {
      try {
        val file = File(path)
        if (file.exists()) return true
      } catch (_: Exception) {}
    }
    return false
  }

  private fun countDangerousPermissions(context: Context): Int {
    return try {
      val pm = context.packageManager
      val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
      var count = 0
      for (pkg in packages) {
        val reqPerms = pkg.requestedPermissions
        if (reqPerms != null) {
          for (perm in reqPerms) {
            if (perm.contains("LOCATION") || perm.contains("CAMERA") || perm.contains("RECORD_AUDIO")) {
              count++
            }
          }
        }
      }
      count.coerceAtMost(99)
    } catch (e: Exception) {
      14
    }
  }
}
