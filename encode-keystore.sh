#!/usr/bin/env bash
# Encodes your release keystore to base64 so it can be pasted into the
# GitHub secret ANDROID_KEYSTORE_BASE64.
#
# Usage: bash encode-keystore.sh path/to/meditrack-release.jks
set -e

if [ -z "$1" ]; then
  echo "Usage: bash encode-keystore.sh path/to/meditrack-release.jks"
  exit 1
fi

if [ ! -f "$1" ]; then
  echo "ERROR: file not found: $1"
  exit 1
fi

OUT="keystore-base64.txt"
base64 -i "$1" -o "$OUT" 2>/dev/null || base64 "$1" > "$OUT"

echo "Wrote base64-encoded keystore to $OUT"
echo "Copy its full contents into the GitHub secret: ANDROID_KEYSTORE_BASE64"
echo "(Settings -> Secrets and variables -> Actions -> New repository secret)"
echo ""
echo "Do NOT commit $OUT to git — delete it after copying the value."
