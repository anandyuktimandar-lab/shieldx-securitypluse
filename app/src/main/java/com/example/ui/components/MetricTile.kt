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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
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
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MetricTile(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color = NeonEmerald,
  modifier: Modifier = Modifier,
  testTag: String = "metric_tile",
  onClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(16.dp)

  Box(
    modifier = modifier
      .testTag(testTag)
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
      .padding(horizontal = 12.dp, vertical = 14.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accentColor,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = title,
        color = TextSecondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = value,
        color = if (value.equals("Good", ignoreCase = true)) accentColor else TextPrimary,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun MetricTilesRow(
  threatsBlocked: Int,
  spamCalls: Int,
  phishingLinks: Int,
  systemHealth: String,
  modifier: Modifier = Modifier,
  onTileClick: (String) -> Unit = {}
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    MetricTile(
      title = "Threats Blocked",
      value = "$threatsBlocked",
      icon = Icons.Filled.Security,
      accentColor = NeonEmerald,
      modifier = Modifier.weight(1f),
      testTag = "metric_threats_blocked",
      onClick = { onTileClick("threats") }
    )
    MetricTile(
      title = "Spam Calls",
      value = "$spamCalls",
      icon = Icons.Filled.Phone,
      accentColor = Color(0xFF00B4D8),
      modifier = Modifier.weight(1f),
      testTag = "metric_spam_calls",
      onClick = { onTileClick("calls") }
    )
    MetricTile(
      title = "Phishing Links",
      value = "$phishingLinks",
      icon = Icons.Filled.Link,
      accentColor = Color(0xFFF59E0B),
      modifier = Modifier.weight(1f),
      testTag = "metric_phishing_links",
      onClick = { onTileClick("links") }
    )
    MetricTile(
      title = "System Health",
      value = systemHealth,
      icon = Icons.Filled.Favorite,
      accentColor = NeonEmerald,
      modifier = Modifier.weight(1f),
      testTag = "metric_system_health",
      onClick = { onTileClick("health") }
    )
  }
}
