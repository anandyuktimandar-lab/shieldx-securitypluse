# ShieldX by SecuriPulse - Firestore Security Specification (Phase 0 TDD)

## 1. Data Invariants
1. **User Isolation**: Every document in `/users/{userId}/watchlist/{coinId}`, `/users/{userId}/portfolio/{holdingId}`, and `/users/{userId}/scans/{scanId}` must belong exclusively to `request.auth.uid == userId`, and the body's `userId` field must match `request.auth.uid`.
2. **Schema & Bounds Integrity**:
   - All document IDs (`userId`, `coinId`, `holdingId`, `scanId`) must satisfy `isValidId(id)` (`size <= 128`, alphanumeric + `_-`).
   - All strings have explicit upper size bounds to prevent Denial-of-Wallet storage exhaustion.
   - Numeric fields (`amount`, `buyPriceUsd`, `riskScore`) are validated for valid ranges (`amount > 0`, `buyPriceUsd >= 0`, `riskScore >= 0 && riskScore <= 100`).
3. **Temporal & Immutable Fields**:
   - `createdAt` and `updatedAt` must be server timestamps (`is timestamp && <= request.time`).
   - On update, `userId` and `createdAt` are strictly immutable (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read**: `unauthDb.collection("users/alice/watchlist").get()` -> REJECT
2. **Cross-User Read (IDOR)**: Bob reads `users/alice/portfolio/eth_holding` -> REJECT
3. **Cross-User Write**: Bob writes to `users/alice/watchlist/bitcoin` -> REJECT
4. **UID Spoofing in Body**: Alice writes to `users/alice/watchlist/bitcoin` with `{ userId: "bob_456", ... }` -> REJECT
5. **Shadow / Ghost Field Injection**: Alice writes `{ ..., isAdmin: true }` to `watchlist` -> REJECT (`hasOnly` check)
6. **Missing Required Keys**: Alice omits `symbol` or `createdAt` on create -> REJECT (`hasAll` check)
7. **Oversized String Poisoning**: Alice sends a 10,000-char string in `name` or `url` -> REJECT (`size()` check)
8. **Negative Portfolio Amount**: Alice sets `amount: -5.0` or `buyPriceUsd: -100` -> REJECT
9. **Out-of-Bounds Risk Score**: Alice sets `riskScore: 999` in `scans` -> REJECT
10. **Immutable Field Mutation**: Alice updates `createdAt` or `userId` on an existing portfolio document -> REJECT
11. **Invalid Enum Value**: Alice sets `riskLevel: "HACKED"` (not in `['SAFE', 'SUSPICIOUS', 'HIGH_RISK', 'UNKNOWN']`) -> REJECT
12. **Client Epoch Timestamp Spoofing**: Alice sends `createdAt: 1700000000000` (int instead of `Timestamp`) -> REJECT
