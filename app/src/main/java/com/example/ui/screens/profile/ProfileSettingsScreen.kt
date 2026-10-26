package com.example.ui.screens.profile

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileSettingsScreen(
  viewModel: MainViewModel,
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  var isBiometricEnabled by remember { mutableStateOf(true) }
  var isAutoThreatSync by remember { mutableStateOf(true) }
  var showPrivacyDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  val watchlistDocs by viewModel.watchlistDocs.collectAsState()
  val portfolioDocs by viewModel.portfolioDocs.collectAsState()
  val allScans by viewModel.allScans.collectAsState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(bottom = 80.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "Privacy & Preferences",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Direct Local Device Profile Card (No Mandatory Login)
    CyberCard {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F2E3A))
            .border(2.dp, NeonEmerald, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "SX",
            color = NeonCyan,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "ShieldX On-Device Profile",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Direct Access • No Mandatory Login Required",
            color = TextSecondary,
            fontSize = 12.sp
          )
          Text(
            text = "Local Storage: ${watchlistDocs.size} Watched • ${portfolioDocs.size} Holdings • ${allScans.size} Scans",
            color = NeonEmerald,
            fontSize = 11.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Security Settings Section
    Text(
      text = "Security Controls",
      color = TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingToggleRow(
      title = "Biometric & Local KeyStore Lock",
      subtitle = "Require fingerprint / device credential to open Vault",
      icon = Icons.Default.Fingerprint,
      checked = isBiometricEnabled,
      onCheckedChange = { isBiometricEnabled = it }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SettingToggleRow(
      title = "On-Device Threat Rule Updates",
      subtitle = "Automatically refresh known malicious domains & drainers",
      icon = Icons.Default.Sync,
      checked = isAutoThreatSync,
      onCheckedChange = { isAutoThreatSync = it }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Learning Center & Education Section
    Text(
      text = "Education & Knowledge",
      color = TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingActionRow(
      title = "Cybersecurity Learning Center",
      subtitle = "Interactive guides on phishing, MFA & Wi-Fi defense",
      icon = Icons.Default.School,
      onClick = { onNavigate(AppScreen.LEARNING_CENTER) }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Compliance & Transparency Section
    Text(
      text = "Compliance & Trust",
      color = TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    SettingActionRow(
      title = "Zero-Knowledge Privacy Policy",
      subtitle = "Learn how ShieldX processes all scans strictly on device",
      icon = Icons.Default.Policy,
      onClick = { showPrivacyDialog = true }
    )

    Spacer(modifier = Modifier.height(8.dp))

    SettingActionRow(
      title = "About ShieldX & Methodology",
      subtitle = "Version 1.0 (Build 36) • Engineering standards",
      icon = Icons.Default.Info,
      onClick = { showAboutDialog = true }
    )
  }

  // Privacy Policy Dialog
  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      title = { Text("ShieldX Privacy Policy", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "1. 100% On-Device Processing: Scanned URLs, password evaluations, and vault records are stored exclusively in your device's local encrypted Room SQLite database.\n\n" +
              "2. No Ad-Tracking: We do not monetize personal data or track analytics IDs.\n\n" +
              "3. Least-Privilege Permissions: ShieldX requests only normal network state and vibration permissions.",
            color = TextSecondary,
            fontSize = 13.sp
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showPrivacyDialog = false }) {
          Text("I Understand", color = NeonCyan)
        }
      },
      containerColor = CyberSurface
    )
  }

  // About Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = { Text("ShieldX by SecuriPulse", color = TextPrimary, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(
            text = "ShieldX provides defensive cybersecurity tools, SSRF URL scanning, AES-256 GCM vault protection, and genuine Android hardware telemetry.\n\n" +
              "Methodology Notice: Security scores reflect verified hardware metrics and heuristics. No software can guarantee 100% immunity; always maintain multiple layers of security hygiene.",
            color = TextSecondary,
            fontSize = 13.sp
          )
        }
      },
      confirmButton = {
        TextButton(onClick = { showAboutDialog = false }) {
          Text("Close", color = NeonCyan)
        }
      },
      containerColor = CyberSurface
    )
  }
}

@Composable
fun SettingToggleRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
      }
      Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
          checkedThumbColor = CyberBackground,
          checkedTrackColor = NeonEmerald,
          uncheckedThumbColor = TextMuted,
          uncheckedTrackColor = CyberBackground
        )
      )
    }
  }
}

@Composable
fun SettingActionRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .clickable { onClick() }
      .padding(horizontal = 14.dp, vertical = 12.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(text = title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
      }
      Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
  }
}
