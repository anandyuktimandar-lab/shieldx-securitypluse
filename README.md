# ShieldX by SecuriPulse

**ShieldX by SecuriPulse** is a production-grade Android cybersecurity and privacy application developed with Kotlin, Jetpack Compose, Material 3, and Room.

![ShieldX Cybersecurity Dashboard](/public/assets/aistudio/ShieldX_Neon_Cybersecurity_Dashboard_2ad7cc43.png)

## Highlights & Features
- 🛡️ **Cyber Defense Dashboard**: Real-time device security status, active scan alerts, and key security metric indicators.
- 🔍 **Safe URL & Phishing Scanner**: Analyzes web addresses for spoofing, deceptive homoglyphs, SSL/TLS flaws, and SSRF threat vectors (blocking loopbacks, internal RFC1918 IPs, and cloud metadata).
- 📲 **Device Integrity & Permissions Auditor**: Checks root binaries, USB debugging status, developer options, and network connectivity state (VPN, Wi-Fi security).
- ⚡ **Device Health & Telemetry**: Genuine on-device memory (RAM), battery thermal/percentage telemetry, and storage statistics using Android platform APIs.
- 🔐 **Encrypted Privacy Vault**: Authenticated AES-GCM encrypted storage for sensitive credentials and private security notes.
- 📚 **Interactive Learning Center**: Actionable lessons on phishing detection, password management, MFA best practices, and emergency incident response.
- 📊 **Security Reports & History**: Room-backed persistent database logging every scan event and alert.

## Architecture
- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Local Database**: Android Room 2.7 (SQLite)
- **Networking**: OkHttp 4 + Retrofit 2
- **Design System**: Neon Cyber Obsidian theme (`#090D14` background, `#00F5A0` / `#00FFC2` glowing accents)

## Building & Verification
- Compile and verify applet:
  ```bash
  gradle assembleDebug
  ```
- Run automated unit and Robolectric tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```
