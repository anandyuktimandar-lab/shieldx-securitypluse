package com.example.data.remote

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class WatchlistCoinDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val coinId: String = "",
    val symbol: String = "",
    val name: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PortfolioHoldingDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val coinId: String = "",
    val symbol: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val buyPriceUsd: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudScanReportDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val url: String = "",
    val domain: String = "",
    val riskLevel: String = "SAFE",
    val riskScore: Int = 0,
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class CloudSyncRepository(
    private val firestore: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    // --- Watchlist Operations ---
    fun observeWatchlist(userId: String): Flow<List<WatchlistCoinDoc>> = callbackFlow {
        val path = "users/$userId/watchlist"
        val registration = firestore.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(WatchlistCoinDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addWatchlistCoin(
        userId: String,
        coinId: String,
        symbol: String,
        name: String
    ) {
        val path = "users/$userId/watchlist/$coinId"
        val doc = WatchlistCoinDoc(
            id = coinId,
            userId = userId,
            coinId = coinId,
            symbol = symbol,
            name = name
        )
        try {
            firestore.document(path).set(doc).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun removeWatchlistCoin(userId: String, coinId: String) {
        val path = "users/$userId/watchlist/$coinId"
        try {
            firestore.document(path).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    suspend fun getWatchlistCoin(userId: String, coinId: String): WatchlistCoinDoc? {
        val path = "users/$userId/watchlist/$coinId"
        return try {
            val snap = firestore.document(path).get().await()
            snap.toObject(WatchlistCoinDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    // --- Portfolio Operations ---
    fun observePortfolio(userId: String): Flow<List<PortfolioHoldingDoc>> = callbackFlow {
        val path = "users/$userId/portfolio"
        val registration = firestore.collection(path)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PortfolioHoldingDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun addPortfolioHolding(
        userId: String,
        holdingId: String,
        coinId: String,
        symbol: String,
        name: String,
        amount: Double,
        buyPriceUsd: Double
    ) {
        val path = "users/$userId/portfolio/$holdingId"
        val doc = PortfolioHoldingDoc(
            id = holdingId,
            userId = userId,
            coinId = coinId,
            symbol = symbol,
            name = name,
            amount = amount,
            buyPriceUsd = buyPriceUsd
        )
        try {
            firestore.document(path).set(doc).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun updatePortfolioHolding(
        userId: String,
        holdingId: String,
        amount: Double,
        buyPriceUsd: Double
    ) {
        val path = "users/$userId/portfolio/$holdingId"
        val updates = mapOf(
            "amount" to amount,
            "buyPriceUsd" to buyPriceUsd,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            firestore.document(path).update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
        }
    }

    suspend fun deletePortfolioHolding(userId: String, holdingId: String) {
        val path = "users/$userId/portfolio/$holdingId"
        try {
            firestore.document(path).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }

    // --- Cloud Scan Reports ---
    fun observeCloudScans(userId: String): Flow<List<CloudScanReportDoc>> = callbackFlow {
        val path = "users/$userId/scans"
        val registration = firestore.collection(path)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val docs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudScanReportDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } ?: emptyList()
                trySend(docs)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveCloudScanReport(
        userId: String,
        scanId: String,
        url: String,
        domain: String,
        riskLevel: String,
        riskScore: Int,
        summary: String
    ) {
        val path = "users/$userId/scans/$scanId"
        val doc = CloudScanReportDoc(
            id = scanId,
            userId = userId,
            url = url,
            domain = domain,
            riskLevel = riskLevel,
            riskScore = riskScore,
            summary = summary
        )
        try {
            firestore.document(path).set(doc).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun deleteCloudScanReport(userId: String, scanId: String) {
        val path = "users/$userId/scans/$scanId"
        try {
            firestore.document(path).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
        }
    }
}
