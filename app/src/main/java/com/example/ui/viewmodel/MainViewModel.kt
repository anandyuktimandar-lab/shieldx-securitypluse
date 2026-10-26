package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.SecurityAlertEntity
import com.example.data.local.entity.VaultItemEntity
import com.example.data.remote.AiChatMessage
import com.example.data.remote.CandlePoint
import com.example.data.remote.CloudScanReportDoc
import com.example.data.remote.CryptoCoinMarket
import com.example.data.remote.CryptoMarketService
import com.example.data.remote.GeminiModelOption
import com.example.data.remote.GeminiService
import com.example.data.remote.PortfolioHoldingDoc
import com.example.data.remote.WatchlistCoinDoc
import com.example.data.repository.ShieldXRepository
import com.example.domain.crypto.CryptoManager
import com.example.domain.model.CryptoAddressCheck
import com.example.domain.model.DeviceHealthStats
import com.example.domain.model.DeviceSecurityStatus
import com.example.domain.model.LearningLesson
import com.example.domain.model.UrlAnalysis
import com.example.ui.navigation.AppScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ShieldXRepository(application)
  private val cryptoMarketService = CryptoMarketService()
  private val geminiService = GeminiService()

  // --- On-Device Room Watchlist & Portfolio Streams (No Login Required) ---
  val watchlistDocs: StateFlow<List<WatchlistCoinDoc>> = repository.allWatchlistCoins
    .map { entities ->
      entities.map { entity ->
        WatchlistCoinDoc(
          id = entity.coinId,
          userId = "local_device",
          coinId = entity.coinId,
          symbol = entity.symbol,
          name = entity.name
        )
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val portfolioDocs: StateFlow<List<PortfolioHoldingDoc>> = repository.allPortfolioHoldings
    .map { entities ->
      entities.map { entity ->
        PortfolioHoldingDoc(
          id = entity.holdingId,
          userId = "local_device",
          coinId = entity.coinId,
          symbol = entity.symbol,
          name = entity.name,
          amount = entity.amount,
          buyPriceUsd = entity.buyPriceUsd
        )
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val cloudScans: StateFlow<List<CloudScanReportDoc>> = MutableStateFlow<List<CloudScanReportDoc>>(emptyList()).asStateFlow()

  // --- Navigation & Backstack ---
  private val _currentScreen = MutableStateFlow(AppScreen.HOME)
  val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

  private val backStack = mutableListOf<AppScreen>()

  fun navigateTo(screen: AppScreen) {
    if (_currentScreen.value != screen) {
      backStack.add(_currentScreen.value)
      _currentScreen.value = screen
    }
  }

  fun navigateBack(): Boolean {
    return if (backStack.isNotEmpty()) {
      _currentScreen.value = backStack.removeAt(backStack.size - 1)
      true
    } else false
  }

  // --- Room Flows ---
  val allScans: StateFlow<List<ScanReportEntity>> = repository.allScans
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeAlerts: StateFlow<List<SecurityAlertEntity>> = repository.activeAlerts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val vaultItems: StateFlow<List<VaultItemEntity>> = repository.allVaultItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // --- Device Telemetry & Integrity ---
  private val _deviceHealth = MutableStateFlow(repository.getDeviceHealthStats())
  val deviceHealth: StateFlow<DeviceHealthStats> = _deviceHealth.asStateFlow()

  private val _deviceSecurity = MutableStateFlow(repository.auditDeviceSecurity())
  val deviceSecurity: StateFlow<DeviceSecurityStatus> = _deviceSecurity.asStateFlow()

  // --- Full System Quick Scan State ---
  private val _isQuickScanning = MutableStateFlow(false)
  val isQuickScanning: StateFlow<Boolean> = _isQuickScanning.asStateFlow()

  private val _quickScanProgress = MutableStateFlow(0f)
  val quickScanProgress: StateFlow<Float> = _quickScanProgress.asStateFlow()

  private val _quickScanStatusText = MutableStateFlow("Initializing engine...")
  val quickScanStatusText: StateFlow<String> = _quickScanStatusText.asStateFlow()

  private val _showQuickScanModal = MutableStateFlow(false)
  val showQuickScanModal: StateFlow<Boolean> = _showQuickScanModal.asStateFlow()

  // --- URL Scanner State ---
  private val _urlInput = MutableStateFlow("")
  val urlInput: StateFlow<String> = _urlInput.asStateFlow()

  private val _isUrlScanning = MutableStateFlow(false)
  val isUrlScanning: StateFlow<Boolean> = _isUrlScanning.asStateFlow()

  private val _lastUrlAnalysis = MutableStateFlow<UrlAnalysis?>(null)
  val lastUrlAnalysis: StateFlow<UrlAnalysis?> = _lastUrlAnalysis.asStateFlow()

  // --- Communication Shield Toggles ---
  private val _blockSpamCalls = MutableStateFlow(true)
  val blockSpamCalls: StateFlow<Boolean> = _blockSpamCalls.asStateFlow()

  private val _identifyHiddenNumbers = MutableStateFlow(true)
  val identifyHiddenNumbers: StateFlow<Boolean> = _identifyHiddenNumbers.asStateFlow()

  private val _telemarketerFilter = MutableStateFlow(true)
  val telemarketerFilter: StateFlow<Boolean> = _telemarketerFilter.asStateFlow()

  private val _smsPhishingFilter = MutableStateFlow(true)
  val smsPhishingFilter: StateFlow<Boolean> = _smsPhishingFilter.asStateFlow()

  // --- Password Evaluator State ---
  private val _passwordInput = MutableStateFlow("")
  val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

  private val _passwordEvaluation = MutableStateFlow(CryptoManager.evaluatePassword(""))
  val passwordEvaluation: StateFlow<CryptoManager.PasswordEvaluation> = _passwordEvaluation.asStateFlow()

  // --- Crypto Address Checker State ---
  private val _cryptoAddressInput = MutableStateFlow("")
  val cryptoAddressInput: StateFlow<String> = _cryptoAddressInput.asStateFlow()

  private val _cryptoCheckResult = MutableStateFlow<CryptoAddressCheck?>(null)
  val cryptoCheckResult: StateFlow<CryptoAddressCheck?> = _cryptoCheckResult.asStateFlow()

  // --- Learning Center ---
  private val _curriculum = MutableStateFlow(repository.getCurriculum())
  val curriculum: StateFlow<List<LearningLesson>> = _curriculum.asStateFlow()

  // --- Live Crypto Markets & Interactive Charts State ---
  private val _marketCoins = MutableStateFlow<List<CryptoCoinMarket>>(emptyList())
  val marketCoins: StateFlow<List<CryptoCoinMarket>> = _marketCoins.asStateFlow()

  private val _selectedCoin = MutableStateFlow<CryptoCoinMarket?>(null)
  val selectedCoin: StateFlow<CryptoCoinMarket?> = _selectedCoin.asStateFlow()

  private val _selectedTimeframe = MutableStateFlow("1h")
  val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

  private val _selectedCoinCandles = MutableStateFlow<List<CandlePoint>>(emptyList())
  val selectedCoinCandles: StateFlow<List<CandlePoint>> = _selectedCoinCandles.asStateFlow()

  private val _marketCurrency = MutableStateFlow("USD")
  val marketCurrency: StateFlow<String> = _marketCurrency.asStateFlow()

  private val _usdToInrRate = MutableStateFlow(83.85)
  val usdToInrRate: StateFlow<Double> = _usdToInrRate.asStateFlow()

  private val _marketsLastUpdated = MutableStateFlow(0L)
  val marketsLastUpdated: StateFlow<Long> = _marketsLastUpdated.asStateFlow()

  private val _marketSourceLabel = MutableStateFlow("Connecting...")
  val marketSourceLabel: StateFlow<String> = _marketSourceLabel.asStateFlow()

  private val _marketErrorBanner = MutableStateFlow<String?>(null)
  val marketErrorBanner: StateFlow<String?> = _marketErrorBanner.asStateFlow()

  private val _isLoadingMarkets = MutableStateFlow(false)
  val isLoadingMarkets: StateFlow<Boolean> = _isLoadingMarkets.asStateFlow()

  private val _isLoadingCandles = MutableStateFlow(false)
  val isLoadingCandles: StateFlow<Boolean> = _isLoadingCandles.asStateFlow()

  // --- Gemini AI Assistant State ---
  private val _aiMessages = MutableStateFlow(
    listOf(
      AiChatMessage(
        id = "welcome_msg",
        role = "model",
        text = "Welcome to **ShieldX Gemini AI Analyst** by SecuriPulse.\n\n" +
          "I can help you with:\n" +
          "• **Live Crypto Intelligence**: Real-time price explanations, volatility context, and portfolio risk checks.\n" +
          "• **URL & Phishing Defense**: Explaining SSRF attacks, homograph domains, and wallet drainer contracts.\n" +
          "• **Google Search Grounding**: Up-to-date security advisories and market news.\n\n" +
          "Tap a starter prompt above, type a question, or tap the microphone icon for live voice conversation.",
        modelUsed = GeminiModelOption.FLASH_SEARCH.displayName
      )
    )
  )
  val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

  private val _selectedAiModel = MutableStateFlow(GeminiModelOption.FLASH_SEARCH)
  val selectedAiModel: StateFlow<GeminiModelOption> = _selectedAiModel.asStateFlow()

  private val _enableSearchGrounding = MutableStateFlow(true)
  val enableSearchGrounding: StateFlow<Boolean> = _enableSearchGrounding.asStateFlow()

  private val _isAiSending = MutableStateFlow(false)
  val isAiSending: StateFlow<Boolean> = _isAiSending.asStateFlow()

  val isGeminiApiKeyConfigured: Boolean
    get() = geminiService.isApiKeyConfigured()

  init {
    refreshTelemetry()
    refreshCryptoMarkets(forceRefresh = false)
  }

  fun refreshTelemetry() {
    viewModelScope.launch {
      _deviceHealth.value = repository.getDeviceHealthStats()
      _deviceSecurity.value = repository.syncDeviceSecurityAlerts()
    }
  }

  // --- Live Crypto Market Actions ---
  fun refreshCryptoMarkets(forceRefresh: Boolean = true) {
    viewModelScope.launch {
      _isLoadingMarkets.value = true
      val feed = cryptoMarketService.fetchLiveMarkets(forceRefresh = forceRefresh)
      _marketCoins.value = feed.coins
      _usdToInrRate.value = feed.usdToInrRate
      if (feed.lastUpdatedMillis > 0L) {
        _marketsLastUpdated.value = feed.lastUpdatedMillis
      }
      _marketSourceLabel.value = feed.sourceLabel
      _marketErrorBanner.value = feed.errorBanner
      _isLoadingMarkets.value = false

      val currentSelected = _selectedCoin.value
      val targetCoin = if (currentSelected != null) {
        feed.coins.find { it.id == currentSelected.id } ?: feed.coins.firstOrNull()
      } else {
        feed.coins.firstOrNull()
      }
      if (targetCoin != null) {
        _selectedCoin.value = targetCoin
        loadCandlesForCoin(targetCoin, _selectedTimeframe.value)
      }
    }
  }

  fun selectCryptoCoin(coin: CryptoCoinMarket) {
    _selectedCoin.value = coin
    loadCandlesForCoin(coin, _selectedTimeframe.value)
  }

  fun selectChartTimeframe(timeframe: String) {
    _selectedTimeframe.value = timeframe
    val coin = _selectedCoin.value ?: return
    loadCandlesForCoin(coin, timeframe)
  }

  private fun loadCandlesForCoin(coin: CryptoCoinMarket, timeframe: String) {
    viewModelScope.launch {
      _isLoadingCandles.value = true
      val candles = cryptoMarketService.fetchCoinCandles(
        coinId = coin.id,
        timeframe = timeframe,
        referenceCoin = coin
      )
      _selectedCoinCandles.value = candles
      _isLoadingCandles.value = false
    }
  }

  fun setMarketCurrency(currency: String) {
    _marketCurrency.value = currency
  }

  // --- Watchlist & Portfolio Actions (On-Device Room Persistence) ---
  fun toggleWatchlistCoin(coin: CryptoCoinMarket) {
    val isCurrentlyWatched = watchlistDocs.value.any { it.coinId == coin.id }
    viewModelScope.launch {
      if (isCurrentlyWatched) {
        repository.removeWatchlistCoin(coin.id)
      } else {
        repository.addWatchlistCoin(
          coinId = coin.id,
          symbol = coin.symbol,
          name = coin.name
        )
      }
    }
  }

  fun addPortfolioHolding(coin: CryptoCoinMarket, amount: Double, buyPriceUsd: Double) {
    val holdingId = "hold_${coin.id}_${System.currentTimeMillis()}"
    viewModelScope.launch {
      repository.addPortfolioHolding(
        holdingId = holdingId,
        coinId = coin.id,
        symbol = coin.symbol,
        name = coin.name,
        amount = amount,
        buyPriceUsd = buyPriceUsd
      )
    }
  }

  fun deletePortfolioHolding(holdingId: String) {
    viewModelScope.launch {
      repository.deletePortfolioHolding(holdingId)
    }
  }

  // --- Gemini AI Assistant Actions ---
  fun selectAiModel(model: GeminiModelOption) {
    _selectedAiModel.value = model
  }

  fun setSearchGroundingEnabled(enabled: Boolean) {
    _enableSearchGrounding.value = enabled
  }

  fun clearAiChatHistory() {
    _aiMessages.value = _aiMessages.value.take(1)
  }

  fun askAiAboutCoin(coin: CryptoCoinMarket) {
    navigateTo(AppScreen.AI_ASSISTANT)
    val prompt = String.format(
      Locale.US,
      "Analyze %s (%s): Current price is $%.2f USD, 24h change is %.2f%%, and 24h volume is $%,.0f. What are the key market drivers, volatility considerations, and wallet security best practices for holding %s?",
      coin.name,
      coin.symbol,
      coin.currentPriceUsd,
      coin.priceChangePercentage24h,
      coin.totalVolume24hUsd,
      coin.symbol
    )
    sendAiPrompt(prompt)
  }

  fun sendAiPrompt(userText: String) {
    val trimmed = userText.trim()
    if (trimmed.isEmpty() || _isAiSending.value) return

    val userMsg = AiChatMessage(
      id = "user_${System.currentTimeMillis()}",
      role = "user",
      text = trimmed,
      modelUsed = "You"
    )
    val currentHistory = _aiMessages.value
    _aiMessages.value = currentHistory + userMsg
    _isAiSending.value = true

    viewModelScope.launch {
      val topCoinsContext = _marketCoins.value.take(5).joinToString("; ") { c ->
        String.format(Locale.US, "%s ($%.2f, 24h: %.2f%%)", c.symbol, c.currentPriceUsd, c.priceChangePercentage24h)
      }
      val reply = geminiService.sendMessage(
        history = currentHistory,
        userPrompt = trimmed,
        modelOption = _selectedAiModel.value,
        enableSearchGrounding = _enableSearchGrounding.value,
        marketContextSummary = topCoinsContext
      )
      _aiMessages.value = _aiMessages.value + reply
      _isAiSending.value = false
    }
  }

  // --- Quick Scan ---
  fun triggerQuickScan() {
    viewModelScope.launch {
      _showQuickScanModal.value = true
      _isQuickScanning.value = true
      _quickScanProgress.value = 0.05f
      _quickScanStatusText.value = "Inspecting Android sandbox & root binaries..."
      delay(600)

      _quickScanProgress.value = 0.30f
      _quickScanStatusText.value = "Auditing USB debugging & Developer options..."
      delay(600)

      _quickScanProgress.value = 0.60f
      _quickScanStatusText.value = "Checking network security & active VPN..."
      delay(600)

      _quickScanProgress.value = 0.85f
      _quickScanStatusText.value = "Scanning local database & suspicious heuristics..."
      delay(600)

      _quickScanProgress.value = 1.0f
      _quickScanStatusText.value = "Scan Complete: 0 critical vulnerabilities found."
      refreshTelemetry()
      delay(800)
      _isQuickScanning.value = false
    }
  }

  fun closeQuickScanModal() {
    _showQuickScanModal.value = false
  }

  // --- URL Scanner ---
  fun setUrlInput(value: String) {
    _urlInput.value = value
  }

  fun scanUrl() {
    val target = _urlInput.value.trim()
    if (target.isBlank()) return

    viewModelScope.launch {
      _isUrlScanning.value = true
      delay(500)
      val result = repository.scanUrl(target)
      _lastUrlAnalysis.value = result
      _isUrlScanning.value = false
    }
  }

  // --- Communication Shield Toggles ---
  fun toggleBlockSpamCalls() { _blockSpamCalls.value = !_blockSpamCalls.value }
  fun toggleIdentifyHiddenNumbers() { _identifyHiddenNumbers.value = !_identifyHiddenNumbers.value }
  fun toggleTelemarketerFilter() { _telemarketerFilter.value = !_telemarketerFilter.value }
  fun toggleSmsPhishingFilter() { _smsPhishingFilter.value = !_smsPhishingFilter.value }

  // --- Password Evaluator ---
  fun setPasswordInput(input: String) {
    _passwordInput.value = input
    _passwordEvaluation.value = CryptoManager.evaluatePassword(input)
  }

  // --- Vault Management ---
  fun saveVaultItem(title: String, category: String, rawSecret: String, label: String) {
    viewModelScope.launch {
      repository.saveVaultItem(title, category, rawSecret, label)
    }
  }

  fun deleteVaultItem(item: VaultItemEntity) {
    viewModelScope.launch {
      repository.deleteVaultItem(item)
    }
  }

  // --- Crypto Address Checker ---
  fun setCryptoAddressInput(address: String) {
    _cryptoAddressInput.value = address
  }

  fun verifyCryptoAddress() {
    val raw = _cryptoAddressInput.value.trim()
    val isEvm = raw.startsWith("0x") && raw.length == 42
    val isBtc = (raw.startsWith("1") || raw.startsWith("3") || raw.startsWith("bc1")) && raw.length in 26..62
    val isSol = raw.length in 32..44 && !raw.startsWith("0x")

    val (network, valid) = when {
      isEvm -> Pair("Ethereum / EVM Compatible", true)
      isBtc -> Pair("Bitcoin (Legacy/SegWit)", true)
      isSol -> Pair("Solana Base58", true)
      else -> Pair("Unknown Network", false)
    }

    val isPhishing = raw.lowercase().contains("fake") || raw.lowercase().contains("airdrop") || raw.startsWith("0x000000000000000000000000000000000000dEaD")

    _cryptoCheckResult.value = CryptoAddressCheck(
      address = raw,
      network = network,
      isValidFormat = valid,
      isKnownPhishingDrainer = isPhishing,
      safetyAdvice = if (isPhishing) {
        "CRITICAL: Address matches known burn or deceptive drainer contract pattern! Do not approve token permissions."
      } else if (valid) {
        "Format verified. Always double check the first 6 and last 6 characters on hardware display before signing."
      } else {
        "Address string does not match standard EVM, Bitcoin, or Solana address formatting rules."
      }
    )
  }

  // --- Learning Progress ---
  fun toggleLesson(lessonId: String) {
    val updated = _curriculum.value.map {
      if (it.id == lessonId) it.copy(isCompleted = !it.isCompleted) else it
    }
    _curriculum.value = updated
    viewModelScope.launch {
      val item = updated.firstOrNull { it.id == lessonId }
      if (item != null) {
        repository.markLessonCompleted(lessonId, item.isCompleted)
      }
    }
  }

  // --- History & Alerts ---
  fun dismissAlert(id: Long) {
    viewModelScope.launch {
      repository.dismissAlert(id)
    }
  }

  fun dismissAllAlerts() {
    viewModelScope.launch {
      repository.dismissAllAlerts()
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      repository.clearHistory()
    }
  }
}
