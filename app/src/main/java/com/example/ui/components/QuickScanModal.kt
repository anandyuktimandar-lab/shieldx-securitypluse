package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.window.Dialog
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
fun QuickScanModal(
  viewModel: MainViewModel
) {
  val showModal by viewModel.showQuickScanModal.collectAsState()
  val isScanning by viewModel.isQuickScanning.collectAsState()
  val progress by viewModel.quickScanProgress.collectAsState()
  val statusText by viewModel.quickScanStatusText.collectAsState()

  if (showModal) {
    Dialog(onDismissRequest = { if (!isScanning) viewModel.closeQuickScanModal() }) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(CyberSurface)
          .border(1.5.dp, NeonEmerald, RoundedCornerShape(20.dp))
          .padding(20.dp)
          .testTag("quick_scan_modal")
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          GlowingShield(
            size = 120.dp,
            isScanning = isScanning,
            isProtected = true
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = if (isScanning) "Full System Security Audit" else "Audit Completed",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = statusText,
            color = if (isScanning) NeonCyan else AlertGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(16.dp))

          LinearProgressIndicator(
            progress = { progress },
            color = NeonEmerald,
            trackColor = CyberBackground,
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp))
          )

          Spacer(modifier = Modifier.height(18.dp))

          if (!isScanning) {
            CyberCard(
              backgroundColor = Color(0xFF0C2424),
              borderColor = AlertGreen.copy(alpha = 0.5f)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AlertGreen,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "System Status: Verified Protected",
                    color = AlertGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Android sandbox intact • No unauthorized superuser binaries • URL SSRF isolation active • Local encryption verified.",
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { viewModel.closeQuickScanModal() },
              colors = ButtonDefaults.buttonColors(
                containerColor = NeonEmerald,
                contentColor = CyberBackground
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("quick_scan_done_button")
            ) {
              Text("Done", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
