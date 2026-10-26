package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.QuickScanModal
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.ShieldXBottomBar
import com.example.ui.screens.activity.ActivityScreen
import com.example.ui.screens.ai.AiAssistantScreen
import com.example.ui.screens.communication.CommunicationShieldScreen
import com.example.ui.screens.crypto.CryptoSecurityScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.features.FeaturesScreen
import com.example.ui.screens.health.DeviceHealthScreen
import com.example.ui.screens.integrity.DeviceSecurityScreen
import com.example.ui.screens.learn.LearningCenterScreen
import com.example.ui.screens.markets.CryptoMarketsScreen
import com.example.ui.screens.profile.ProfileSettingsScreen
import com.example.ui.screens.urlscan.UrlScannerScreen
import com.example.ui.screens.vault.PrivacyVaultScreen
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.ShieldXTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ShieldXTheme {
        ShieldXApp()
      }
    }
  }
}

@Composable
fun ShieldXApp(viewModel: MainViewModel = viewModel()) {
  val currentScreen by viewModel.currentScreen.collectAsState()

  val marketCoins by viewModel.marketCoins.collectAsState()
  val selectedCoin by viewModel.selectedCoin.collectAsState()
  val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
  val selectedCoinCandles by viewModel.selectedCoinCandles.collectAsState()
  val marketCurrency by viewModel.marketCurrency.collectAsState()
  val usdToInrRate by viewModel.usdToInrRate.collectAsState()
  val marketsLastUpdated by viewModel.marketsLastUpdated.collectAsState()
  val marketSourceLabel by viewModel.marketSourceLabel.collectAsState()
  val marketErrorBanner by viewModel.marketErrorBanner.collectAsState()
  val isLoadingMarkets by viewModel.isLoadingMarkets.collectAsState()
  val isLoadingCandles by viewModel.isLoadingCandles.collectAsState()
  val watchlistDocs by viewModel.watchlistDocs.collectAsState()
  val portfolioDocs by viewModel.portfolioDocs.collectAsState()

  val aiMessages by viewModel.aiMessages.collectAsState()
  val selectedAiModel by viewModel.selectedAiModel.collectAsState()
  val enableSearchGrounding by viewModel.enableSearchGrounding.collectAsState()
  val isAiSending by viewModel.isAiSending.collectAsState()

  // Handle Android system back gesture to navigate back inside sub-screens
  BackHandler(enabled = currentScreen != AppScreen.HOME) {
    if (!viewModel.navigateBack()) {
      viewModel.navigateTo(AppScreen.HOME)
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    containerColor = CyberBackground,
    bottomBar = {
      val isMainTab = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.FEATURES,
        AppScreen.ACTIVITY,
        AppScreen.PROFILE
      )
      if (isMainTab) {
        ShieldXBottomBar(
          currentScreen = currentScreen,
          onNavigate = { screen -> viewModel.navigateTo(screen) },
          onQuickScanClick = { viewModel.triggerQuickScan() }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(CyberBackground)
    ) {
      when (currentScreen) {
        AppScreen.HOME -> DashboardScreen(
          viewModel = viewModel,
          onNavigate = { screen -> viewModel.navigateTo(screen) }
        )

        AppScreen.FEATURES -> FeaturesScreen(
          onNavigate = { screen -> viewModel.navigateTo(screen) }
        )

        AppScreen.ACTIVITY, AppScreen.NOTIFICATIONS_MODAL -> ActivityScreen(
          viewModel = viewModel
        )

        AppScreen.PROFILE -> ProfileSettingsScreen(
          viewModel = viewModel,
          onNavigate = { screen -> viewModel.navigateTo(screen) }
        )

        AppScreen.URL_SCANNER -> UrlScannerScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.DEVICE_SECURITY -> DeviceSecurityScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.DEVICE_HEALTH -> DeviceHealthScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.PRIVACY_VAULT -> PrivacyVaultScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.COMMUNICATION_SHIELD -> CommunicationShieldScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.CRYPTO_SECURITY -> CryptoSecurityScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )

        AppScreen.CRYPTO_MARKETS -> {
          Column(modifier = Modifier.fillMaxSize()) {
            SubScreenBackHeader(
              title = "Live Crypto Markets & Charts",
              onBack = {
                if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME)
              }
            )
            CryptoMarketsScreen(
              coins = marketCoins,
              selectedCoin = selectedCoin,
              candles = selectedCoinCandles,
              selectedTimeframe = selectedTimeframe,
              currency = marketCurrency,
              usdToInrRate = usdToInrRate,
              lastUpdatedMillis = marketsLastUpdated,
              sourceLabel = marketSourceLabel,
              errorBanner = marketErrorBanner,
              isLoadingMarkets = isLoadingMarkets,
              isLoadingCandles = isLoadingCandles,
              watchlistDocs = watchlistDocs,
              portfolioDocs = portfolioDocs,
              onSelectCoin = { viewModel.selectCryptoCoin(it) },
              onSelectTimeframe = { viewModel.selectChartTimeframe(it) },
              onToggleCurrency = { viewModel.setMarketCurrency(it) },
              onRefreshMarkets = { viewModel.refreshCryptoMarkets(forceRefresh = true) },
              onToggleWatchlist = { viewModel.toggleWatchlistCoin(it) },
              onAddPortfolioHolding = { coin, amt, price ->
                viewModel.addPortfolioHolding(coin, amt, price)
              },
              onDeletePortfolioHolding = { viewModel.deletePortfolioHolding(it) },
              onAskAiAboutCoin = { viewModel.askAiAboutCoin(it) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        AppScreen.AI_ASSISTANT -> {
          Column(modifier = Modifier.fillMaxSize()) {
            SubScreenBackHeader(
              title = "Gemini AI Threat & Market Analyst",
              onBack = {
                if (!viewModel.navigateBack()) viewModel.navigateTo(AppScreen.HOME)
              }
            )
            AiAssistantScreen(
              messages = aiMessages,
              selectedModel = selectedAiModel,
              enableSearchGrounding = enableSearchGrounding,
              isSending = isAiSending,
              isApiKeyConfigured = viewModel.isGeminiApiKeyConfigured,
              onSelectModel = { viewModel.selectAiModel(it) },
              onToggleSearchGrounding = { viewModel.setSearchGroundingEnabled(it) },
              onSendMessage = { viewModel.sendAiPrompt(it) },
              onClearHistory = { viewModel.clearAiChatHistory() },
              modifier = Modifier.weight(1f)
            )
          }
        }

        AppScreen.LEARNING_CENTER -> LearningCenterScreen(
          viewModel = viewModel,
          onBack = { viewModel.navigateBack() }
        )
      }

      // Animated Full System Security Audit Modal
      QuickScanModal(viewModel = viewModel)
    }
  }
}

@Composable
private fun SubScreenBackHeader(
  title: String,
  onBack: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(CyberSurface)
      .padding(horizontal = 8.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onBack) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Navigate back",
        tint = CyberCyan
      )
    }
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = TextPrimary,
      fontWeight = FontWeight.Bold
    )
  }
}
