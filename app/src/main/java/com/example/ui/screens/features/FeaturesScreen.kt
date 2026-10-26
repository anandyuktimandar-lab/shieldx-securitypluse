package com.example.ui.screens.features

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.domain.model.FeatureCategory
import com.example.ui.navigation.AppScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class FeatureRowItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val category: FeatureCategory,
  val icon: ImageVector,
  val targetScreen: AppScreen
)

@Composable
fun FeaturesScreen(
  onNavigate: (AppScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(FeatureCategory.ALL) }

  val allFeatures = listOf(
    FeatureRowItem(
      id = "live_crypto_markets",
      title = "Live Crypto Markets & Charts",
      subtitle = "Real-time USD/INR prices, Candlestick charts & Cloud Portfolio",
      category = FeatureCategory.CRYPTO,
      icon = Icons.Default.CurrencyBitcoin,
      targetScreen = AppScreen.CRYPTO_MARKETS
    ),
    FeatureRowItem(
      id = "gemini_ai_assistant",
      title = "Gemini AI Security & Market Analyst",
      subtitle = "Multi-turn AI chat with Google Search Grounding & Voice Mode",
      category = FeatureCategory.SECURITY,
      icon = Icons.Default.VerifiedUser,
      targetScreen = AppScreen.AI_ASSISTANT
    ),
    FeatureRowItem(
      id = "spam_call",
      title = "Spam Call Blocker",
      subtitle = "Block unwanted calls in real-time",
      category = FeatureCategory.SECURITY,
      icon = Icons.Default.PhoneInTalk,
      targetScreen = AppScreen.COMMUNICATION_SHIELD
    ),
    FeatureRowItem(
      id = "sms_phishing",
      title = "SMS Phishing Detector",
      subtitle = "Detect suspicious messages & smishing",
      category = FeatureCategory.SECURITY,
      icon = Icons.Default.Message,
      targetScreen = AppScreen.COMMUNICATION_SHIELD
    ),
    FeatureRowItem(
      id = "device_integrity",
      title = "Device Integrity Check",
      subtitle = "Verify root, ADB & platform security status",
      category = FeatureCategory.SECURITY,
      icon = Icons.Default.Shield,
      targetScreen = AppScreen.DEVICE_SECURITY
    ),
    FeatureRowItem(
      id = "perm_audit",
      title = "Permission Audit",
      subtitle = "Review dangerous app permissions",
      category = FeatureCategory.PRIVACY,
      icon = Icons.Default.VerifiedUser,
      targetScreen = AppScreen.DEVICE_SECURITY
    ),
    FeatureRowItem(
      id = "url_scanner",
      title = "Safe Website & URL Analyzer",
      subtitle = "SSRF guard, domain reputation & phishing check",
      category = FeatureCategory.SECURITY,
      icon = Icons.Default.Link,
      targetScreen = AppScreen.URL_SCANNER
    ),
    FeatureRowItem(
      id = "encrypted_vault",
      title = "Encrypted Vault",
      subtitle = "Store confidential notes & credentials",
      category = FeatureCategory.PRIVACY,
      icon = Icons.Default.Lock,
      targetScreen = AppScreen.PRIVACY_VAULT
    ),
    FeatureRowItem(
      id = "password_manager",
      title = "Password Manager & Auditor",
      subtitle = "Entropy analyzer and secure pass generator",
      category = FeatureCategory.PRIVACY,
      icon = Icons.Default.Key,
      targetScreen = AppScreen.PRIVACY_VAULT
    ),
    FeatureRowItem(
      id = "crypto_wallet",
      title = "Crypto & Asset Protection",
      subtitle = "Verify contract addresses & phishing drainers",
      category = FeatureCategory.CRYPTO,
      icon = Icons.Default.CurrencyBitcoin,
      targetScreen = AppScreen.CRYPTO_SECURITY
    ),
    FeatureRowItem(
      id = "device_health",
      title = "Device Telemetry & Health",
      subtitle = "RAM usage, thermal battery & internal storage",
      category = FeatureCategory.UTILITIES,
      icon = Icons.Default.Security,
      targetScreen = AppScreen.DEVICE_HEALTH
    )
  )

  val filteredFeatures = allFeatures.filter { item ->
    val matchesCategory = selectedCategory == FeatureCategory.ALL || item.category == selectedCategory
    val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
      item.subtitle.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CyberBackground)
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(16.dp))

    // Header
    Text(
      text = "Features",
      fontSize = 24.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Search Field
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search features...", color = TextMuted, fontSize = 14.sp) },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
      },
      singleLine = true,
      shape = RoundedCornerShape(14.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CyberSurface,
        unfocusedContainerColor = CyberSurface,
        focusedBorderColor = NeonEmerald,
        unfocusedBorderColor = CyberBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("features_search_input")
    )

    Spacer(modifier = Modifier.height(14.dp))

    // Filter Chips Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      val categories = listOf(
        Pair(FeatureCategory.ALL, "All"),
        Pair(FeatureCategory.SECURITY, "Security"),
        Pair(FeatureCategory.PRIVACY, "Privacy"),
        Pair(FeatureCategory.UTILITIES, "Utilities"),
        Pair(FeatureCategory.CRYPTO, "Crypto")
      )
      items(categories) { (cat, label) ->
        val isSelected = selectedCategory == cat
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) NeonEmerald else CyberSurface)
            .border(
              1.dp,
              if (isSelected) NeonEmerald else CyberBorder,
              RoundedCornerShape(20.dp)
            )
            .clickable { selectedCategory = cat }
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .testTag("filter_chip_${label.lowercase()}")
        ) {
          Text(
            text = label,
            color = if (isSelected) CyberBackground else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Features List
    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .padding(bottom = 76.dp)
    ) {
      items(filteredFeatures) { item ->
        FeatureListCard(
          item = item,
          onClick = { onNavigate(item.targetScreen) }
        )
      }
    }
  }
}

@Composable
fun FeatureListCard(
  item: FeatureRowItem,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(16.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .clickable { onClick() }
      .padding(14.dp)
      .testTag("feature_card_${item.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF0F2538)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = item.icon,
          contentDescription = item.title,
          tint = NeonEmerald,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = item.subtitle,
          color = TextSecondary,
          fontSize = 12.sp,
          maxLines = 1
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Open",
        tint = TextMuted,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
