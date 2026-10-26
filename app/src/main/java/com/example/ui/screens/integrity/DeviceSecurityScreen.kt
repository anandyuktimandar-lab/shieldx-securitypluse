package com.example.ui.screens.integrity

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DeviceSecurityScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val status by viewModel.deviceSecurity.collectAsState()
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .verticalScroll(scrollState)
      .padding(horizontal = 16.dp)
      .padding(bottom = 80.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Top Bar
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("device_security_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = TextPrimary
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Column {
        Text(
          text = "Device Security",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Keep Your Device Safe",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Center Circular Status Gauge matching mockup
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(160.dp)) {
          // Track ring
          drawCircle(
            color = CyberBorder,
            radius = size.width * 0.42f,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
          )
          // Progress arc
          val sweep = (status.overallScore / 100f) * 360f
          drawArc(
            color = if (status.overallScore >= 80) NeonEmerald else AlertAmber,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = NeonEmerald,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (status.overallScore >= 85) "Good" else "Attention",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${status.overallScore}% Integrity",
            color = NeonCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = status.statusSummary,
        color = TextSecondary,
        fontSize = 13.sp
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // List of Security Inspections matching the mockup
    SecurityCheckRow(
      title = "Root Detection",
      subtitle = if (status.isRooted) "Superuser detected" else "Not Detected",
      icon = Icons.Default.Security,
      isWarning = status.isRooted,
      testTag = "sec_row_root"
    )

    Spacer(modifier = Modifier.height(10.dp))

    SecurityCheckRow(
      title = "USB Debugging",
      subtitle = if (status.isUsbDebuggingEnabled) "Active (Shell accessible)" else "Disabled",
      icon = Icons.Default.Usb,
      isWarning = status.isUsbDebuggingEnabled,
      testTag = "sec_row_usb"
    )

    Spacer(modifier = Modifier.height(10.dp))

    SecurityCheckRow(
      title = "Developer Options",
      subtitle = if (status.isDeveloperOptionsEnabled) "Enabled in OS" else "Locked",
      icon = Icons.Default.BugReport,
      isWarning = false,
      testTag = "sec_row_dev"
    )

    Spacer(modifier = Modifier.height(10.dp))

    SecurityCheckRow(
      title = "App Permissions Audit",
      subtitle = "${status.totalDangerousPermissions} apps with sensitive permissions",
      icon = Icons.Default.VerifiedUser,
      isWarning = false,
      testTag = "sec_row_perms"
    )

    Spacer(modifier = Modifier.height(10.dp))

    SecurityCheckRow(
      title = "Network Security (VPN)",
      subtitle = if (status.isVpnActive) "Encrypted VPN Active" else "Direct Connection (No VPN)",
      icon = Icons.Default.Wifi,
      isWarning = false,
      testTag = "sec_row_net"
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Actionable Refresh Button
    Button(
      onClick = { viewModel.refreshTelemetry() },
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF0F2C2C),
        contentColor = NeonCyan
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .border(1.dp, NeonEmerald, RoundedCornerShape(12.dp))
        .testTag("device_security_refresh_button")
    ) {
      Text("Re-Audit Device Integrity", fontWeight = FontWeight.Bold, color = NeonCyan)
    }
  }
}

@Composable
fun SecurityCheckRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  isWarning: Boolean,
  modifier: Modifier = Modifier,
  testTag: String = "security_check_row"
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, if (isWarning) AlertRed.copy(alpha = 0.5f) else CyberBorder, shape)
      .padding(14.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F2338)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (isWarning) AlertRed else NeonEmerald,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          color = if (isWarning) AlertAmber else TextSecondary,
          fontSize = 12.sp
        )
      }

      Text(
        text = if (isWarning) "Warning" else "Secure",
        color = if (isWarning) AlertAmber else NeonEmerald,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
