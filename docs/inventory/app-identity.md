# App Identity and Infrastructure Inventory

> Generated: 2026-07-29T15:55:00.000000000+03:30
> Task ID: P00-PROGRAM-DISC-002

## 1. Android Application IDs (Flavors)

| Flavor | Application ID | Notes |
|---|---|---|
| carmila | `com.kazemieh.shop` | Default |
| atris | `com.kazemieh.shop.atris` | |
| chronos | `com.kazemieh.shop.chronos` | |
| academy | `com.kazemieh.shop.academy` | |
| psych | `com.kazemieh.shop.psych` | |
| wp | `com.kazemieh.shop.wp` | WordPress Connector |

## 2. iOS Bundle Identifier

| Component | Bundle ID | Notes |
|---|---|---|
| Main App | `com.kazemieh.shop.kmpShop` | Derived from Config.xcconfig |

## 3. Signing Keys & Certificates

| Platform | Owner | Store | Location | Status |
|---|---|---|---|---|
| Android | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | Not found in repo |
| iOS | `UNKNOWN` | `UNKNOWN` | `UNKNOWN` | Not found in repo |

## 4. Domains & API Endpoints

| Usage | URL / Domain | Notes |
|---|---|---|
| Active Tunnel | `lopez-closely-demonstration-traveler.trycloudflare.com` | Current in PlatformConfig.android.kt |
| Alternative Tunnel | `ngrok-free.dev` | Commented out |
| Liara Deployment | `shop-server.liara.run` | Commented out |
| Puria Demo | `api.miaad.puriademo.ir` | Commented out |
| WP Placeholder | `example.com` | Found in Brand.kt |

## 5. Previous Artifacts

| Type | Path / Link | Version |
|---|---|---|
| WP Theme | `wordpress/carmilla-theme.zip` | 0.8.0 / 0.7.7 |
| WP Bridge | `wordpress/carmilla-bridge.zip` | 0.7.3 |
| Android APK | `NONE` | No APKs found in repository |
| Android AAB | `NONE` | No AABs found in repository |

---

## 6. Questions for Human Reviewer
1. **Signing Keys**: Are the release signing keys stored in a secure external vault or a private CI environment?
2. **Store Accounts**: Do you have active Google Play Console or Apple Developer Program accounts for these IDs?
3. **Artifact History**: Are there any external artifact repositories (e.g., GitHub Releases, S3) containing previous versions?
