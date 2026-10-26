package com.example.ui.screens.markets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.CandlePoint
import com.example.data.remote.CryptoCoinMarket
import com.example.data.remote.PortfolioHoldingDoc
import com.example.data.remote.WatchlistCoinDoc
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MarketSubTab(val label: String) {
    LIVE_MARKETS("Live Market"),
    WATCHLIST("Watchlist"),
    PORTFOLIO("Portfolio")
}

@Composable
fun CryptoMarketsScreen(
    coins: List<CryptoCoinMarket>,
    selectedCoin: CryptoCoinMarket?,
    candles: List<CandlePoint>,
    selectedTimeframe: String,
    currency: String,
    usdToInrRate: Double,
    lastUpdatedMillis: Long,
    sourceLabel: String,
    errorBanner: String?,
    isLoadingMarkets: Boolean,
    isLoadingCandles: Boolean,
    watchlistDocs: List<WatchlistCoinDoc>,
    portfolioDocs: List<PortfolioHoldingDoc>,
    onSelectCoin: (CryptoCoinMarket) -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onToggleCurrency: (String) -> Unit,
    onRefreshMarkets: () -> Unit,
    onToggleWatchlist: (CryptoCoinMarket) -> Unit,
    onAddPortfolioHolding: (CryptoCoinMarket, Double, Double) -> Unit,
    onDeletePortfolioHolding: (String) -> Unit,
    onAskAiAboutCoin: (CryptoCoinMarket) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(MarketSubTab.LIVE_MARKETS) }
    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("RANK") } // RANK, PRICE, CHANGE
    var showAddHoldingDialog by remember { mutableStateOf(false) }

    val watchlistIds = remember(watchlistDocs) { watchlistDocs.map { it.coinId }.toSet() }
    val rateMultiplier = if (currency.equals("INR", ignoreCase = true)) usdToInrRate else 1.0
    val currencySymbol = if (currency.equals("INR", ignoreCase = true)) "₹" else "$"

    val filteredCoins = remember(coins, searchQuery, sortBy, activeTab, watchlistIds) {
        val base = if (activeTab == MarketSubTab.WATCHLIST) {
            coins.filter { watchlistIds.contains(it.id) }
        } else {
            coins
        }
        val searched = if (searchQuery.isBlank()) {
            base
        } else {
            val q = searchQuery.trim().lowercase()
            base.filter { it.name.lowercase().contains(q) || it.symbol.lowercase().contains(q) }
        }
        when (sortBy) {
            "PRICE" -> searched.sortedByDescending { it.currentPriceUsd }
            "CHANGE" -> searched.sortedByDescending { it.priceChangePercentage24h }
            else -> searched.sortedBy { it.rank }
        }
    }

    if (showAddHoldingDialog && selectedCoin != null) {
        AddPortfolioHoldingDialog(
            coin = selectedCoin,
            defaultBuyPriceUsd = selectedCoin.currentPriceUsd,
            onDismiss = { showAddHoldingDialog = false },
            onConfirm = { amount, buyPriceUsd ->
                onAddPortfolioHolding(selectedCoin, amount, buyPriceUsd)
                showAddHoldingDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header + USD/INR toggle + Refresh
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Crypto Market Intelligence",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    val timeStr = if (lastUpdatedMillis > 0L) {
                        SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(lastUpdatedMillis))
                    } else {
                        "Syncing..."
                    }
                    Text(
                        text = "Source: $sourceLabel • Updated: $timeStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Currency Toggle (USD / INR)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CyberCardSurface,
                        modifier = Modifier
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                onToggleCurrency(if (currency == "USD") "INR" else "USD")
                            }
                            .testTag("currency_toggle_button")
                    ) {
                        Text(
                            text = if (currency == "INR") "INR (₹)" else "USD ($)",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    IconButton(
                        onClick = onRefreshMarkets,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CyberCardSurface)
                            .testTag("refresh_markets_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh live prices",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }

        // Error or cache notice banner
        if (!errorBanner.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberWarning.copy(alpha = 0.14f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Feed status",
                            tint = CyberWarning
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorBanner,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextPrimary
                        )
                    }
                }
            }
        }

        // Interactive Chart for Selected Coin
        if (selectedCoin != null) {
            item {
                InteractiveCryptoChartCard(
                    coin = selectedCoin,
                    candles = candles,
                    selectedTimeframe = selectedTimeframe,
                    onSelectTimeframe = onSelectTimeframe,
                    currency = currency,
                    usdToInrRate = usdToInrRate,
                    isLoading = isLoadingCandles
                )
            }

            // Quick Action Row for Selected Coin (Watchlist + Add Holding + Ask Gemini AI)
            item {
                val isWatched = watchlistIds.contains(selectedCoin.id)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleWatchlist(selectedCoin) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isWatched) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Watchlist",
                            tint = if (isWatched) CyberWarning else CyberTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isWatched) "Watched" else "Watchlist",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberTextPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { showAddHoldingDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddChart,
                            contentDescription = "Add to Portfolio",
                            tint = CyberGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+ Portfolio",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberTextPrimary
                        )
                    }

                    Button(
                        onClick = { onAskAiAboutCoin(selectedCoin) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberNavyDark
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask AI",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask AI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sub-Tab Selector (Live Market / Watchlist / Portfolio)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarketSubTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Button(
                        onClick = { activeTab = tab },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) CyberBlue else CyberCardSurface,
                            contentColor = if (selected) Color.White else CyberTextSecondary
                        )
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (activeTab == MarketSubTab.PORTFOLIO) {
            // Cloud Portfolio Tracker Summary + Holdings
            item {
                PortfolioSummaryCard(
                    holdings = portfolioDocs,
                    coinsById = coins.associateBy { it.id },
                    currencySymbol = currencySymbol,
                    rateMultiplier = rateMultiplier
                )
            }

            if (portfolioDocs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Empty portfolio",
                                tint = CyberTextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your Cloud Portfolio is empty",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Select any cryptocurrency above and tap '+ Portfolio' to track your holdings across devices with Firestore sync.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(portfolioDocs, key = { it.id }) { holding ->
                    val liveCoin = coins.find { it.id == holding.coinId }
                    val currentUsd = liveCoin?.currentPriceUsd ?: holding.buyPriceUsd
                    val totalValue = holding.amount * currentUsd * rateMultiplier
                    val costBasis = holding.amount * holding.buyPriceUsd * rateMultiplier
                    val pnl = totalValue - costBasis
                    val isProfit = pnl >= 0.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${holding.name} (${holding.symbol})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${holding.amount} ${holding.symbol} • Avg Buy: $currencySymbol${formatPrice(holding.buyPriceUsd * rateMultiplier)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberTextSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${formatPrice(totalValue)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%s%s%s",
                                        if (isProfit) "+" else "-",
                                        currencySymbol,
                                        formatPrice(kotlin.math.abs(pnl))
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isProfit) CyberGreen else CyberDanger,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(onClick = { onDeletePortfolioHolding(holding.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove holding",
                                    tint = CyberDanger
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Search & Sort Controls
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search coin name or ticker (e.g., BTC, SOL)...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search coins",
                                tint = CyberTextSecondary
                            )
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("crypto_search_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sort by:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextSecondary
                        )
                        listOf("RANK" to "Market Cap", "PRICE" to "Price", "CHANGE" to "24h %").forEach { (key, label) ->
                            FilterChip(
                                selected = sortBy == key,
                                onClick = { sortBy = key },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            if (isLoadingMarkets && filteredCoins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CyberCyan)
                    }
                }
            } else if (filteredCoins.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
                    ) {
                        Text(
                            text = if (activeTab == MarketSubTab.WATCHLIST) {
                                "No coins in your Cloud Watchlist yet. Tap the star icon on any coin to sync it."
                            } else {
                                "No matching coins found."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextSecondary,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            } else {
                items(filteredCoins, key = { it.id }) { coin ->
                    val isSelected = selectedCoin?.id == coin.id
                    val isWatched = watchlistIds.contains(coin.id)
                    val isPositive = coin.priceChangePercentage24h >= 0.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyberCyan else CyberBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onSelectCoin(coin) }
                            .testTag("coin_row_${coin.symbol}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onToggleWatchlist(coin) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (isWatched) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Toggle Watchlist",
                                    tint = if (isWatched) CyberWarning else CyberTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = coin.symbol,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = CyberTextPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = coin.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyberTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "MCap: ${formatCompactNumber(coin.marketCapInCurrency(currency, usdToInrRate), currencySymbol)} • Vol: ${formatCompactNumber(coin.volumeInCurrency(currency, usdToInrRate), currencySymbol)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberTextMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${formatPrice(coin.priceInCurrency(currency, usdToInrRate))}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(
                                        Locale.US,
                                        "%s%.2f%%",
                                        if (isPositive) "+" else "",
                                        coin.priceChangePercentage24h
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPositive) CyberGreen else CyberDanger,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(
    holdings: List<PortfolioHoldingDoc>,
    coinsById: Map<String, CryptoCoinMarket>,
    currencySymbol: String,
    rateMultiplier: Double
) {
    var totalCurrent = 0.0
    var totalCost = 0.0
    holdings.forEach { h ->
        val liveUsd = coinsById[h.coinId]?.currentPriceUsd ?: h.buyPriceUsd
        totalCurrent += h.amount * liveUsd * rateMultiplier
        totalCost += h.amount * h.buyPriceUsd * rateMultiplier
    }
    val pnl = totalCurrent - totalCost
    val pnlPct = if (totalCost > 0) (pnl / totalCost) * 100.0 else 0.0
    val isProfit = pnl >= 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Cloud Synced Portfolio Value",
                style = MaterialTheme.typography.labelMedium,
                color = CyberTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$currencySymbol${formatPrice(totalCurrent)}",
                style = MaterialTheme.typography.headlineMedium,
                color = CyberTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = String.format(
                    Locale.US,
                    "Total P&L: %s%s%s (%.2f%%)",
                    if (isProfit) "+" else "-",
                    currencySymbol,
                    formatPrice(kotlin.math.abs(pnl)),
                    pnlPct
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (isProfit) CyberGreen else CyberDanger,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddPortfolioHoldingDialog(
    coin: CryptoCoinMarket,
    defaultBuyPriceUsd: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("1.0") }
    var buyPriceInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", defaultBuyPriceUsd)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add ${coin.name} (${coin.symbol})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Quantity (${coin.symbol})") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = buyPriceInput,
                    onValueChange = { buyPriceInput = it },
                    label = { Text("Buy Price (USD)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    val price = buyPriceInput.toDoubleOrNull() ?: defaultBuyPriceUsd
                    if (amt > 0.0 && price >= 0.0) {
                        onConfirm(amt, price)
                    }
                }
            ) {
                Text("Save to Cloud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
