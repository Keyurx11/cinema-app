# KeySmi Cinema

Android TV / FireStick wrapper for the KeySmi Cinema web app. Built by GitHub
Actions (Gradle) and published to the `v2.2.*` release below.

### 📺 FireStick / Android TV Installation

* **Downloader Code:** `5369613`
* **Direct URL (always newest):** `https://github.com/Keyurx11/cinema-app/releases/latest/download/keysmi-cinema.apk`
* **On the home LAN:** `http://192.168.68.116:5055/download/keysmi-cinema.apk`

The app opens `http://192.168.68.116:5055` full-screen with D-pad support. No
Tailscale needed on the TV — it talks straight to the mini-PC over WiFi.

### Build

`gradle assembleRelease` (JDK 17, Android SDK 34). CI signs the APK with a
persistent key (`SIGNING_KEYSTORE_B64` secret, else `signing/keysmi-release.jks`)
so installed TVs update in place.
