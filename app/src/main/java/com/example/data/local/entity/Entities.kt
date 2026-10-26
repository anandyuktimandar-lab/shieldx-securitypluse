package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_reports")
data class ScanReportEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val url: String,
  val domain: String,
  val riskLevel: String,
  val riskScore: Int,
  val threatsJson: String,
  val recommendationsJson: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_alerts")
data class SecurityAlertEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val message: String,
  val severity: String, // INFO, WARNING, CRITICAL
  val category: String, // NETWORK, SYSTEM, URL, PRIVACY
  val isDismissed: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vault_items")
data class VaultItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val category: String, // NOTE, PASSWORD, DOCUMENT
  val encryptedPayload: String, // AES-GCM encrypted
  val usernameOrLabel: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_progress")
data class LearningProgressEntity(
  @PrimaryKey val lessonId: String,
  val isCompleted: Boolean,
  val completedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watchlist_coins")
data class WatchlistCoinEntity(
  @PrimaryKey val coinId: String,
  val symbol: String,
  val name: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "portfolio_holdings")
data class PortfolioHoldingEntity(
  @PrimaryKey val holdingId: String,
  val coinId: String,
  val symbol: String,
  val name: String,
  val amount: Double,
  val buyPriceUsd: Double,
  val timestamp: Long = System.currentTimeMillis()
)
