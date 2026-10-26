package com.example.ui.screens.health

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.theme.AlertGreen
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
fun DeviceHealthScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val health by viewModel.deviceHealth.collectAsState()
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
        modifier = Modifier.testTag("device_health_back_button")
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
          text = "Device Health",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "System Performance & Hardware Telemetry",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Central Gauge: Overall Health % matching the mockup
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(160.dp)) {
          // Track
          drawCircle(
            color = CyberBorder,
            radius = size.width * 0.42f,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
          )
          // Sweep
          val sweep = (health.healthPercentage / 100f) * 360f
          drawArc(
            color = NeonEmerald,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "${health.healthPercentage}%",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
          )
          Text(
            text = "Overall Health",
            color = NeonEmerald,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Hardware Telemetry Cards matching mockup
    TelemetryRow(
      title = "RAM",
      value = "${health.ramUsedMb} MB / ${health.ramTotalMb} MB",
      icon = Icons.Default.Memory,
      accent = Color(0xFF00B4D8),
      testTag = "telemetry_ram"
    )

    Spacer(modifier = Modifier.height(10.dp))

    val usedStorage = String.format("%.1f", health.storageTotalGb - health.storageFreeGb)
    TelemetryRow(
      title = "Storage",
      value = "$usedStorage GB / ${health.storageTotalGb} GB",
      icon = Icons.Default.SdCard,
      accent = NeonEmerald,
      testTag = "telemetry_storage"
    )

    Spacer(modifier = Modifier.height(10.dp))

    TelemetryRow(
      title = "Battery",
      value = "${health.batteryPercentage}% • ${if (health.isCharging) "Charging" else "Discharging"} • ${health.batteryTempCelsius}°C",
      icon = Icons.Default.BatteryChargingFull,
      accent = Color(0xFF10B981),
      testTag = "telemetry_battery"
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Real-time Canvas Sparkline Activity Graph
    CyberCard {
      Column {
        Text(
          text = "System Telemetry Pulse",
          color = TextPrimary,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
        ) {
          val w = size.width
          val h = size.height
          val path = Path().apply {
            moveTo(0f, h * 0.7f)
            lineTo(w * 0.15f, h * 0.65f)
            lineTo(w * 0.30f, h * 0.40f)
            lineTo(w * 0.45f, h * 0.55f)
            lineTo(w * 0.60f, h * 0.30f)
            lineTo(w * 0.75f, h * 0.45f)
            lineTo(w * 0.90f, h * 0.20f)
            lineTo(w, h * 0.35f)
          }
          drawPath(
            path = path,
            brush = Brush.horizontalGradient(listOf(NeonTeal, NeonEmerald, NeonCyan)),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = { viewModel.refreshTelemetry() },
      colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF0F2C2C),
        contentColor = NeonCyan
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .border(1.dp, NeonEmerald, RoundedCornerShape(12.dp))
        .testTag("device_health_refresh_button")
    ) {
      Text("Refresh Hardware Telemetry", fontWeight = FontWeight.Bold)
    }
  }
}

private val NeonTeal = Color(0xFF00D8A5)

@Composable
fun TelemetryRow(
  title: String,
  value: String,
  icon: ImageVector,
  accent: Color,
  modifier: Modifier = Modifier,
  testTag: String = "telemetry_row"
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .padding(14.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accent,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextSecondary,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = value,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
