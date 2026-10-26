# ShieldX by SecuriPulse - Security Policy & Threat Model

## 1. Core Security Commitments
- **Zero Ingestion of User Secrets**: Passwords and vault notes stored in ShieldX are locally protected using authenticated AES-GCM encryption. Keys are never transmitted over the internet.
- **Defensive URL Scanning**: ShieldX inspects domain structure, certificate configuration, and known threat patterns without performing unauthenticated recursive fetches into internal networks.
- **Transparent Risk Scoring**: We clearly delineate between verified OS configurations (e.g. Developer Options state, Root detection) and heuristic estimations.

---

## 2. SSRF (Server-Side Request Forgery / Client Forgery) Defense Specification
When analyzing URLs provided by users:
1. **Scheme Validation**: Only `http` and `https` protocols are permitted. Schemes like `file://`, `content://`, `gopher://`, `ftp://`, or `javascript:` are rejected immediately.
2. **IP Sanitization**:
   - Loopback: `127.0.0.0/8`, `::1`
   - Private subnets (RFC 1918): `10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`
   - Carrier-Grade NAT: `100.64.0.0/10`
   - Link-Local: `169.254.0.0/16`, `fe80::/10`
   - Cloud Metadata Service: `169.254.169.254`
   - Multicast & Broadcast: `224.0.0.0/4`, `255.255.255.255`
3. **Safe Redirection Limits**: Maximum 3 redirects allowed, each destination evaluated against the SSRF filter before following.
4. **Content-Length & Execution Restrictions**: ShieldX never executes JavaScript, downloads executable binaries, or renders untrusted HTML.

---

## 3. Cryptography Standards
- **Encryption Algorithm**: AES-256 in Galois/Counter Mode (GCM) providing both confidentiality and data integrity authentication.
- **Key Derivation**: PBKDF2 with HMAC-SHA256 and a minimum of 10,000 iterations with a unique 128-bit cryptographic salt.
- **Randomness**: `java.security.SecureRandom` for all nonce/IV and salt generation.

---

## 4. Privacy & Android Permissions
- ShieldX requests only non-intrusive permissions:
  - `INTERNET`: For checking external URL threat indicators and reputation feeds.
  - `ACCESS_NETWORK_STATE` & `ACCESS_WIFI_STATE`: To detect Wi-Fi encryption status (WPA3 vs open) and active VPN protection.
- ShieldX **does NOT request**: Contacts, Call logs, SMS read, Camera, Mic, or Fine Location.
