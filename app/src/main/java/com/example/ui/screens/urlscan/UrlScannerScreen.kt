package com.example.ui.screens.urlscan

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.AlertGreen
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
fun UrlScannerScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val urlInput by viewModel.urlInput.collectAsState()
  val isScanning by viewModel.isUrlScanning.collectAsState()
  val lastAnalysis by viewModel.lastUrlAnalysis.collectAsState()
  val recentScans by viewModel.allScans.collectAsState()

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

    // Top Navigation Header
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.testTag("url_scanner_back_button")
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
          text = "Website & URL Risk Scanner",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "SSRF Guard & Phishing Heuristics",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // SSRF & Safety Protection Notice Card
    CyberCard(
      borderColor = NeonEmerald.copy(alpha = 0.4f),
      backgroundColor = Color(0xFF091F26)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = NeonEmerald,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "Zero-Trust SSRF Defense Active",
            color = NeonCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Private RFC1918 subnets, cloud metadata (169.254.169.254), and loopback IPs are strictly isolated and prevented from server/device egress.",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Input Field
    OutlinedTextField(
      value = urlInput,
      onValueChange = { viewModel.setUrlInput(it) },
      placeholder = { Text("Enter link or domain (e.g. secure-login.xyz)", color = TextMuted, fontSize = 13.sp) },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = NeonEmerald)
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
        .testTag("url_scanner_input")
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Scan Button
    Button(
      onClick = { viewModel.scanUrl() },
      enabled = urlInput.isNotBlank() && !isScanning,
      colors = ButtonDefaults.buttonColors(
        containerColor = NeonEmerald,
        contentColor = CyberBackground,
        disabledContainerColor = CyberSurface
      ),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .testTag("url_scanner_submit_button")
    ) {
      if (isScanning) {
        CircularProgressIndicator(
          color = CyberBackground,
          strokeWidth = 2.dp,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("Analyzing DNS & Heuristics...", fontWeight = FontWeight.Bold)
      } else {
        Text("Scan URL for Threats", fontWeight = FontWeight.Bold, fontSize = 14.sp)
      }
    }

    // Results Display
    if (lastAnalysis != null) {
      val res = lastAnalysis!!
      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Analysis Results",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(10.dp))

      CyberCard(
        borderColor = when (res.riskLevel) {
          RiskLevel.SAFE -> AlertGreen
          RiskLevel.SUSPICIOUS -> AlertAmber
          RiskLevel.HIGH_RISK -> AlertRed
          RiskLevel.UNKNOWN -> CyberBorder
        }
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = res.domain,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (res.isHttps) Icons.Default.Lock else Icons.Default.LockOpen,
                  contentDescription = null,
                  tint = if (res.isHttps) AlertGreen else AlertAmber,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (res.isHttps) "HTTPS Encrypted" else "Insecure HTTP",
                  color = if (res.isHttps) AlertGreen else AlertAmber,
                  fontSize = 11.sp
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              RiskBadge(level = res.riskLevel)
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Score: ${res.riskScore}/100",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Detected Threats
          Text(
            text = "Detected Indicators:",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          res.detectedThreats.forEach { threat ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = if (res.riskLevel == RiskLevel.SAFE) AlertGreen else AlertAmber,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = threat,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Recommendations
          Text(
            text = "Safety Recommendation:",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(4.dp))
          res.recommendations.forEach { rec ->
            Text(
              text = "• $rec",
              color = NeonCyan,
              fontSize = 12.sp,
              modifier = Modifier.padding(vertical = 1.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Recent Scans from Room
    Text(
      text = "Recent Scan Logs",
      color = TextPrimary,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    if (recentScans.isEmpty()) {
      CyberCard {
        Text(
          text = "No URLs scanned yet. Enter a website address above to test for phishing and malicious indicators.",
          color = TextMuted,
          fontSize = 12.sp
        )
      }
    } else {
      recentScans.take(6).forEach { scan ->
        CyberCard(
          modifier = Modifier.padding(vertical = 4.dp),
          testTag = "recent_scan_${scan.id}"
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = scan.domain,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = scan.url,
                color = TextMuted,
                fontSize = 11.sp,
                maxLines = 1
              )
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
