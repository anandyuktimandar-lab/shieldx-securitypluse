# ShieldX by SecuriPulse — Authentication Repair Plan (`AUTH_FIX_PLAN.md`)

## 1. Objective
Resolve the `google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdP is blocked` (`API_KEY_SERVICE_BLOCKED`) error while preserving all existing ShieldX cybersecurity, live crypto market, charting, Room SQLite, Cloud Firestore, and Gemini AI functionality.

---

## 2. Automated Code Fixes Implemented in the App

1. **Activity Context Resolution (`AuthScreen.kt`)**
   - Added `Context.findActivity()` helper so `CredentialManager.getCredential(request, activityContext)` always receives the host `Activity` context rather than a wrapped `Context`.
2. **Deterministic Error Classification (`AuthErrorAnalyzer` in `AuthScreen.kt`)**
   - Classifies exceptions thrown during `CredentialManager` and `Firebase.auth.signInWithCredential` into structured categories:
     - `SIGN_IN_WITH_IDP_BLOCKED`: Triggered when the error message contains `SignInWithIdp`, `SignInWithIdP`, or `API_KEY_SERVICE_BLOCKED`.
     - `USER_CANCELLED`: Triggered on `GetCredentialCancellationException`.
     - `NO_GOOGLE_ACCOUNT`: Triggered on `NoCredentialException`.
     - `NETWORK_ERROR`: Triggered on `FirebaseNetworkException` or connectivity timeouts.
     - `GENERAL_AUTH_ERROR`: Any other unexpected authentication exception.
3. **Interactive Cloud Setup Guide & One-Tap Console Links (`AuthScreen.kt`)**
   - When `SIGN_IN_WITH_IDP_BLOCKED` occurs, `AuthScreen` renders a step-by-step **Cloud API Key Unblock Guide** tailored specifically to Firebase project `studio-4054916737-dd4e4` with buttons to open Google Cloud Credentials and Firebase Authentication settings in the browser.
4. **Clean Retry & Session Hygiene**
   - Prevents duplicate taps while `isSigningIn` is true.
   - Clears cached `CredentialManager` state before retrying when a previous token exchange was blocked.
   - Never logs ID tokens, access tokens, or full API keys.

---

## 3. Simple Step-by-Step Manual Instructions (Google Cloud & Firebase Console)

Because Google Cloud restricts API keys at the project level (`studio-4054916737-dd4e4`), please complete these two quick checks in your browser:

### Step 1: Allow `Identity Toolkit API` on Your Android API Key
1. **What to open**: Open the Google Cloud Console Credentials page for project `studio-4054916737-dd4e4`:
   `https://console.cloud.google.com/apis/credentials?project=studio-4054916737-dd4e4`
2. **Where to click**: Click on the Android API key (labeled **Android key (auto created by Firebase)**, ending in `...Bfkk`).
3. **What to select**:
   - Scroll down to **API restrictions**.
   - Under **Restrict key**, click the **Select APIs** dropdown.
   - Check the box next to **Identity Toolkit API** (and ensure all methods under Identity Toolkit API, including `SignInWithIdp`, are checked) and **Token Service API**.
   - Click **OK**, then click **Save** at the bottom of the page.
4. **What result to expect**: Within 1–2 minutes, Google Cloud will allow `SignInWithIdp` requests from your Android app.

### Step 2: Confirm Google Sign-In Provider is Enabled in Firebase Console
1. **What to open**: Open the Firebase Console Sign-in Method page for project `studio-4054916737-dd4e4`:
   `https://console.firebase.google.com/project/studio-4054916737-dd4e4/authentication/providers`
2. **Where to click**: Click **Google** in the list of Sign-in providers.
3. **What to select**: Ensure the **Enable** toggle is turned **ON**, select your support email if prompted, and click **Save**.
4. **What result to expect**: Returning to the ShieldX app and tapping **Sign in with Google** will complete the Firebase token exchange and open your ShieldX dashboard.
