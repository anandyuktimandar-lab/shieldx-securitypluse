package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LearningProgressEntity
import com.example.data.local.entity.PortfolioHoldingEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.SecurityAlertEntity
import com.example.data.local.entity.VaultItemEntity
import com.example.data.local.entity.WatchlistCoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanReportDao {
  @Query("SELECT * FROM scan_reports ORDER BY timestamp DESC")
  fun getAllScans(): Flow<List<ScanReportEntity>>

  @Query("SELECT COUNT(*) FROM scan_reports")
  fun getScanCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertScan(scan: ScanReportEntity): Long

  @Query("DELETE FROM scan_reports")
  suspend fun clearAll()
}

@Dao
interface SecurityAlertDao {
  @Query("SELECT * FROM security_alerts WHERE isDismissed = 0 ORDER BY timestamp DESC")
  fun getActiveAlerts(): Flow<List<SecurityAlertEntity>>

  @Query("SELECT * FROM security_alerts ORDER BY timestamp DESC")
  fun getAllAlerts(): Flow<List<SecurityAlertEntity>>

  @Query("SELECT COUNT(*) FROM security_alerts")
  suspend fun getAlertCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: SecurityAlertEntity): Long

  @Query("UPDATE security_alerts SET isDismissed = 1 WHERE id = :id")
  suspend fun dismissAlert(id: Long)

  @Query("UPDATE security_alerts SET isDismissed = 1")
  suspend fun dismissAllAlerts()

  @Query("DELETE FROM security_alerts")
  suspend fun clearAll()
}

@Dao
interface VaultItemDao {
  @Query("SELECT * FROM vault_items ORDER BY timestamp DESC")
  fun getAllVaultItems(): Flow<List<VaultItemEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: VaultItemEntity): Long

  @Delete
  suspend fun deleteItem(item: VaultItemEntity)

  @Query("DELETE FROM vault_items")
  suspend fun clearAll()
}

@Dao
interface LearningProgressDao {
  @Query("SELECT * FROM learning_progress")
  fun getAllProgress(): Flow<List<LearningProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun setProgress(progress: LearningProgressEntity)
}

@Dao
interface WatchlistDao {
  @Query("SELECT * FROM watchlist_coins ORDER BY timestamp DESC")
  fun getAllWatchlistCoins(): Flow<List<WatchlistCoinEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCoin(coin: WatchlistCoinEntity)

  @Query("DELETE FROM watchlist_coins WHERE coinId = :coinId")
  suspend fun removeCoin(coinId: String)
}

@Dao
interface PortfolioDao {
  @Query("SELECT * FROM portfolio_holdings ORDER BY timestamp DESC")
  fun getAllHoldings(): Flow<List<PortfolioHoldingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertHolding(holding: PortfolioHoldingEntity)

  @Query("DELETE FROM portfolio_holdings WHERE holdingId = :holdingId")
  suspend fun deleteHolding(holdingId: String)
}
