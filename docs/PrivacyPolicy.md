# Privacy Policy

Last updated: 2026-07-03

This document summarizes what Elovaire does with user data. It is written as a plain-language privacy summary for the app and repository. It is not a substitute for jurisdiction-specific legal advice.

## Summary

Elovaire is a local-first Android music player. Its core playback, library, playlist, search, equalizer, lyrics display, tag editing, and media-library features are designed to run on the user's device.

The app does **not** include ads, advertising SDKs, analytics SDKs, tracking SDKs, crash-reporting SDKs, social login, account creation, location tracking, contact access, microphone access, camera access, SMS access, or payment processing.

The app does **not** sell personal data and does **not** intentionally collect user listening history, music libraries, playlists, search history, or audio files for the developer.

## Data the app uses locally

Elovaire may access or store the following data on the user's device so the app can function:

- Local audio files selected or permitted by the user.
- Android MediaStore audio-library records.
- Metadata read from audio files, such as title, artist, album, duration, track number, disc number, year, genre, embedded artwork, and file format information.
- Library scan results and cached library snapshots.
- Custom music folder selections, including Android Storage Access Framework tree URIs or folder paths where applicable.
- Playlists, favorites, playback queue state, repeat/shuffle state, recent playback state, search history, sort/layout preferences, settings, equalizer settings, lyrics settings, and app language/theme preferences.
- Locally cached artwork, lyrics, metadata, tag-matching results, or temporary files created during safe tag-editing workflows.

This data is used to display the music library, play music, manage playlists, restore app state, improve app responsiveness, and let users edit tags or artwork where supported.

## Permissions

Depending on Android version and feature use, Elovaire may request or declare:

- Audio/media library access so it can find and play local music.
- Selected folder access when the user chooses custom library folders.
- Notification permission so playback controls can appear in Android notifications.
- Foreground media playback service permission so playback can continue reliably.
- Internet access for optional online lyrics, optional online tag matching, album artwork lookup, and GitHub/self-update checks where enabled.
- Modify audio settings permission for audio playback and audio-effect behavior.
- Optional package-install permission in self-distributed builds that support in-app update installation.
- Optional USB host support for device/audio-routing features where available.

The app should continue to operate as a local music player without optional online lookup features.

## Network activity

Elovaire is local-first, but some optional features can make network requests.

### Online lyrics lookup

If online lyrics lookup is enabled and lyrics are requested, the app may contact LRCLIB over HTTPS. Requests may include track metadata needed to find matching lyrics, such as song title, artist name, album name, and duration. The app does not need to upload the audio file itself for lyrics lookup.

Lyrics results may be cached locally so the app can display them again without repeating the same lookup.

### Online tag matching

If the user starts online tag matching from the tag editor, the app may use services such as AcoustID, MusicBrainz, Cover Art Archive, and artwork providers such as Tidal artwork endpoints to find better album metadata and artwork.

Depending on the matching path, the app may send or request:

- Audio fingerprints and track duration for matching.
- Album, artist, title, recording, release, and track identifiers.
- Search queries needed to resolve album metadata.
- Artwork-download requests for candidate album covers.

The app should not upload full local audio files for online tag matching. Tag matching is intended to be user-initiated and used only to suggest metadata before the user applies changes.

### GitHub/self-update checks

In self-distributed builds where the GitHub update flow is enabled, the app may contact GitHub to check release metadata, compare versions, download update APKs, verify downloads, and start Android's installer flow when the user chooses to update.

## What the app does not do

Elovaire does not intentionally:

- Sell user data.
- Use advertising identifiers.
- Include third-party analytics or advertising SDKs.
- Track user behavior across other apps or websites.
- Require an account.
- Upload the user's music library to the developer.
- Upload full audio files for lyrics lookup or online tag matching.
- Access contacts, calendar, SMS, phone call logs, camera, microphone, or location.
- Use cleartext HTTP traffic for app network requests.

## Local tag editing

When the user edits tags or album artwork, Elovaire may read and write supported local audio files. The app may create temporary working files and backups during the edit process so it can apply changes more safely. Unsupported or unsafe file formats should be skipped rather than modified unsafely.

## Data sharing

Elovaire does not intentionally share user data with the developer. Optional third-party services may receive request data only when the user uses features that require them, such as online lyrics, online tag matching, artwork lookup, or update checks.

Those third-party services may process technical request information such as IP address, user agent, timestamps, request URLs, and request parameters according to their own policies.

## Data retention and deletion

Most app data is stored locally on the user's device. Users can remove app data by clearing the app's storage through Android settings or uninstalling the app. Users can also revoke media and notification permissions through Android settings.

Custom folder access granted through Android's folder picker can be revoked through Android system settings or by removing the folder from the app where supported.

## Backups

The app is intended to keep user app data local and should not rely on Android cloud backup for app-private data. Backup behavior may still depend on Android version, device policy, and system configuration.

## Children's privacy

Elovaire is a general-purpose local music player. It does not knowingly collect children's personal data for the developer. Optional online services may receive request data if online lookup features are used.

## Changes to this summary

This summary should be updated whenever the app adds or removes permissions, network services, analytics/crash-reporting SDKs, account features, cloud sync, advertising, payment processing, or other data-handling behavior.
