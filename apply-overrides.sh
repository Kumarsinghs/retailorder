#!/usr/bin/env bash
# Copies all prebuilt overrides (manifest, icons, splash, gradle config, MainActivity)
# into the android/ project created by `npx cap add android`.
#
# Usage: run this AFTER `npx cap add android`, BEFORE `npx cap sync android`.
set -e

if [ ! -d "android" ]; then
  echo "ERROR: android/ folder not found. Run 'npx cap add android' first."
  exit 1
fi

echo "==> Applying AndroidManifest.xml"
cp android-overrides/AndroidManifest.xml android/app/src/main/AndroidManifest.xml

echo "==> Applying MainActivity.java"
mkdir -p android/app/src/main/java/com/meditrack/retailorder
cp android-overrides/java/com/meditrack/retailorder/MainActivity.java \
   android/app/src/main/java/com/meditrack/retailorder/MainActivity.java

echo "==> Applying values (colors, styles, strings)"
mkdir -p android/app/src/main/res/values
cp android-overrides/values/*.xml android/app/src/main/res/values/

echo "==> Applying xml/ (network security config, file paths)"
mkdir -p android/app/src/main/res/xml
cp android-overrides/xml/*.xml android/app/src/main/res/xml/

echo "==> Applying gradle config"
echo "==> Applying gradle config"
# cp android-overrides/variables.gradle android/variables.gradle
# cp android-overrides/app-build.gradle android/app/build.gradle
# cp android-overrides/build.gradle.root android/build.gradle
# cp android-overrides/proguard-rules.pro android/app/proguard-rules.pro
cp android-overrides/keystore.properties.example android/../keystore.properties.example 2>/dev/null || \
  cp android-overrides/keystore.properties.example keystore.properties.example

echo "==> Applying launcher icons + adaptive icon + splash screen"
for d in mdpi hdpi xhdpi xxhdpi xxxhdpi; do
  mkdir -p android/app/src/main/res/mipmap-$d
  cp android-res/mipmap-$d/*.png android/app/src/main/res/mipmap-$d/
done
mkdir -p android/app/src/main/res/mipmap-anydpi-v26
cp android-res/mipmap-anydpi-v26/*.xml android/app/src/main/res/mipmap-anydpi-v26/

mkdir -p android/app/src/main/res/drawable android/app/src/main/res/drawable-night
cp android-res/drawable/splash.png android/app/src/main/res/drawable/splash.png
cp android-res/drawable-night/splash.png android/app/src/main/res/drawable-night/splash.png

echo ""
echo "Done. Next steps:"
echo "  npx cap sync android"
echo "  npx cap open android      # or: cd android && ./gradlew assembleDebug"
