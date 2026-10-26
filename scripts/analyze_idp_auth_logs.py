#!/usr/bin/env python3
"""
ShieldX by SecuriPulse — Verbose Identity Toolkit (SignInWithIdP) Log & Network Analyzer
========================================================================================
Intercepts/probes and analyzes verbose authentication responses from Google Cloud
Identity Toolkit (`google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdp`)
to pinpoint whether a failure is caused by:
  1. Misconfigured Project Scopes / API Key Service Restrictions (`API_KEY_SERVICE_BLOCKED`)
  2. SHA-1 Signing Certificate / Package Name Mismatch (`API_KEY_ANDROID_APP_BLOCKED`)
  3. App Integrity / Firebase App Check Enforcement (`APP_CHECK_INVALID`)
"""

import json
import os
import subprocess
import urllib.error
import urllib.request
from pathlib import Path


def mask_secret(value: str) -> str:
    if not value or len(value) < 12:
        return "[MASKED]"
    return f"{value[:8]}...{value[-4:]}"


def extract_keystore_sha1() -> str:
    keystore = Path("debug.keystore")
    if not keystore.exists():
        return "MISSING_KEYSTORE"
    try:
        out = subprocess.check_output(
            [
                "keytool", "-list", "-v",
                "-keystore", "debug.keystore",
                "-alias", "androiddebugkey",
                "-storepass", "android",
                "-keypass", "android",
            ],
            stderr=subprocess.STDOUT,
            text=True,
        )
        for line in out.splitlines():
            if "SHA1:" in line or "SHA-1:" in line:
                return line.split(":", 1)[1].strip().replace(":", "").upper()
    except Exception as exc:
        return f"ERROR({exc})"
    return "UNKNOWN"


def inspect_google_services() -> dict:
    gs_path = Path("app/google-services.json")
    if not gs_path.exists():
        return {}
    data = json.loads(gs_path.read_text(encoding="utf-8"))
    project_info = data.get("project_info", {})
    client = (data.get("client") or [{}])[0]
    package_name = (
        client.get("client_info", {})
        .get("android_client_info", {})
        .get("package_name", "")
    )
    api_key = ((client.get("api_key") or [{}])[0]).get("current_key", "")
    cert_hashes = []
    for oauth in client.get("oauth_client", []):
        android_info = oauth.get("android_info", {})
        if android_info.get("certificate_hash"):
            cert_hashes.append(android_info["certificate_hash"].upper())
    return {
        "project_id": project_info.get("project_id", ""),
        "project_number": project_info.get("project_number", ""),
        "package_name": package_name,
        "api_key": api_key,
        "registered_sha1_hashes": cert_hashes,
    }


def probe_sign_in_with_idp(api_key: str, package_name: str, sha1_hex: str) -> tuple[int, dict]:
    url = f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key={api_key}"
    payload = json.dumps({
        "postBody": "id_token=diagnostic_probe_token&providerId=google.com",
        "requestUri": "http://localhost",
        "returnIdpCredential": True,
        "returnSecureToken": True,
    }).encode("utf-8")

    req = urllib.request.Request(
        url,
        data=payload,
        method="POST",
        headers={
            "Content-Type": "application/json",
            "X-Android-Package": package_name,
            "X-Android-Cert": sha1_hex,
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            return resp.status, json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as err:
        raw_body = err.read().decode("utf-8", errors="replace")
        try:
            return err.code, json.loads(raw_body)
        except Exception:
            return err.code, {"raw_error": raw_body}
    except Exception as exc:
        return 0, {"network_exception": str(exc)}


def classify_failure(http_status: int, body: dict, sha1_match: bool) -> dict:
    err = body.get("error", {})
    message = err.get("message", "")
    status = err.get("status", "")
    details = err.get("details", [])

    error_reason = "UNKNOWN"
    metadata = {}
    for item in details:
        if item.get("@type", "").endswith("ErrorInfo"):
            error_reason = item.get("reason", "UNKNOWN")
            metadata = item.get("metadata", {})
            break

    if error_reason == "API_KEY_SERVICE_BLOCKED" or "SignInWithIdp are blocked" in message:
        category = "MISCONFIGURED_PROJECT_SCOPES_OR_API_KEY_RESTRICTION"
        explanation = (
            "The Android API key in Google Cloud has API restrictions that block "
            "google.cloud.identitytoolkit.v1.AuthenticationService.SignInWithIdp "
            "(Identity Toolkit API). SHA-1 and App Integrity were NOT the cause of this rejection."
        )
    elif error_reason in ("API_KEY_ANDROID_APP_BLOCKED", "ACCESS_TOKEN_SCOPE_INSUFFICIENT") or not sha1_match:
        category = "SHA1_CERTIFICATE_OR_PACKAGE_MISMATCH"
        explanation = (
            "The APK's signing SHA-1 fingerprint or package_name was rejected by Google Cloud "
            "Android application restrictions."
        )
    elif "APP_CHECK" in error_reason or "appIntegrity" in message:
        category = "APP_INTEGRITY_OR_APP_CHECK_REJECTION"
        explanation = (
            "Firebase App Check / Play Integrity enforcement rejected the client attestation token."
        )
    elif "INVALID_IDP_RESPONSE" in message:
        category = "ENDPOINT_REACHABLE_AND_UNBLOCKED"
        explanation = (
            "SignInWithIdp endpoint accepted the API key and SHA-1 headers and reached token verification."
        )
    else:
        category = f"OTHER_{status}_{error_reason}"
        explanation = f"HTTP {http_status}: {message}"

    return {
        "http_status": http_status,
        "grpc_status": status,
        "error_reason": error_reason,
        "root_cause_category": category,
        "service": metadata.get("service", "identitytoolkit.googleapis.com"),
        "method": metadata.get("methodName", "SignInWithIdp"),
        "consumer": metadata.get("consumer", ""),
        "diagnostic_summary": explanation,
    }


def main() -> None:
    gs = inspect_google_services()
    local_sha1 = extract_keystore_sha1()
    sha1_match = local_sha1 in gs.get("registered_sha1_hashes", [])

    http_status, response_body = probe_sign_in_with_idp(
        api_key=gs.get("api_key", ""),
        package_name=gs.get("package_name", ""),
        sha1_hex=local_sha1,
    )
    classification = classify_failure(http_status, response_body, sha1_match)

    report = {
        "project_id": gs.get("project_id"),
        "project_number": gs.get("project_number"),
        "package_name": gs.get("package_name"),
        "masked_api_key": mask_secret(gs.get("api_key", "")),
        "local_debug_keystore_sha1": local_sha1,
        "google_services_registered_sha1": gs.get("registered_sha1_hashes", []),
        "sha1_certificate_matches": sha1_match,
        "identity_toolkit_probe": classification,
    }
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
