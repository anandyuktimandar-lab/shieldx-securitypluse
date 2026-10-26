package com.example.ui.screens.communication

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
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
fun CommunicationShieldScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val blockSpam by viewModel.blockSpamCalls.collectAsState()
  val identifyHidden by viewModel.identifyHiddenNumbers.collectAsState()
  val telemarketerFilter by viewModel.telemarketerFilter.collectAsState()
  val smsPhishingFilter by viewModel.smsPhishingFilter.collectAsState()

  var selectedTab by remember { mutableStateOf("Calls") }
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
        modifier = Modifier.testTag("communication_shield_back_button")
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
          text = "Communication Shield",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Calls, SMS & Spam Defense",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Tabs: Calls, Messages, Rules matching mockup
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(CyberSurface)
        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
        .padding(4.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      listOf("Calls", "Messages", "Rules").forEach { tab ->
        val isSelected = selectedTab == tab
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF0F2F2F) else Color.Transparent)
            .clickable { selectedTab = tab }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = tab,
            color = if (isSelected) NeonCyan else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Center Phone Emblem with Cyber Rings matching mockup
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(130.dp)) {
          drawCircle(
            color = CyberBorder,
            radius = size.width * 0.45f,
            style = Stroke(width = 2.dp.toPx())
          )
          drawCircle(
            color = NeonEmerald.copy(alpha = 0.3f),
            radius = size.width * 0.35f,
            style = Stroke(width = 1.5.dp.toPx())
          )
        }
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF0C242A))
            .border(2.dp, NeonEmerald, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PhoneInTalk,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(34.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Spam Call Protection",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = if (blockSpam) "Active & Shielding Incoming Invocations" else "Protection Paused",
        color = if (blockSpam) NeonEmerald else TextMuted,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Switch Toggles matching mockup
    CommSwitchCard(
      title = "Block Spam Calls",
      subtitle = "Silence known fraudulent & robo-dialers",
      checked = blockSpam,
      onCheckedChange = { viewModel.toggleBlockSpamCalls() },
      testTag = "comm_toggle_spam"
    )

    Spacer(modifier = Modifier.height(10.dp))

    CommSwitchCard(
      title = "Identify Hidden Numbers",
      subtitle = "Filter out private and anonymous callers",
      checked = identifyHidden,
      onCheckedChange = { viewModel.toggleIdentifyHiddenNumbers() },
      testTag = "comm_toggle_hidden"
    )

    Spacer(modifier = Modifier.height(10.dp))

    CommSwitchCard(
      title = "Telemarketer Filter",
      subtitle = "Aggressively drop aggressive cold call numbers",
      checked = telemarketerFilter,
      onCheckedChange = { viewModel.toggleTelemarketerFilter() },
      testTag = "comm_toggle_telemarket"
    )

    Spacer(modifier = Modifier.height(10.dp))

    CommSwitchCard(
      title = "SMS Phishing & Smishing Filter",
      subtitle = "Screen messages for deceptive bank/airdrop links",
      checked = smsPhishingFilter,
      onCheckedChange = { viewModel.toggleSmsPhishingFilter() },
      testTag = "comm_toggle_smishing"
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Recent Activity Section
    Text(
      text = "Recent Activity",
      color = TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    CyberCard {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Spam Call Blocked",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "+1 234 567 8900 • Robocall",
            color = TextMuted,
            fontSize = 11.sp
          )
        }
        Text(
          text = "2m ago",
          color = NeonEmerald,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun CommSwitchCard(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "comm_switch"
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .padding(horizontal = 14.dp, vertical = 10.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 11.sp
        )
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
