# Elovaire Privacy Policy

**Effective date:** July 22, 2026  
**Last updated:** July 22, 2026

This Privacy Policy explains how Elovaire (package name
`elovaire.music.droidbeauty.app`) accesses and handles information. Elovaire is
provided by **Droid Beauty** ("Droid Beauty", "we", "us", or "our").

Elovaire is an offline-first music player. It does not require an account and
does not include advertising, analytics, tracking SDKs, or an app-operated
cloud service. Most information used by Elovaire stays on your device. Some
optional features make direct requests to third-party music, lyrics, and image
services as described below.

## Information handled on your device

Elovaire may access or create the following information locally to provide its
features:

- **Music library information:** audio files and the folders or content
  locations you select, file names, content URIs, titles, artists, albums,
  genres, durations, technical audio details, embedded tags, artwork, and
  embedded or sidecar lyrics.
- **Library and playback data:** the indexed library, playlists, favorites,
  smart-playlist definitions, queue and playback position, playback history,
  play counts, recent items, and search history.
- **Settings:** theme, language, layout, audio and equalizer settings, online
  lyrics preference, and other app preferences.
- **Editing data:** metadata, artwork, and lyrics that you choose to write to a
  supported audio file. Elovaire performs these edits locally and asks Android
  for write access when required.
- **Audio fingerprints:** when you explicitly request online tag matching,
  Elovaire can calculate a Chromaprint fingerprint locally. It does not upload
  the complete audio file.
- **Device and playback capabilities:** Android version, available audio
  routes, decoder capabilities, and USB audio capabilities needed to configure
  playback. These details are processed locally.
- **Local diagnostics:** a bounded history of Android process-exit reasons and
  technical operation diagnostics used for recovery and troubleshooting.
  Elovaire does not upload these diagnostics.

This information is stored in app-private files, preferences, and a local
database. Elovaire does not send your playlists, favorites, playback history,
search history, settings, local file paths, complete audio files, saved lyrics,
or local database identifiers to Droid Beauty.

## Optional network features

Elovaire connects only over HTTPS. A network request necessarily discloses the
device's public IP address, request time, and basic connection information to
the destination service. Requests also include an Elovaire user-agent that can
contain the app version and Android platform. Droid Beauty does not receive or
separately store this network information because Elovaire has no app-operated
backend.

The app can contact the following independent services:

| Feature | Information sent | Recipient |
| --- | --- | --- |
| Online lyrics, disabled by default | Song title, artist, album, and duration, or a subset needed for a match | [LRCLIB](https://lrclib.net/) and [Lyrics.ovh](https://lyrics.ovh/) |
| Artist images | Artist name or MusicBrainz identifier | [MusicBrainz](https://musicbrainz.org/), [TheAudioDB](https://www.theaudiodb.com/), [fanart.tv](https://fanart.tv/), and, when configured, the [YouTube Data API](https://developers.google.com/youtube/v3) |
| User-requested metadata matching | Locally generated audio fingerprint and duration, or title, artist, album, release identifiers, and related metadata | [AcoustID](https://acoustid.org/), [MusicBrainz](https://musicbrainz.org/), and the [Cover Art Archive](https://coverartarchive.org/) |
| Artwork download | The requested image URL or release/artist identifier | The provider above and, for Cover Art Archive images, the [Internet Archive](https://archive.org/) delivery host |
| About screen developer logo | A request for the Droid Beauty logo when the About screen is opened | [GitHub-hosted content](https://github.com/) |

Online lyrics requests occur only when you enable online lyrics lookup. Online
metadata matching occurs only when you request matching. Artist-image requests
may occur when you open an artist screen that needs remote artwork. Responses,
lyrics, fingerprints, and images may be cached locally to improve performance
and reduce repeat requests.

These providers are independent controllers of information they receive. Their
retention and use practices are governed by their own terms and privacy
policies. Relevant policies include:

- [MetaBrainz privacy policy](https://metabrainz.org/privacy) for MusicBrainz
  and related MetaBrainz services
- [Internet Archive privacy policy](https://archive.org/about/terms.php)
- [TheAudioDB privacy policy](https://www.theaudiodb.com/docs_privacy_policy.php)
- [fanart.tv privacy policy](https://fanart.tv/privacy-policy/)
- [Google privacy policy](https://policies.google.com/privacy) for the YouTube
  Data API and Android services
- [GitHub privacy statement](https://docs.github.com/en/site-policy/privacy-policies/github-general-privacy-statement)

LRCLIB, Lyrics.ovh, and AcoustID may not provide a dedicated privacy policy at
a stable URL. Review their linked sites and terms before enabling or using the
related feature if this concerns you.

## External links

The About and Privacy screens may contain links to websites such as the Droid
Beauty social profiles, Google Play, Ko-fi, Android Developers, Lucide, and the
project repository. Elovaire opens these links in your browser or another app.
After you choose a link, the destination and your browser handle information
under their own privacy policies. Elovaire does not use GitHub to check for,
download, or install app updates.

## Permissions and storage access

Elovaire requests only access needed for its music features:

- media or legacy storage read access, depending on Android version, to find
  and play audio;
- Storage Access Framework access to folders you explicitly select;
- file-specific write approval when Android requires it for a metadata or
  lyrics edit;
- Internet access for optional online features;
- audio-setting and media-playback foreground-service access for playback; and
- optional USB host access for compatible USB audio devices.

Elovaire does not request all-files access, photo/video-library access,
location, contacts, phone, advertising ID, unknown-app installation, or package
installation permission.

## Backups

If Android backup or device transfer is enabled, Android may back up a limited
set of portable Elovaire settings, such as theme, language, layout, playback,
equalizer, and online lyrics preferences. Google or the device manufacturer
controls that backup service under its own privacy terms.

Elovaire excludes music files, playlists and other user collections, playback
runtime state, search and playback history, media identifiers, folder grants,
caches, diagnostics, temporary files, and mutation journals from its Android
backup rules.

## Retention and deletion

- Local library data, collections, history, settings, and diagnostics remain
  until you delete them through an available app control, clear Elovaire's app
  data in Android settings, or uninstall the app.
- Cached online results and images remain until they expire, are evicted,
  become invalid after a media revision, are cleared by Android, or app data is
  cleared. The app bounds its caches to avoid indefinite growth.
- Clearing app data or uninstalling removes Elovaire's private data. It does not
  delete your music files. Metadata, artwork, or lyrics that you intentionally
  wrote into a music file remain in that file.
- Removing a selected folder from Elovaire removes it from the scan list but
  does not delete the folder or its audio files.
- Third-party providers determine retention of requests they receive. Contact
  the relevant provider to exercise rights concerning information held by that
  provider.

## Sharing and sale

Droid Beauty does not sell personal information. Elovaire does not share data
with advertisers or analytics companies. Information is transmitted only as
described for optional provider requests, Android backup when enabled by the
device, or when you deliberately open an external link.

## Security

Elovaire uses Android's app-private storage, scoped media and document access,
HTTPS-only network requests, bounded network responses, and storage-specific
write approval. No method of storage or transmission is completely secure, so
absolute security cannot be guaranteed.

## International processing

Third-party providers may operate or process requests in countries other than
your own. Their privacy policies explain their locations, legal bases, and
international-transfer safeguards. Droid Beauty does not operate a server that
receives Elovaire user data.

## Your choices and rights

You can:

- leave online lyrics lookup disabled or turn it off in Elovaire;
- avoid requesting online metadata matching;
- revoke media or selected-folder access in Android settings;
- clear search history and other data using available app controls;
- clear all private app data or uninstall Elovaire; and
- contact Droid Beauty regarding access, correction, deletion, objection, or
  other rights available under applicable privacy law.

Because Droid Beauty does not receive app data on a server, we generally cannot
identify or retrieve information stored only on your device. Requests sent to
independent providers must be addressed to those providers.

## Children's privacy

Elovaire does not require an account, display targeted advertising, or
knowingly solicit personal information from children. If you believe a child
has provided personal information through a third-party service reached from
Elovaire, contact that service and notify Droid Beauty.

## Changes to this policy

This policy may be updated when Elovaire's data practices or legal obligations
change. The current version will remain available at this repository location,
and the effective date above will be updated for material changes.

## Contact

**Developer and data controller:** Droid Beauty  
**Privacy contact:** [droidbeautycontact@gmail.com](mailto:droidbeautycontact@gmail.com)  
**Project repository:** [github.com/droidbeauty/elovaire-music](https://github.com/droidbeauty/elovaire-music)

Please do not include music files, lyrics, local file paths, or other sensitive
content in a public issue.
