# ShieldX by SecuriPulse — Testing & Verification Guide

## 1. Automated Verification Summary

### Tier 1: JavaScript Firestore Security Rules Unit Tests (`firestore.test.js`)
Validates `/users/{userId}/watchlist/{coinId}`, `/users/{userId}/portfolio/{holdingId}`, and `/users/{userId}/scans/{scanId}` against the local Firestore Emulator (`127.0.0.1:8085`):
- Unauthenticated reads/writes are rejected (`PERMISSION_DENIED`).
- Cross-user reads/updates/deletes are rejected (`PERMISSION_DENIED`).
- Authenticated owner CRUD with valid schemas and `@ServerTimestamp` succeeds.
- Invalid schema mutations (e.g., mutating `userId`, negative portfolio `amount`, invalid `riskLevel` enum) are rejected.

### Tier 2: Kotlin / Robolectric Firestore Emulator Tests (`CloudSyncRepositoryRuleTest.kt`)
Validates real Android Kotlin data classes (`WatchlistCoinDoc`, `PortfolioHoldingDoc`, `CloudScanReportDoc`) with `@IgnoreExtraProperties`, `@DocumentId`, and `@ServerTimestamp` against the live Firestore (`8085`) and Auth (`9099`) emulators:
- `watchlistCoin_authenticatedOwnerCrudSucceeds_andCrossUserFails`: **PASSED**
- `portfolioHolding_ownerCreateAndUpdateSucceeds_negativeAmountFails`: **PASSED**
- `cloudScanReport_ownerCreateAndListQuerySucceeds`: **PASSED**

### Tier 3: Domain Security & Cryptography Unit Tests (`ExampleUnitTest.kt`)
Validates `SsrfGuard`, `UrlThreatAnalyzer`, and `CryptoManager` (AES-256-GCM encryption/decryption & Shannon password entropy):
- `ssrfGuard_blocksPrivateAndLoopbackAddresses`: **PASSED**
- `ssrfGuard_allowsPublicHttpsUrl`: **PASSED**
- `urlThreatAnalyzer_detectsHighRiskPhishingUrl`: **PASSED**
- `urlThreatAnalyzer_marksCleanHttpsDomainSafe`: **PASSED**
- `cryptoManager_encryptsAndDecryptsRoundTrip`: **PASSED**

## 2. Running Tests Locally
- **Unit & Domain Tests**:
  ```bash
  gradle :app:testDebugUnitTest --tests 'com.example.ExampleUnitTest'
  ```
- **Firestore Emulator Rule Tests**:
  ```bash
  FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
  ```
