#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_PATH="${ROOT_DIR}/iosApp/iosApp.xcodeproj"
SCHEME="${IOS_SCHEME:-iosApp}"
CONFIGURATION="${IOS_CONFIGURATION:-Release}"
ARCHIVE_PATH="${IOS_ARCHIVE_PATH:-${ROOT_DIR}/build/ios/${SCHEME}.xcarchive}"
EXPORT_PATH="${IOS_EXPORT_PATH:-${ROOT_DIR}/build/ios/export}"
EXPORT_OPTIONS="${IOS_EXPORT_OPTIONS:-${ROOT_DIR}/iosApp/Configuration/ExportOptions.plist}"
GOOGLE_SERVICE_INFO="${GOOGLE_SERVICE_INFO:-${ROOT_DIR}/iosApp/iosApp/GoogleService-Info.plist}"
APP_ID="${FIREBASE_APP_ID:-$(/usr/libexec/PlistBuddy -c 'Print :GOOGLE_APP_ID' "${GOOGLE_SERVICE_INFO}")}"
IPA_PATH="${IPA_PATH:-${EXPORT_PATH}/${SCHEME}.ipa}"
RELEASE_NOTES="${RELEASE_NOTES:-iOS build $(date '+%Y-%m-%d %H:%M:%S %Z')}"

if ! command -v firebase >/dev/null 2>&1; then
  echo "firebase CLI is not installed. Run: npm install -g firebase-tools" >&2
  exit 1
fi

if [[ -z "${FIREBASE_TESTERS:-}" && -z "${FIREBASE_GROUPS:-}" ]]; then
  echo "Set FIREBASE_TESTERS or FIREBASE_GROUPS before uploading." >&2
  echo "Example: FIREBASE_TESTERS=tester@example.com $0" >&2
  exit 1
fi

mkdir -p "$(dirname "${ARCHIVE_PATH}")"
rm -rf "${ARCHIVE_PATH}" "${EXPORT_PATH}"
mkdir -p "${EXPORT_PATH}"

xcodebuild archive \
  -project "${PROJECT_PATH}" \
  -scheme "${SCHEME}" \
  -configuration "${CONFIGURATION}" \
  -destination "generic/platform=iOS" \
  -archivePath "${ARCHIVE_PATH}" \
  -allowProvisioningUpdates

xcodebuild -exportArchive \
  -archivePath "${ARCHIVE_PATH}" \
  -exportPath "${EXPORT_PATH}" \
  -exportOptionsPlist "${EXPORT_OPTIONS}" \
  -allowProvisioningUpdates

if [[ ! -f "${IPA_PATH}" ]]; then
  found_ipa="$(find "${EXPORT_PATH}" -maxdepth 1 -name '*.ipa' -print -quit)"
  if [[ -z "${found_ipa}" ]]; then
    echo "IPA was not created in ${EXPORT_PATH}" >&2
    exit 1
  fi
  IPA_PATH="${found_ipa}"
fi

firebase_args=(
  appdistribution:distribute "${IPA_PATH}"
  --app "${APP_ID}"
  --release-notes "${RELEASE_NOTES}"
)

if [[ -n "${FIREBASE_TESTERS:-}" ]]; then
  firebase_args+=(--testers "${FIREBASE_TESTERS}")
fi

if [[ -n "${FIREBASE_GROUPS:-}" ]]; then
  firebase_args+=(--groups "${FIREBASE_GROUPS}")
fi

firebase "${firebase_args[@]}"
