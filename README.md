# MediTrack Retail Order — Android App (Capacitor)

Native Android wrapper for **https://meditrack-retailorder.netlify.app/**, built with Capacitor.

**Package name:** `com.meditrack.retailorder`
**App name:** MediTrack Retail Order
**Min SDK:** 26 (Android 8.0+)
**Target SDK:** 35

---

## 0. Important — how this app works

This project loads your **live, deployed website** directly inside a native WebView
(configured in `capacitor.config.ts` → `server.url`). That means:

- Every feature works exactly as it does on the website today — no re-implementation, no drift.
- Any update you push to Netlify shows up in the app automatically, with no rebuild needed.
- The `www/` folder here is just a placeholder Capacitor requires — it is **not** what users see.

This is the standard, Google-approved approach (Trusted Web Activity / WebView wrapper) for
turning an existing hosted PWA into a Play Store app when you're distributing from the same
domain. If instead you want the JS/CSS bundled **inside** the APK (fully offline-first, no
network call needed even on first launch), see **§7 "Going fully offline-first"** below.

Separately, the `web-fixes/` folder contains files to add to your **website's own repo** so the
site itself becomes a fully spec-compliant, installable PWA (this matters for anyone who opens
it in mobile Chrome directly, and improves the in-app offline behavior too).

---

## 1. Prerequisites

Install once:
- [Node.js 18+](https://nodejs.org)
- [Android Studio](https://developer.android.com/studio) (includes the Android SDK)
- JDK 17 (bundled with recent Android Studio)

---

## 2. One-time project setup

```bash
# 1. Unzip this project and enter it
cd meditrack-retailorder-android

# 2. Install JS dependencies (Capacitor CLI + plugins)
npm install

# 3. Generate the native Android project (creates the android/ folder)
npx cap add android

# 4. Copy the prebuilt overrides (icons, manifest, gradle config, MainActivity)
#    into the generated android/ project
bash apply-overrides.sh

# 5. Sync Capacitor config + plugins into the native project
npx cap sync android
```

After this, `android/` is a **complete, standard Android Studio project**. Open it with:

```bash
npx cap open android
```
(or open the `android/` folder directly in Android Studio)

---

## 3. What `apply-overrides.sh` does

`npx cap add android` scaffolds a generic native shell. The script copies these prebuilt,
production-ready files on top of it:

| Override | Destination |
|---|---|
| `android-overrides/AndroidManifest.xml` | `android/app/src/main/AndroidManifest.xml` |
| `android-overrides/java/.../MainActivity.java` | `android/app/src/main/java/com/meditrack/retailorder/MainActivity.java` |
| `android-overrides/values/*.xml` | `android/app/src/main/res/values/` |
| `android-overrides/xml/*.xml` | `android/app/src/main/res/xml/` |
| `android-overrides/variables.gradle` | `android/variables.gradle` |
| `android-overrides/app-build.gradle` | `android/app/build.gradle` |
| `android-overrides/build.gradle.root` | `android/build.gradle` |
| `android-overrides/proguard-rules.pro` | `android/app/proguard-rules.pro` |
| `android-res/mipmap-*/*` | `android/app/src/main/res/mipmap-*/` (launcher + adaptive icons) |
| `android-res/drawable*/splash.png` | `android/app/src/main/res/drawable*/` (splash screen) |

All permissions (camera, storage, network, notifications), the FileProvider for camera/file
uploads-downloads, HTTPS-only network security config, portrait lock, and back-button handling
are already wired into these files — no manual edits needed.

---

## 4. Icons & splash screen

Already generated and included in `icons/` and `android-res/`:
- Adaptive icon (foreground + background layers) for Android 8+
- Legacy + round launcher icons at mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi
- 1024×1024 Play Store listing icon (`icons/play_store_icon_1024.png`)
- Splash screen (light + dark) matching the site's `#0A1628` theme color

To regenerate from a different source logo later:
```bash
npx @capacitor/assets generate --android --iconBackgroundColor '#0A1628' --splashBackgroundColor '#0A1628'
```
(place a 1024×1024 `icon.png` and 2732×2732 `splash.png` in `resources/` first)

---

## 5. Build commands

### Debug APK (for testing, unsigned/debug-signed)
```bash
cd android
./gradlew assembleDebug
# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (signed, for direct install/sideloading)
```bash
cd android
./gradlew assembleRelease
# Output: android/app/build/outputs/apk/release/app-release.apk
```

### Release AAB (for Google Play Store submission)
```bash
cd android
./gradlew bundleRelease
# Output: android/app/build/outputs/bundle/release/app-release.aab
```

> Release builds require signing to be configured first — see §6.

---

## 6. Signing configuration (required for release APK/AAB)

### Step 1 — Generate a release keystore (do this once, store it safely — losing it means
you can never update the app on Play Store again)

```bash
keytool -genkey -v -keystore meditrack-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias meditrack
```
Follow the prompts (organization, name, password). Move the resulting `meditrack-release.jks`
into the `android/` folder.

### Step 2 — Configure Gradle to use it
```bash
cp keystore.properties.example android/keystore.properties
```
Edit `android/keystore.properties`:
```properties
storeFile=../meditrack-release.jks
storePassword=<your store password>
keyAlias=meditrack
keyPassword=<your key password>
```
This file is git-ignored — never commit it or the `.jks` file.

### Step 3 — Build
```bash
cd android
./gradlew bundleRelease   # for Play Store
./gradlew assembleRelease # for a signed APK
```
`app-build.gradle` (already applied) automatically picks up `keystore.properties` and signs the
release build type with it.

---

## 7. Going fully offline-first (optional, later upgrade)

Right now the app requires network on first launch (it loads the live site). If you later want
the HTML/CSS/JS bundled inside the APK so the app works fully offline from install:
1. Get a production build of your site (`npm run build` in your site's repo, or download the
   deployed `dist`/`build` folder).
2. Replace the contents of this project's `www/` folder with that build output.
3. Remove (or comment out) the `server.url` block in `capacitor.config.ts`.
4. Run `npx cap sync android` and rebuild.

Both approaches can also be combined (bundle a cached shell locally, still fetch live data over
the network) — ask if you'd like that variant scaffolded.

---

## 8. Adding the PWA fixes to your website repo

Copy from `web-fixes/` into your site's project root / public folder:
- `manifest.json` → site root (referenced via `<link rel="manifest" href="/manifest.json">` in `<head>`)
- `sw.js` → site root
- `register-sw.js` → paste its contents into your app's entry file, or link it as a `<script>` tag
- `offline.html` → site root

Also add to `<head>` if not already present:
```html
<meta name="theme-color" content="#0A1628">
<link rel="manifest" href="/manifest.json">
<link rel="apple-touch-icon" href="/icons/icon-192.png">
```
You'll need to export actual icon PNGs at the sizes listed in `manifest.json` — Chrome DevTools
→ Application → Manifest can validate everything once deployed. If your framework is Vite/CRA/
Next, most have a plugin (`vite-plugin-pwa`, `next-pwa`, CRA's built-in `service-worker.js`) that
automates this — let me know your exact framework and I can wire it in precisely instead of the
generic vanilla version here.

---

## 9. Permissions included

| Permission | Why |
|---|---|
| INTERNET, ACCESS_NETWORK_STATE | Load the app, detect online/offline |
| CAMERA | Prescription/invoice photo capture; future barcode & QR scanning |
| READ_MEDIA_IMAGES / READ_EXTERNAL_STORAGE (≤32) | File uploads |
| WRITE_EXTERNAL_STORAGE (≤29) | File downloads on older Android |
| POST_NOTIFICATIONS | Future push notifications |
| VIBRATE, WAKE_LOCK, RECEIVE_BOOT_COMPLETED | Push notification support infra |

All are declared in `AndroidManifest.xml`; camera/storage are requested at runtime by the
`@capacitor/camera` and `@capacitor/filesystem` plugins (already in `package.json`) when your
web code calls them — nothing else to configure.

---

## 10. Future-ready integrations already scaffolded

- **Barcode/QR scanning:** `@capacitor/camera` is installed; add
  `@capacitor-community/barcode-scanner` or `@capacitor-mlkit/barcode-scanning` when ready — camera
  permission is already declared.
- **Push notifications:** `@capacitor/push-notifications` is installed and the manifest/gradle
  are wired for it. To activate: create a Firebase project, drop `google-services.json` into
  `android/app/`, and call `PushNotifications.register()` from your web code.
- **QR code generation:** works entirely in the webview (canvas/SVG based libs) — no native
  changes needed.

---

## 11. GitHub Actions cloud build (no local Android Studio required)

A ready-to-use workflow is included at `.github/workflows/android-build.yml`. It regenerates the
native project fresh on every run (same `cap add android` + `apply-overrides.sh` + `cap sync`
steps as local setup), then builds:
- **Debug APK** — always, no setup required.
- **Signed Release APK + AAB** — only if you've added signing secrets (see below).

### Step 1 — Push this project to a GitHub repo
```bash
cd meditrack-retailorder-android
git init
git add .
git commit -m "Initial Capacitor Android project"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```
(Create the empty repo first at github.com/new — don't initialize it with a README so the push
doesn't conflict.)

### Step 2 — Watch it build
Go to your repo → **Actions** tab. The workflow runs automatically on push, or click **Run
workflow** to trigger it manually. It takes ~5-8 minutes. When it finishes, open the run →
scroll to **Artifacts** → download `meditrack-debug-apk.zip`, unzip it, and you have your APK.

At this point you already have a working, installable APK with zero local Android Studio setup.

### Step 3 (optional) — Enable signed release builds
To also get a signed release APK/AAB (needed for Play Store or a "real" production APK):

1. Generate a keystore locally once (needs a JDK installed — or do this from Android Studio's
   "Generate Signed Bundle" dialog as in the previous instructions, then use the resulting file):
   ```bash
   keytool -genkey -v -keystore meditrack-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 -alias meditrack
   ```
2. Encode it to base64:
   ```bash
   bash encode-keystore.sh meditrack-release.jks
   ```
   This writes `keystore-base64.txt` — open it and copy the full contents.
3. In your GitHub repo: **Settings → Secrets and variables → Actions → New repository secret**.
   Add these four secrets:
   | Name | Value |
   |---|---|
   | `ANDROID_KEYSTORE_BASE64` | contents of `keystore-base64.txt` |
   | `ANDROID_KEYSTORE_PASSWORD` | the store password you set with `keytool` |
   | `ANDROID_KEY_ALIAS` | `meditrack` (or whatever alias you used) |
   | `ANDROID_KEY_PASSWORD` | the key password you set with `keytool` |
4. Delete `keystore-base64.txt` locally (don't commit it) and keep `meditrack-release.jks` backed
   up somewhere safe outside git — you'll need the exact same file for every future update.
5. Re-run the workflow (push a commit, or **Run workflow** manually). This time it will also
   produce `meditrack-release-apk` and `meditrack-release-aab` artifacts, ready for sideloading
   or Play Store upload respectively.

---

## 12. Performance & UX notes

- WebView is hardware-accelerated with GPU compositing and DOM storage enabled for smooth
  scrolling and localStorage/offline caching.
- Native "no internet" overlay shows automatically when the device loses connectivity, on top of
  the site's own offline banner.
- Native splash screen shows for 2s (or until the page is ready) while the site loads.
- Hardware back button navigates WebView history first, then requires a second press to exit
  from the root screen (prevents accidental exits mid-order).
- `shrinkResources` + `minifyEnabled` are on for release builds to keep APK size and RAM
  footprint down.
- Portrait orientation is locked at the manifest level.

---

## Project structure reference

```
meditrack-retailorder-android/
├── capacitor.config.ts       # points the WebView at the live PWA
├── package.json
├── apply-overrides.sh        # copies overrides into android/ after `cap add android`
├── www/                      # placeholder webDir (site loads via server.url instead)
├── icons/                    # source-generated icon & splash PNGs
├── android-res/              # ready-to-copy mipmap/drawable resources
├── android-overrides/        # AndroidManifest, MainActivity, gradle files, styles
├── web-fixes/                # manifest.json / sw.js / offline.html for your site repo
└── android/                  # created by `npx cap add android` (full Android Studio project)
```
