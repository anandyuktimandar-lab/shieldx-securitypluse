# ShieldX by SecuriPulse — Authentication Error Diagnosis (`AUTH_ERROR_DIAGNOSIS.md`)

## 1. Exact Reported Error
```text
An internal error has occurred. Request to this API Identity Toolkit method google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdP is blocked.
```

## 2. Application & Authentication Architecture Audited
- **Platform & UI Framework**: Android native application built with **Kotlin** and **Jetpack Compose (Material 3)** (`compileSdk = 36`, `minSdk = 24`, `applicationId = "com.aistudio.shieldx.secpulse"`).
- **Firebase SDK Version**: Managed via `libs.firebase.bom` (`34.7.0`) in `gradle/libs.versions.toml` with `firebase-auth`, `firebase-firestore`, `androidx.credentials:credentials:1.5.0`, `androidx.credentials:credentials-play-services-auth:1.5.0`, and `com.google.android.libraries.identity.googleid:googleid:1.1.1`.
- **Firebase Configuration Files**:
  - `app/google-services.json`:
    - `project_id`: `studio-4054916737-dd4e4`
    - `project_number`: `345432115619`
    - `package_name`: `com.aistudio.shieldx.secpulse`
    - `certificate_hash` (SHA-1): `0cc4f2a7011bf5d772bf3f414f8e2fd9caff236a` (Matches `debug.keystore` SHA-1 `0C:C4:F2:A7:01:1B:F5:D7:72:BF:3F:41:4F:8E:2F:D9:CA:FF:23:6A`)
    - Android OAuth 2.0 Client ID (`client_type: 1`): `345432115619-i9jmmmrnfqf56b4urj7m8sfmmlcv6vhj.apps.googleusercontent.com`
    - Web OAuth 2.0 Server Client ID (`client_type: 3`, used as `default_web_client_id`): `345432115619-b05ff1f7rj5reaa7qssm586bajas4n20.apps.googleusercontent.com`
    - Android API Key: `AIzaSyCJ...Bfkk` (masked)
  - `app/src/main/res/values/firebase_applet_config.xml`:
    - `firestore_database_id`: `ai-studio-android-shieldxb-869da137-cf7d-4d5b-a6c0-36aa54bd9d82`
- **Relevant Code Files & Functions**:
  - `app/src/main/java/com/example/ui/screens/auth/AuthScreen.kt`:
    - `attemptAutoSignIn(...)`: Silent sign-in using `GetGoogleIdOption` via `CredentialManager`.
    - `AuthScreen(...)`: Interactive Google Sign-In using `GetSignInWithGoogleOption` via `CredentialManager`, converting the returned `GoogleIdTokenCredential` via `GoogleAuthProvider.getCredential(idToken, null)` and calling `Firebase.auth.signInWithCredential(firebaseCredential).await()`.
    - `signOutWithCredentialManager(...)`: Calls `Firebase.auth.signOut()` and `credentialManager.clearCredentialState(ClearCredentialStateRequest())`.
  - `app/src/main/java/com/example/MainActivity.kt`:
    - `ShieldXApp`: Observes `viewModel.currentUser` and gates protected screens behind `AuthScreen` when `currentUser == null`.
  - `app/src/main/java/com/example/ui/viewmodel/MainViewModel.kt`:
    - Registers `FirebaseAuth.AuthStateListener` and uses `flatMapLatest` on `_currentUser` to attach/detach Cloud Firestore listeners cleanly.

---

## 3. Empirical Evidence Collected

To isolate whether the failure occurred in the Android client code, the OAuth SHA-1 certificate registration, or Google Cloud's Identity Toolkit API gateway, we executed a direct diagnostic probe against the Identity Toolkit `signInWithIdp` REST endpoint using the project's configured Android package headers:

```json
{
  "error": {
    "code": 403,
    "message": "Requests to this API identitytoolkit method google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdp are blocked.",
    "status": "PERMISSION_DENIED",
    "details": [
      {
        "@type": "type.googleapis.com/google.rpc.ErrorInfo",
        "reason": "API_KEY_SERVICE_BLOCKED",
        "domain": "googleapis.com",
        "metadata": {
          "consumer": "projects/345432115619",
          "methodName": "google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdp",
          "service": "identitytoolkit.googleapis.com",
          "apiName": "identitytoolkit"
        }
      }
    ]
  }
}
```

---

## 4. Confirmed Root Cause vs. Ruled-Out Causes

### Confirmed Root Cause (`API_KEY_SERVICE_BLOCKED` on `identitytoolkit.googleapis.com`)
1. **On-Device Google Account Selection & OAuth Token Issuance Succeeds**: Jetpack `CredentialManager` (`GetSignInWithGoogleOption`) successfully validates the APK's `package_name` (`com.aistudio.shieldx.secpulse`) and `SHA-1` certificate (`0CC4F2A7011BF5D772BF3F414F8E2FD9CAFF236A`) and returns a Google ID token (`GoogleIdTokenCredential`).
2. **Firebase Token Exchange (`SignInWithIdP`) is Blocked by Google Cloud API Key / Provider Restrictions**: When `Firebase.auth.signInWithCredential(...)` sends the Google ID token to `https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp`, Google Cloud returns HTTP `403 PERMISSION_DENIED` with reason `API_KEY_SERVICE_BLOCKED` on project `studio-4054916737-dd4e4` (`projects/345432115619`).
3. **Why `API_KEY_SERVICE_BLOCKED` Happens**:
   - The Android API Key (`AIzaSyCJ...Bfkk`) in Google Cloud Console (`APIs & Services -> Credentials`) for project `studio-4054916737-dd4e4` has **API Restrictions** applied that either exclude **Identity Toolkit API (`identitytoolkit.googleapis.com`)** or restrict the `google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdp` method.
   - Additionally, if the **Google** Sign-In provider is not toggled to **Enabled** in **Firebase Console -> Authentication -> Sign-in method**, federated IdP sign-in requests cannot complete.

### Ruled-Out Causes
- **SHA-1 / Keystore Mismatch**: Ruled out. `debug.keystore` SHA-1 (`0cc4f2a7011bf5d772bf3f414f8e2fd9caff236a`) matches `app/google-services.json` verbatim.
- **Wrong `default_web_client_id`**: Ruled out. The Type-3 Web OAuth client (`345432115619-b05ff1f7rj5reaa7qssm586bajas4n20.apps.googleusercontent.com`) belongs to the exact same project (`345432115619`).
- **Duplicate Firebase Initialization**: Ruled out. `FirebaseApp` is initialized once by the `google-services` plugin, and Firestore uses the single custom database ID `R.string.firestore_database_id`.

---

## 5. Recommended Fix & Manual Cloud Console Steps Required

### A. Code-Level Improvements Implemented in ShieldX
1. **Activity Context Unwrapping (`findActivity()`)**: Unwraps `ContextWrapper` in `AuthScreen.kt` so `CredentialManager.getCredential()` always receives a valid `Activity` context.
2. **Structured `AuthErrorAnalyzer` & Actionable Remediation UI**: Detects `API_KEY_SERVICE_BLOCKED` / `SignInWithIdp` errors, network interruptions, account absence (`NoCredentialException`), and user cancellations (`GetCredentialCancellationException`), presenting clear beginner-friendly instructions and direct links to the exact Google Cloud Console & Firebase Console pages without ever logging sensitive tokens or keys.
3. **Credential State Reset on Retry**: Clears stale credential state before retrying after a blocked `SignInWithIdp` response.

### B. Manual Google Cloud / Firebase Console Steps Required (User Action)
Because external Google Cloud Console API key restrictions cannot be modified from inside the Android build container after `ProvisionFirebase` completes, the project owner must allow `Identity Toolkit API` on the Android API key in Google Cloud Console (see `AUTH_FIX_PLAN.md` for step-by-step beginner instructions).

---

## 6. Billing, Permissions & Rollback Plan
- **Billing Impact**: None. Enabling **Identity Toolkit API** (`SignInWithIdp`) and Google Sign-In in Firebase Authentication is free on the standard Firebase Spark/Blaze tier (up to 50,000 MAUs).
- **Security Impact**: Do **not** remove all API restrictions globally; only add **Identity Toolkit API** and **Token Service API** to the Android API key's allowed APIs list while keeping the Android app package/SHA-1 restriction intact.
- **Rollback Plan**: All changes in `AuthScreen.kt` are self-contained and preserve existing ViewModel/Firestore contracts. Reverting `AuthScreen.kt` restores the prior UI immediately without affecting any database schema or user records.
