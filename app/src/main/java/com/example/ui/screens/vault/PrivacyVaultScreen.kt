package com.example.ui.screens.vault

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.crypto.CryptoManager
import com.example.ui.components.CyberCard
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
fun PrivacyVaultScreen(
  viewModel: MainViewModel,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  BackHandler { onBack() }

  val vaultItems by viewModel.vaultItems.collectAsState()
  val passwordInput by viewModel.passwordInput.collectAsState()
  val passwordEval by viewModel.passwordEvaluation.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  var newTitle by remember { mutableStateOf("") }
  var newSecret by remember { mutableStateOf("") }
  var newCategory by remember { mutableStateOf("Passwords") }

  var revealedItemId by remember { mutableStateOf<Long?>(null) }
  var isPasswordVisible by remember { mutableStateOf(false) }

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
        modifier = Modifier.testTag("privacy_vault_back_button")
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
          text = "Privacy Vault",
          color = TextPrimary,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Your Data, Your Control (AES-256 GCM)",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Center Hero: Safe Emblem
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(110.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(Color(0xFF0C242A))
          .border(2.dp, NeonEmerald, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Encrypted Vault",
          tint = NeonCyan,
          modifier = Modifier.size(54.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Hardware KeyStore & Zero-Knowledge",
        color = NeonEmerald,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Interactive Password Strength Auditor
    CyberCard {
      Column {
        Text(
          text = "Password Strength & Entropy Auditor",
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Test credential entropy against modern brute-force dictionaries.",
          color = TextMuted,
          fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = passwordInput,
          onValueChange = { viewModel.setPasswordInput(it) },
          placeholder = { Text("Enter a password to test...", color = TextMuted, fontSize = 13.sp) },
          singleLine = true,
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle password visibility",
                tint = TextSecondary
              )
            }
          },
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
            .testTag("vault_password_eval_input")
        )

        if (passwordInput.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Rating: ${passwordEval.strengthText}",
              color = if (passwordEval.score >= 70) AlertGreen else NeonCyan,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            Text(
              text = "${passwordEval.entropyBits} bits of entropy",
              color = TextSecondary,
              fontSize = 11.sp
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          passwordEval.feedback.forEach { tip ->
            Text(
              text = "• $tip",
              color = TextMuted,
              fontSize = 11.sp
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Categories matching mockup
    VaultCategoryRow(
      title = "Photos & Videos",
      subtitle = "Encrypted biometric storage",
      icon = Icons.Default.PhotoLibrary,
      onClick = { newCategory = "Photos"; showAddDialog = true }
    )
    Spacer(modifier = Modifier.height(8.dp))

    VaultCategoryRow(
      title = "Documents",
      subtitle = "Secure document metadata",
      icon = Icons.Default.Description,
      onClick = { newCategory = "Documents"; showAddDialog = true }
    )
    Spacer(modifier = Modifier.height(8.dp))

    VaultCategoryRow(
      title = "Passwords",
      subtitle = "Manage your credentials",
      icon = Icons.Default.Key,
      onClick = { newCategory = "Passwords"; showAddDialog = true }
    )
    Spacer(modifier = Modifier.height(8.dp))

    VaultCategoryRow(
      title = "Notes",
      subtitle = "Encrypted private memos",
      icon = Icons.Default.Note,
      onClick = { newCategory = "Notes"; showAddDialog = true }
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Stored Vault Items Section
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Encrypted Vault Entries (${vaultItems.size})",
        color = TextPrimary,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
      )
      IconButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.testTag("vault_add_item_button")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item", tint = NeonEmerald)
      }
    }

    if (vaultItems.isEmpty()) {
      CyberCard {
        Text(
          text = "Your vault is empty. Click '+' to store an encrypted credential or secure note.",
          color = TextMuted,
          fontSize = 12.sp
        )
      }
    } else {
      vaultItems.forEach { item ->
        val isRevealed = revealedItemId == item.id
        CyberCard(
          modifier = Modifier.padding(vertical = 4.dp),
          testTag = "vault_item_${item.id}"
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isRevealed) CryptoManager.decrypt(item.encryptedPayload) else "•••••••••••••••• (AES-256-GCM)",
                color = if (isRevealed) NeonCyan else TextMuted,
                fontSize = 12.sp
              )
            }
            Row {
              IconButton(onClick = {
                revealedItemId = if (isRevealed) null else item.id
              }) {
                Icon(
                  imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = "Reveal",
                  tint = TextSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              IconButton(onClick = { viewModel.deleteVaultItem(item) }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = AlertRed,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Add Item Dialog
  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Store in Encrypted Vault", color = TextPrimary) },
      text = {
        Column {
          OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Title or Account") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newSecret,
            onValueChange = { newSecret = it },
            label = { Text("Secret / Passphrase / Note") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newTitle.isNotBlank() && newSecret.isNotBlank()) {
              viewModel.saveVaultItem(newTitle, newCategory, newSecret, "UserVault")
              newTitle = ""
              newSecret = ""
              showAddDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald, contentColor = CyberBackground)
        ) {
          Text("Encrypt & Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = CyberSurface
    )
  }
}

@Composable
fun VaultCategoryRow(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(14.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(CyberSurface)
      .border(1.dp, CyberBorder, shape)
      .clickable { onClick() }
      .padding(14.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFF0F2538)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = NeonEmerald,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          color = TextSecondary,
          fontSize = 12.sp
        )
      }
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
