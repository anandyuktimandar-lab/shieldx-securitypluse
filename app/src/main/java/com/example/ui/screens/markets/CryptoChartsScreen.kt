package com.example.ui.screens.markets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.remote.CandlePoint
import com.example.data.remote.CryptoCoinMarket
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

enum class ChartDisplayMode(val label: String) {
    CANDLESTICK("Candles"),
    LINE("Line"),
    AREA("Area")
}

val SUPPORTED_TIMEFRAMES = listOf("1m", "5m", "15m", "1h", "4h", "1d", "1w")

@Composable
fun InteractiveCryptoChartCard(
    coin: CryptoCoinMarket,
    candles: List<CandlePoint>,
    selectedTimeframe: String,
    onSelectTimeframe: (String) -> Unit,
    currency: String,
    usdToInrRate: Double,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    var displayMode by remember { mutableStateOf(ChartDisplayMode.CANDLESTICK) }
    var showVolume by remember { mutableStateOf(true) }
    var visibleCount by remember(candles.size) {
        mutableIntStateOf(candles.size.coerceIn(12, 42))
    }

    val rateMultiplier = if (currency.equals("INR", ignoreCase = true)) usdToInrRate else 1.0
    val currencySymbol = if (currency.equals("INR", ignoreCase = true)) "₹" else "$"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row: Coin name + current price + 24h delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${coin.name} (${coin.symbol})",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberCyan.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "#${coin.rank}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${formatPrice(coin.currentPriceUsd * rateMultiplier)}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CyberTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val isPositive = coin.priceChangePercentage24h >= 0.0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (isPositive) CyberGreen else CyberDanger).copy(alpha = 0.16f)
                ) {
                    Text(
                        text = String.format(
                            Locale.US,
                            "%s%.2f%% (24h)",
                            if (isPositive) "+" else "",
                            coin.priceChangePercentage24h
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPositive) CyberGreen else CyberDanger,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Mode & Volume toggles + Zoom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartDisplayMode.entries.forEach { mode ->
                    val selected = displayMode == mode
                    FilterChip(
                        selected = selected,
                        onClick = { displayMode = mode },
                        label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan.copy(alpha = 0.22f),
                            selectedLabelColor = CyberCyan
                        )
                    )
                }

                FilterChip(
                    selected = showVolume,
                    onClick = { showVolume = !showVolume },
                    label = { Text("Vol", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPurple.copy(alpha = 0.22f),
                        selectedLabelColor = CyberCyan
                    )
                )

                IconButton(
                    onClick = {
                        visibleCount = (visibleCount - 6).coerceAtLeast(8)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom in chart",
                        tint = CyberTextSecondary
                    )
                }

                IconButton(
                    onClick = {
                        visibleCount = (visibleCount + 6).coerceAtMost(max(12, candles.size))
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom out chart",
                        tint = CyberTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeframe Selector (1m, 5m, 15m, 1h, 4h, 1d, 1w)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SUPPORTED_TIMEFRAMES.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) CyberBlue else CyberNavyDark
                            )
                            .clickable { onSelectTimeframe(tf) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("chart_tf_$tf"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tf.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else CyberTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Canvas
            if (isLoading && candles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyberCyan)
                }
            } else if (candles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No OHLC chart data available for ${coin.symbol}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextSecondary
                    )
                }
            } else {
                val slice = candles.takeLast(visibleCount.coerceAtLeast(6))
                val minLow = (slice.minOfOrNull { it.low } ?: coin.low24hUsd) * rateMultiplier
                val maxHigh = (slice.maxOfOrNull { it.high } ?: coin.high24hUsd) * rateMultiplier
                val maxVol = (slice.maxOfOrNull { it.volume } ?: 1.0).coerceAtLeast(1.0)
                val priceSpan = (maxHigh - minLow).coerceAtLeast(0.000001)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "High: $currencySymbol${formatPrice(maxHigh)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberGreen,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Low: $currencySymbol${formatPrice(minLow)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberWarning,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberNavyDark)
                            .pointerInput(candles.size) {
                                detectTransformGestures { _, _, zoom, _ ->
                                    if (zoom > 1.05f) {
                                        visibleCount = (visibleCount - 2).coerceAtLeast(8)
                                    } else if (zoom < 0.95f) {
                                        visibleCount = (visibleCount + 2).coerceAtMost(max(12, candles.size))
                                    }
                                }
                            }
                            .padding(8.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val chartH = if (showVolume) h * 0.74f else h * 0.92f
                        val volTop = chartH + (h * 0.05f)
                        val volH = (h - volTop).coerceAtLeast(10f)

                        // Horizontal grid lines
                        val gridSteps = 4
                        for (g in 0..gridSteps) {
                            val y = (chartH / gridSteps) * g
                            drawLine(
                                color = CyberBorder.copy(alpha = 0.45f),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                        }

                        val count = slice.size
                        val stepX = w / count.toFloat()

                        fun priceToY(rawUsd: Double): Float {
                            val scaled = rawUsd * rateMultiplier
                            val ratio = ((scaled - minLow) / priceSpan).toFloat().coerceIn(0f, 1f)
                            return chartH * (1f - ratio)
                        }

                        when (displayMode) {
                            ChartDisplayMode.CANDLESTICK -> {
                                val candleWidth = (stepX * 0.62f).coerceAtLeast(3f)
                                slice.forEachIndexed { idx, c ->
                                    val centerX = (idx + 0.5f) * stepX
                                    val isBull = c.close >= c.open
                                    val color = if (isBull) CyberGreen else CyberDanger

                                    val highY = priceToY(c.high)
                                    val lowY = priceToY(c.low)
                                    val openY = priceToY(c.open)
                                    val closeY = priceToY(c.close)

                                    // Wick
                                    drawLine(
                                        color = color,
                                        start = Offset(centerX, highY),
                                        end = Offset(centerX, lowY),
                                        strokeWidth = 2f
                                    )

                                    // Body
                                    val topY = min(openY, closeY)
                                    val bodyHeight = kotlin.math.abs(closeY - openY).coerceAtLeast(3f)
                                    drawRect(
                                        color = color,
                                        topLeft = Offset(centerX - candleWidth / 2f, topY),
                                        size = Size(candleWidth, bodyHeight)
                                    )
                                }
                            }

                            ChartDisplayMode.LINE, ChartDisplayMode.AREA -> {
                                val linePath = Path()
                                val areaPath = Path()
                                slice.forEachIndexed { idx, c ->
                                    val x = (idx + 0.5f) * stepX
                                    val y = priceToY(c.close)
                                    if (idx == 0) {
                                        linePath.moveTo(x, y)
                                        areaPath.moveTo(x, chartH)
                                        areaPath.lineTo(x, y)
                                    } else {
                                        linePath.lineTo(x, y)
                                        areaPath.lineTo(x, y)
                                    }
                                    if (idx == slice.lastIndex) {
                                        areaPath.lineTo(x, chartH)
                                        areaPath.close()
                                    }
                                }

                                if (displayMode == ChartDisplayMode.AREA) {
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                CyberCyan.copy(alpha = 0.35f),
                                                CyberCyan.copy(alpha = 0.02f)
                                            ),
                                            startY = 0f,
                                            endY = chartH
                                        )
                                    )
                                }

                                drawPath(
                                    path = linePath,
                                    color = CyberCyan,
                                    style = Stroke(width = 3.5f)
                                )
                            }
                        }

                        // Volume bars
                        if (showVolume) {
                            val barWidth = (stepX * 0.55f).coerceAtLeast(2f)
                            slice.forEachIndexed { idx, c ->
                                val centerX = (idx + 0.5f) * stepX
                                val isBull = c.close >= c.open
                                val volRatio = (c.volume / maxVol).toFloat().coerceIn(0.05f, 1f)
                                val barH = volH * volRatio
                                drawRect(
                                    color = (if (isBull) CyberGreen else CyberDanger).copy(alpha = 0.45f),
                                    topLeft = Offset(centerX - barWidth / 2f, volTop + (volH - barH)),
                                    size = Size(barWidth, barH)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val startFmt = SimpleDateFormat("MMM d, HH:mm", Locale.US)
                    val firstTs = slice.firstOrNull()?.timestampMillis ?: 0L
                    val lastTs = slice.lastOrNull()?.timestampMillis ?: 0L
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (firstTs > 0) startFmt.format(Date(firstTs)) else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                        Text(
                            text = "Pinch or tap +/- to zoom (${slice.size} bars)",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                        Text(
                            text = if (lastTs > 0) startFmt.format(Date(lastTs)) else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                    }
                }
            }
        }
    }
}

fun formatPrice(value: Double): String {
    return when {
        value >= 1000.0 -> String.format(Locale.US, "%,.2f", value)
        value >= 1.0 -> String.format(Locale.US, "%.2f", value)
        value >= 0.01 -> String.format(Locale.US, "%.4f", value)
        else -> String.format(Locale.US, "%.6f", value)
    }
}

fun formatCompactNumber(value: Double, symbol: String): String {
    return when {
        value >= 1_000_000_000_000.0 -> String.format(Locale.US, "%s%.2fT", symbol, value / 1_000_000_000_000.0)
        value >= 1_000_000_000.0 -> String.format(Locale.US, "%s%.2fB", symbol, value / 1_000_000_000.0)
        value >= 1_000_000.0 -> String.format(Locale.US, "%s%.2fM", symbol, value / 1_000_000.0)
        else -> String.format(Locale.US, "%s%,.0f", symbol, value)
    }
}
