package com.example.ui.screens.crypto

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.CyberCard
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
fun CryptoSecurityScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val addressInput by viewModel.cryptoAddressInput.collectAsState()
  val checkResult by viewModel.cryptoCheckResult.collectAsState()

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
        modifier = Modifier.testTag("crypto_back_button")
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
          text = "Crypto & Asset Shield",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Contract Verification & Phishing Drainer Defense",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Total Balance Card matching mockup
    CyberCard(
      borderColor = NeonEmerald.copy(alpha = 0.5f),
      backgroundColor = Color(0xFF0C1F2B)
    ) {
      Column {
        Text(
          text = "Total Monitored Portfolio",
          color = TextSecondary,
          fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            text = "$ 12,428.32",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "+2.4% (24h)",
            color = NeonEmerald,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Send, Receive, Swap, Audit
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          CryptoActionButton(icon = Icons.Default.ArrowUpward, label = "Send")
          CryptoActionButton(icon = Icons.Default.ArrowDownward, label = "Receive")
          CryptoActionButton(icon = Icons.Default.SwapHoriz, label = "Swap")
          CryptoActionButton(icon = Icons.Default.Security, label = "Audit", isAccent = true)
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Address & Contract Risk Scanner
    CyberCard {
      Column {
        Text(
          text = "Recipient Address / Contract Auditor",
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Verify address checksum format and scan for known wallet drainers.",
          color = TextMuted,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = addressInput,
          onValueChange = { viewModel.setCryptoAddressInput(it) },
          placeholder = { Text("Paste 0x... or bc1... or Solana address", color = TextMuted, fontSize = 12.sp) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
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
            .testTag("crypto_address_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
          onClick = { viewModel.verifyCryptoAddress() },
          enabled = addressInput.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonEmerald,
            contentColor = CyberBackground
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("crypto_verify_button")
        ) {
          Text("Verify Address Safety", fontWeight = FontWeight.Bold)
        }

        if (checkResult != null) {
          val res = checkResult!!
          Spacer(modifier = Modifier.height(14.dp))

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (res.isKnownPhishingDrainer) AlertRed.copy(alpha = 0.15f)
                else if (res.isValidFormat) AlertGreen.copy(alpha = 0.15f)
                else AlertAmber.copy(alpha = 0.15f)
              )
              .border(
                1.dp,
                if (res.isKnownPhishingDrainer) AlertRed
                else if (res.isValidFormat) AlertGreen
                else AlertAmber,
                RoundedCornerShape(12.dp)
              )
              .padding(12.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (res.isKnownPhishingDrainer) Icons.Default.Warning else Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = if (res.isKnownPhishingDrainer) AlertRed else if (res.isValidFormat) AlertGreen else AlertAmber,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = res.network,
                  color = TextPrimary,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = res.safetyAdvice,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Monitored Assets list matching mockup
    Text(
      text = "Monitored Cryptographic Assets",
      color = TextPrimary,
      fontSize = 15.sp,
      fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(10.dp))

    AssetCard(name = "Bitcoin", symbol = "0.2456 BTC", value = "$7,428.21", change = "+2.1%")
    Spacer(modifier = Modifier.height(8.dp))
    AssetCard(name = "Ethereum", symbol = "1.2445 ETH", value = "$4,215.67", change = "+3.2%")
    Spacer(modifier = Modifier.height(8.dp))
    AssetCard(name = "Solana", symbol = "12.456 SOL", value = "$784.44", change = "+1.8%")
  }
}

@Composable
fun CryptoActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isAccent: Boolean = false
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(46.dp)
        .clip(CircleShape)
        .background(if (isAccent) NeonEmerald else Color(0xFF142B3B))
        .border(1.dp, if (isAccent) NeonCyan else CyberBorder, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isAccent) CyberBackground else NeonCyan,
        modifier = Modifier.size(22.dp)
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = label,
      color = TextSecondary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun AssetCard(
  name: String,
  symbol: String,
  value: String,
  change: String
) {
  CyberCard {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFF0F2C3D)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CurrencyBitcoin,
            contentDescription = name,
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(text = name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(text = symbol, color = TextMuted, fontSize = 11.sp)
        }
      }
      Column(horizontalAlignment = Alignment.End) {
        Text(text = value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = change, color = NeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}
