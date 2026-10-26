package com.example.ui.screens.activity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.RiskLevel
import com.example.ui.components.CyberCard
import com.example.ui.components.RiskBadge
import com.example.ui.theme.AlertAmber
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
fun ActivityScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val allScans by viewModel.allScans.collectAsState()
  val activeAlerts by viewModel.activeAlerts.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Header with Clear Action
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "Security Activity & Reports",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Persistent audit log of scans and events",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }

      IconButton(
        onClick = { viewModel.clearAllHistory() },
        modifier = Modifier.testTag("activity_clear_history_button")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteSweep,
          contentDescription = "Clear History",
          tint = TextSecondary
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Filter Chips: All, Alerts, Scans
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      listOf("All", "Alerts", "Scans").forEach { filter ->
        val isSelected = selectedFilter == filter
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) NeonEmerald else CyberSurface)
            .border(1.dp, if (isSelected) NeonEmerald else CyberBorder, RoundedCornerShape(20.dp))
            .clickable { selectedFilter = filter }
            .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
          Text(
            text = filter,
            color = if (isSelected) CyberBackground else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Content List
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(bottom = 76.dp)
    ) {
      if (selectedFilter == "All" || selectedFilter == "Alerts") {
        if (activeAlerts.isNotEmpty()) {
          item {
            Text(
              text = "Active Alerts (${activeAlerts.size})",
              color = AlertAmber,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(vertical = 4.dp)
            )
          }
          items(activeAlerts) { alert ->
            CyberCard(
              borderColor = if (alert.severity == "CRITICAL") AlertRed else AlertAmber,
              testTag = "alert_item_${alert.id}"
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = if (alert.severity == "CRITICAL") AlertRed else AlertAmber,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = alert.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  Text(text = alert.message, color = TextSecondary, fontSize = 11.sp)
                }
                IconButton(onClick = { viewModel.dismissAlert(alert.id) }) {
                  Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextMuted)
                }
              }
            }
          }
        }
      }

      if (selectedFilter == "All" || selectedFilter == "Scans") {
        item {
          Text(
            text = "Scan History (${allScans.size})",
            color = NeonCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }
        if (allScans.isEmpty()) {
          item {
            CyberCard {
              Text(
                text = "No URL scans recorded. Submit links in the URL Scanner to generate reports.",
                color = TextMuted,
                fontSize = 12.sp
              )
            }
          }
        } else {
          items(allScans) { scan ->
            CyberCard(testTag = "scan_history_${scan.id}") {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(text = scan.domain, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = scan.url, color = TextMuted, fontSize = 11.sp, maxLines = 1)
                  }
                }
                RiskBadge(
                  level = try { RiskLevel.valueOf(scan.riskLevel) } catch (e: Exception) { RiskLevel.UNKNOWN }
                )
              }
            }
          }
        }
      }
    }
  }
}
