# ShieldX by SecuriPulse — Implementation Plan

## Phase 1: Project Inspection & Baseline Preservation
1. Inspect existing Kotlin/Compose modules (`SsrfGuard`, `UrlThreatAnalyzer`, `DeviceIntegrityAuditor`, `CryptoManager`, `ShieldXDatabase`, and UI screens).
2. Preserve all working local cybersecurity features while upgrading the platform with Cloud Firestore persistence, Google Sign-In, Live Crypto Market Intelligence, Interactive Charts, and Gemini AI.

## Phase 2: Firebase Provisioning & Zero-Trust Security Rules
1. Extract SHA-1 and SHA-256 certificates from `debug.keystore` and provision Firebase (`firestore`, `ai_logic`) via `ProvisionFirebase`.
2. Design `security_spec.md` and `firebase-blueprint.json` for user-scoped subcollections:
   - `/users/{userId}/watchlist/{coinId}`
   - `/users/{userId}/portfolio/{holdingId}`
   - `/users/{userId}/scans/{scanId}`
3. Author `firestore.rules` enforcing owner-only access (`request.auth.uid == userId`), strict schema validation, and immutable field checks on `update`.
4. Verify rules against the local Firebase Emulator using both Tier 1 (`firestore.test.js`) and Tier 2 (`CloudSyncRepositoryRuleTest.kt`), then deploy via `DeployRules`.

## Phase 3: Google Sign-In via Jetpack Credential Manager
1. Implement `AuthScreen.kt` with `GetGoogleIdOption` (silent auto-sign-in) and `GetSignInWithGoogleOption` (interactive sign-in button).
2. Gate `MainActivity` so unauthenticated users authenticate before accessing Cloud Firestore streams, and tear down snapshot listeners on sign-out via `flatMapLatest`.

## Phase 4: Live Crypto Markets, Interactive Charts & Portfolio
1. Build `CryptoMarketService.kt` with CoinGecko and CoinCap REST adapters, USD/INR rate conversion, and OHLC candlestick series retrieval.
2. Build `InteractiveCryptoChartCard` in `CryptoChartsScreen.kt` supporting Candlestick, Line, Area, Volume bars, timeframe buttons (`1m`, `5m`, `15m`, `1h`, `4h`, `1d`, `1w`), and pinch zoom.
3. Build `CryptoMarketsScreen.kt` integrating Live Markets, Cloud Watchlist, and Cloud Portfolio P&L tracking.

## Phase 5: Gemini AI Analyst (Search Grounding + Voice Mode)
1. Build `GeminiService.kt` supporting `gemini-2.5-flash` with Google Search Grounding (`"tools": [{"googleSearch": {}}]`), `gemini-3.1-pro-preview`, `gemini-3.1-flash-lite-preview`, and Firebase AI Logic fallback.
2. Build `AiAssistantScreen.kt` with multi-turn chat, grounding source links, and live Voice Conversation Mode (`SpeechRecognizer` + `TextToSpeech`).
