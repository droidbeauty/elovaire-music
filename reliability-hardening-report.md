# Reliability hardening

## Findings and fixes

- MediaStore and SAF scans could represent one physical file with unrelated URIs and paths. A source-aware duplicate resolver now prefers MediaStore entries, removes overlapping SAF results, and rewrites deduplicated snapshots.
- Resolved SAF roots now retain a verified local path for child files. Synthetic SAF paths remain only for providers without a safe local mapping.
- Parent/child local folder selections are collapsed conservatively; unresolved document-provider roots remain independent.
- MediaStore metadata cache entries are keyed by URI rather than only `_ID`, preventing cross-volume metadata reuse. Row URIs now use the reported volume name.
- Genre lookup prefers the row's volume and never truncates a `Long` media ID for the platform `Int` API.
- A missing MediaStore cursor now fails the scan instead of publishing an accidental empty library. Observer registration and rebuild jobs are idempotent and stale rebuild jobs cannot replace newer observers.
- Full media-index traversal and directory observers skip symbolic-link directories. SAF scanning reuses one filter per tree instead of allocating one per file.
- External audio IDs use a SHA-256-derived reserved negative range. Preference persistence now clamps play-count overflow and removes invalid favorite IDs. Room schema export is enabled.

## Android references

- https://developer.android.com/training/data-storage/shared/media
- https://developer.android.com/training/data-storage/shared/documents-files
- https://developer.android.com/reference/android/provider/DocumentsContract
- https://developer.android.com/topic/architecture/recommendations
- https://developer.android.com/media/media3/session/background-playback

## Validation

- `./gradlew clean`: passed.
- `./gradlew debugQualityCheck`: passed.
- `./gradlew buildHealth`: passed.
- `./gradlew :app:testDebugUnitTest --tests '*Library*'`: passed.
- Focused duplicate resolver, folder normalization, snapshot, cache, genre, and audio-filter tests: passed.
- `./gradlew :app:connectedDebugAndroidTest`: passed on Pixel_8_Pro AVD, Android 17 / API 37. The navigation and player smoke test passed.

## Deferred validation

- Macrobenchmark, Baseline Profile generation, and the complete manual media-mutation/update/route test matrix were not run in this pass.
- USB/direct-output validation requires compatible physical audio hardware.

No user-facing feature was removed and no UI redesign was introduced.
