# ShieldX by SecuriPulse - Architecture & Design Specification

## 1. System Philosophy & Objectives
ShieldX is a privacy-first cybersecurity utility designed for Android. It adheres to zero-trust principles:
- **100% On-Device Processing where possible**: User data, security notes, and credentials never leave the device unencrypted.
- **Defensive Networking & Strict SSRF Defenses**: Any external link verification operates through strict IP sanitization, avoiding arbitrary server or client loopback SSRF.
- **Honest Security Metrics**: No fabricated "100% protected" claims. All assessments clearly cite methodology, verified hardware parameters, and limitations.

---

## 2. Technology Stack

| Layer | Technology | Rationale |
|---|---|---|
| **OS & Language** | Android 14+ (API 24 - 36), Kotlin 2.2 | Modern null-safe language with coroutines |
| **UI Framework** | Jetpack Compose + Material Design 3 | Modern declarative UI, edge-to-edge support, dark obsidian theme |
| **Architecture** | MVVM + Clean Architecture | Clear separation between UI, domain business logic, and repository layer |
| **Local Persistence**| Android Jetpack Room (SQLite) | Fast, typed, offline-first storage for scans, audit logs, and settings |
| **Networking** | OkHttp 4 + Retrofit 2 + Moshi | Robust HTTP client with custom interceptors and timeout protections |
| **Cryptographic Utils**| Java Cryptography Architecture (AES-256 GCM, PBKDF2) | Industry-standard cryptographic primitives for the Vault module |
| **System Telemetry**| Android BatteryManager, ActivityManager, ConnectivityManager | Genuine device telemetry instead of mocked statistics |

---

## 3. Directory Structure

```
app/src/main/java/com/example/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── ScanReportDao.kt
│   │   │   ├── SecurityAlertDao.kt
│   │   │   └── VaultItemDao.kt
│   │   ├── entity/
│   │   │   ├── ScanReportEntity.kt
│   │   │   ├── SecurityAlertEntity.kt
│   │   │   └── VaultItemEntity.kt
│   │   └── ShieldXDatabase.kt
│   ├── model/
│   │   ├── DeviceHealthInfo.kt
│   │   ├── SecurityCheckItem.kt
│   │   └── UrlAnalysisResult.kt
│   └── repository/
│       ├── DeviceSecurityRepository.kt
│       ├── SecurityRepository.kt
│       └── UrlScannerRepository.kt
├── domain/
│   ├── security/
│   │   ├── DeviceIntegrityAuditor.kt
│   │   ├── PasswordStrengthEvaluator.kt
│   │   ├── SsrfGuard.kt
│   │   └── UrlThreatAnalyzer.kt
│   └── crypto/
│       └── CryptoManager.kt
├── ui/
│   ├── components/
│   │   ├── CyberBadge.kt
│   │   ├── CyberCard.kt
│   │   ├── GlowingShield.kt
│   │   ├── MetricTile.kt
│   │   └── ShieldXTopAppBar.kt
│   ├── navigation/
│   │   ├── NavigationItem.kt
│   │   └── ShieldXBottomBar.kt
│   ├── screens/
│   │   ├── activity/ActivityScreen.kt
│   │   ├── dashboard/DashboardScreen.kt
│   │   ├── features/FeaturesScreen.kt
│   │   ├── health/DeviceHealthScreen.kt
│   │   ├── integrity/DeviceSecurityScreen.kt
│   │   ├── learn/LearningCenterScreen.kt
│   │   ├── profile/ProfileSettingsScreen.kt
│   │   ├── urlscan/UrlScannerScreen.kt
│   │   └── vault/PrivacyVaultScreen.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel/
│       ├── MainViewModel.kt
│       └── ViewModelFactory.kt
└── MainActivity.kt
```

---

## 4. Key Security Controls & Design Rules
1. **SSRF Guard**: Before any network connection is attempted on user-supplied URLs:
   - Must reject loopback (`127.0.0.0/8`, `::1`), private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), link-local (`169.254.0.0/16`), multicast, and cloud metadata (`169.254.169.254`).
   - Must enforce HTTP/HTTPS protocol restrictions.
2. **Encrypted Vault**:
   - AES-256 GCM authenticated encryption.
   - Nonce / IV uniqueness per entry.
3. **No Unnecessary Permissions**:
   - Least privilege model. Only install-time permissions declared. No invasive location, camera, or contact queries.
