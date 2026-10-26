package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

enum class AppScreen {
  HOME,
  FEATURES,
  ACTIVITY,
  PROFILE,
  // Sub-screens
  URL_SCANNER,
  DEVICE_SECURITY,
  DEVICE_HEALTH,
  COMMUNICATION_SHIELD,
  PRIVACY_VAULT,
  CRYPTO_SECURITY,
  CRYPTO_MARKETS,
  AI_ASSISTANT,
  LEARNING_CENTER,
  NOTIFICATIONS_MODAL
}

@Composable
fun ShieldXBottomBar(
  currentScreen: AppScreen,
  onNavigate: (AppScreen) -> Unit,
  onQuickScanClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .background(CyberBackground)
  ) {
    // Nav bar base
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(68.dp)
        .background(CyberSurface)
        .border(width = 1.dp, color = CyberBorder.copy(alpha = 0.5f))
        .padding(horizontal = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        BottomNavItem(
          label = "Home",
          selectedIcon = Icons.Filled.Home,
          unselectedIcon = Icons.Outlined.Home,
          isSelected = currentScreen == AppScreen.HOME,
          testTag = "nav_home",
          onClick = { onNavigate(AppScreen.HOME) }
        )

        BottomNavItem(
          label = "Features",
          selectedIcon = Icons.Filled.GridView,
          unselectedIcon = Icons.Outlined.GridView,
          isSelected = currentScreen == AppScreen.FEATURES,
          testTag = "nav_features",
          onClick = { onNavigate(AppScreen.FEATURES) }
        )

        // Empty space for center floating button
        Spacer(modifier = Modifier.size(56.dp))

        BottomNavItem(
          label = "Activity",
          selectedIcon = Icons.Filled.History,
          unselectedIcon = Icons.Outlined.History,
          isSelected = currentScreen == AppScreen.ACTIVITY,
          testTag = "nav_activity",
          onClick = { onNavigate(AppScreen.ACTIVITY) }
        )

        BottomNavItem(
          label = "Profile",
          selectedIcon = Icons.Filled.Person,
          unselectedIcon = Icons.Outlined.Person,
          isSelected = currentScreen == AppScreen.PROFILE,
          testTag = "nav_profile",
          onClick = { onNavigate(AppScreen.PROFILE) }
        )
      }
    }

    // Center Floating Cyber "X" Quick Scan Button
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(y = (-18).dp)
        .size(58.dp)
        .shadow(12.dp, CircleShape, spotColor = NeonEmerald)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFF0F3B3B), Color(0xFF051717))
          )
        )
        .border(2.dp, NeonEmerald, CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null
        ) { onQuickScanClick() }
        .testTag("nav_quick_scan_fab"),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "X",
        color = NeonCyan,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp
      )
    }
  }
}

@Composable
private fun BottomNavItem(
  label: String,
  selectedIcon: ImageVector,
  unselectedIcon: ImageVector,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .testTag(testTag)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
      )
      .padding(horizontal = 12.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = if (isSelected) selectedIcon else unselectedIcon,
      contentDescription = label,
      tint = if (isSelected) NeonEmerald else TextMuted,
      modifier = Modifier.size(24.dp)
    )
    Spacer(modifier = Modifier.height(3.dp))
    Text(
      text = label,
      color = if (isSelected) NeonEmerald else TextMuted,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
    )
  }
}
