package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RiskLevel
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceElevated
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CyberTopBar(
  unreadAlertCount: Int,
  onNotificationClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // ShieldX Logo Branding
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F2338))
          .border(1.dp, NeonEmerald, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "X",
          color = NeonCyan,
          fontWeight = FontWeight.Black,
          fontSize = 20.sp
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Shield",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Text(
            text = "X",
            color = NeonEmerald,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
          )
        }
        Text(
          text = "by SecuriPulse",
          color = TextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }

    // Actions: Notifications & Settings
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box {
        IconButton(
          onClick = onNotificationClick,
          modifier = Modifier.testTag("topbar_notifications_button")
        ) {
          Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            tint = TextPrimary
          )
        }
        if (unreadAlertCount > 0) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 6.dp, end = 6.dp)
              .size(16.dp)
              .clip(CircleShape)
              .background(AlertRed),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (unreadAlertCount > 9) "9+" else "$unreadAlertCount",
              color = Color.White,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.testTag("topbar_settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = TextPrimary
        )
      }
    }
  }
}

@Composable
fun StatusPill(
  isProtected: Boolean = true,
  lastScanText: String = "Last scan: 2 mins ago",
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(20.dp)
  Box(
    modifier = modifier
      .clip(shape)
      .background(Color(0xFF0C2424))
      .border(1.dp, NeonEmerald.copy(alpha = 0.5f), shape)
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = if (isProtected) Icons.Default.CheckCircle else Icons.Default.Warning,
        contentDescription = null,
        tint = if (isProtected) NeonEmerald else AlertAmber,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = if (isProtected) "Your Device: Protected" else "Security Attention Needed",
        color = NeonEmerald,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      color = TextPrimary,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )
    if (actionText != null && onActionClick != null) {
      Text(
        text = actionText,
        color = NeonEmerald,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
          .clickable { onActionClick() }
          .padding(4.dp)
      )
    }
  }
}

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(16.dp),
  borderColor: Color = CyberBorder,
  backgroundColor: Color = CyberSurface,
  testTag: String = "cyber_card",
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  Box(
    modifier = modifier
      .testTag(testTag)
      .clip(shape)
      .background(backgroundColor)
      .border(1.dp, borderColor, shape)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .padding(16.dp)
  ) {
    content()
  }
}

@Composable
fun RiskBadge(level: RiskLevel, modifier: Modifier = Modifier) {
  val (bgColor, textColor, label) = when (level) {
    RiskLevel.SAFE -> Triple(AlertGreen.copy(alpha = 0.15f), AlertGreen, "Safe")
    RiskLevel.SUSPICIOUS -> Triple(AlertAmber.copy(alpha = 0.15f), AlertAmber, "Suspicious")
    RiskLevel.HIGH_RISK -> Triple(AlertRed.copy(alpha = 0.15f), AlertRed, "High Risk")
    RiskLevel.UNKNOWN -> Triple(TextMuted.copy(alpha = 0.15f), TextMuted, "Unknown")
  }
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold
    )
  }
}
