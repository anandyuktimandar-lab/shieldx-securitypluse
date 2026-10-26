package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.LearningProgressDao
import com.example.data.local.dao.PortfolioDao
import com.example.data.local.dao.ScanReportDao
import com.example.data.local.dao.SecurityAlertDao
import com.example.data.local.dao.VaultItemDao
import com.example.data.local.dao.WatchlistDao
import com.example.data.local.entity.LearningProgressEntity
import com.example.data.local.entity.PortfolioHoldingEntity
import com.example.data.local.entity.ScanReportEntity
import com.example.data.local.entity.SecurityAlertEntity
import com.example.data.local.entity.VaultItemEntity
import com.example.data.local.entity.WatchlistCoinEntity

@Database(
  entities = [
    ScanReportEntity::class,
    SecurityAlertEntity::class,
    VaultItemEntity::class,
    LearningProgressEntity::class,
    WatchlistCoinEntity::class,
    PortfolioHoldingEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class ShieldXDatabase : RoomDatabase() {
  abstract fun scanReportDao(): ScanReportDao
  abstract fun securityAlertDao(): SecurityAlertDao
  abstract fun vaultItemDao(): VaultItemDao
  abstract fun learningProgressDao(): LearningProgressDao
  abstract fun watchlistDao(): WatchlistDao
  abstract fun portfolioDao(): PortfolioDao

  companion object {
    @Volatile
    private var INSTANCE: ShieldXDatabase? = null

    fun getDatabase(context: Context): ShieldXDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ShieldXDatabase::class.java,
          "shieldx_cyber_db"
        ).fallbackToDestructiveMigration(true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
