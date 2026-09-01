<p align="center">
  <img src="assets/logo.png" alt="KeymapKit logo" width="128"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-8.0%2B-brightgreen"/>
  <img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF"/>
  <img src="https://img.shields.io/badge/Jetpack-Compose-4285F4"/>
  <img src="https://img.shields.io/badge/Material-3-2196F3"/>
  <img src="https://img.shields.io/github/license/mahmutaunal/KeymapKit"/>
  <img src="https://img.shields.io/github/v/release/mahmutaunal/KeymapKit"/>
  <img src="https://img.shields.io/github/stars/mahmutaunal/KeymapKit"/>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.alpware.keymapkit">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png"
         alt="Get it on Google Play"
         height="80"/>
  </a>
</p>

<h1 align="center">KeymapKit</h1>

<p align="center">
  <strong>Add missing physical keyboard layouts to Android.</strong><br/>
  Lightweight • Offline • No Root • No Permissions • Open Source
</p>

---

## ✨ Why KeymapKit?

Android supports external keyboards, but many useful physical keyboard layouts are missing.

KeymapKit fills that gap by adding **system-level physical keyboard layouts** such as:

- 🇹🇷 Turkish F
- 🇹🇷 Turkish Q
- and many more in future releases.

Unlike traditional keyboard apps:

- ✅ No on-screen keyboard
- ✅ No IME
- ✅ No root
- ✅ No Accessibility Service
- ✅ No permissions
- ✅ Works completely offline

KeymapKit simply provides additional **hardware keyboard layouts** that Android can use.

---

## 📸 Screenshots

<p align="center">
  <img src="assets/store/en/1.png" width="200"/>
  <img src="assets/store/en/2.png" width="200"/>
  <img src="assets/store/en/3.png" width="200"/>
  <img src="assets/store/en/4.png" width="200"/>
</p>

Current screens include:

- Home dashboard
- First-time setup
- Layout Manager
- Typing Test
- Diagnostics
- Material You interface

---

# 🚀 What's New in KeymapKit 2.0

KeymapKit now installs **only the keyboard layouts you actually need**.

### New First-Time Setup

Choose your preferred layouts during the first launch.

For example:

- Turkish Q
- Turkish F
- English US
- German
- French

Only selected layouts become available inside Android.

---

### New Layout Manager

Manage installed layouts anytime.

- Add layouts
- Remove layouts
- Search layouts
- View installed layouts
- Update selections instantly

No reinstall required.

---

### Recommended Layouts

KeymapKit automatically recommends layouts based on your current device language.

Examples:

- Turkish device → Turkish Q + Turkish F
- English device → English US

---

### Typing Test

Verify your selected layout immediately.

No third-party app is required.

---

### Diagnostics

Built-in diagnostics help verify:

- Physical keyboard detection
- Active layouts
- Provider status
- Connected keyboards

---

## ✨ Features

- ✅ System-level physical keyboard layouts
- ✅ Works in every application
- ✅ Dynamic layout installation
- ✅ Layout Manager
- ✅ Typing Test
- ✅ Diagnostics
- ✅ Material 3 / Material You
- ✅ Dark Mode
- ✅ Android 8.0+
- ✅ Lightweight
- ✅ Offline
- ✅ Open Source

---

## 🧠 How It Works

Android supports physical keyboard layouts through `.kcm` (Key Character Map) files.

KeymapKit registers itself as a **Keyboard Layout Provider** and exposes these layouts directly to Android.

Unlike an IME:

- No text input service
- No keyboard replacement
- No accessibility service
- No background process

Android itself performs all key mapping.

---

## 📱 Setup

1. Install KeymapKit
2. Connect a USB or Bluetooth keyboard
3. Launch KeymapKit
4. Select the layouts you want
5. Open:

```
Settings
→ Physical Keyboard
→ Choose KeymapKit layout
```

6. Open the built-in Typing Test
7. Verify everything works

> Samsung devices may require tapping the language entry (for example, "Turkish (Türkiye)") before the available layouts become visible.

---

## 🔒 Privacy

KeymapKit follows a strict privacy-first philosophy.

Keyboard layouts, selected-layout preferences and typing-test content stay on the device. The app
does not create accounts or transmit keyboard input. The official Google Play build uses Google
AdMob, UMP, Firebase Analytics and Remote Config for advertising, consent, aggregate traffic-quality
measurement and safety controls. See [PRIVACY.md](PRIVACY.md) for the complete disclosure.

---

## 🧩 Supported Layouts

Current:

- 🇹🇷 Turkish F
- 🇹🇷 Turkish Q

Planned:

- 🇺🇸 English (US)
- 🇬🇧 English (UK)
- 🇩🇪 German
- 🇫🇷 French
- 🇪🇸 Spanish
- 🇮🇹 Italian
- More community layouts

---

## 🛠 Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Material You
- Android Keyboard Layout API
- Kotlin Coroutines

No XML UI.

---

## 🧪 Testing

KeymapKit includes:

- Typing Test
- Diagnostics
- Keyboard detection
- Layout verification

These tools make it easier to validate different USB and Bluetooth keyboards.

---

## 📄 Third-Party Notice

Some keyboard layout definitions (`.kcm`) are derived from:

**More Physical Keyboard Layouts / extra-keyboard-layouts**

Licensed under the MIT License.

See:

```
THIRD_PARTY_NOTICES.md
```

for attribution details.

---

## 📦 Google Play

Download KeymapKit from Google Play:

https://play.google.com/store/apps/details?id=com.alpware.keymapkit

---

## 🤝 Contributing

Contributions are always welcome.

Especially:

- New keyboard layouts
- Documentation
- Generator improvements
- Bug fixes
- Device compatibility reports

Please open an Issue or Pull Request.

---

## 📜 License

Apache License 2.0

---

## 👨‍💻 Developed by

**AlpWare Studio**

---

<p align="center">
<strong>KeymapKit — because physical keyboards deserve proper layouts.</strong>
</p>
## Google Play experience

KeymapKit uses the official Google Play In-App Review and In-App Updates APIs. Automatic review requests are intentionally conservative and are evaluated only after repeated successful use: multiple layout changes, opening physical keyboard settings, multiple sessions, and at least two days since first use. Google Play always decides whether the review card is shown.

Review eligibility counters remain on the device in `SharedPreferences`. Advertising operations use
the aggregate Firebase telemetry described below; review eligibility data and typing-test content are
not sent to Analytics.

## Advertising in official builds

The official Google Play release is supported by Google AdMob. Debug builds use Google's
sample ad identifiers, while release builds use the maintainer's production identifiers.
Forks should replace or disable the release identifiers before publishing their own builds.

Users can unlock **Premium** with the non-consumable Google Play product `premium_lifetime`.
Premium is a one-time purchase whose only benefit is removing all ads. Purchases are restored from
the current Google Play account and cached locally so an already confirmed entitlement remains
available offline.

For ad-free Premium testing, change only `keymapkit.premium.test=true` in `gradle.properties` and
rebuild the debug app. The override is guarded by `BuildConfig.DEBUG`, so it cannot unlock Premium
in a release build. A one-off debug build can use `-Pkeymapkit.premium.test=true` instead.

A fixed 320x50 banner slot is reserved at the bottom and centered horizontally, so content never
jumps when an ad loads.
Banner and interstitial loads use lifecycle-aware bounded exponential backoff and a device-local
circuit breaker. Interstitials are eligible after every three completed keyboard-layout changes;
there is no minimum session age, display cooldown, per-session cap, or daily impression cap. Remote
Config can disable either ad format; the three-change threshold is fixed in the client.

Firebase Analytics records aggregate ad operations for traffic-quality monitoring. Country, app
version and acquisition source are supplied by Firebase's standard reporting dimensions; custom
events add ad format, placement, operation, load errors and local safety alerts. See
[`docs/ADMOB_OPERATIONS.md`](docs/ADMOB_OPERATIONS.md) for the required console setup.
See [`docs/PREMIUM_BILLING.md`](docs/PREMIUM_BILLING.md) for Play Console product setup, testing,
security boundaries, and the launch-price recommendation.
