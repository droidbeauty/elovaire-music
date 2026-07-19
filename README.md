# Elovaire

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/1.png"
    alt="Elovaire - elegant music listening experience"
    width="100%"
  />
</p>

---

<p align="center">
  <a href="https://ko-fi.com/droidbeauty">
    <img
      alt="Support Elovaire on Ko-fi"
      src="https://img.shields.io/badge/Support%20on%20Ko--fi-ff5f5f?style=for-the-badge&logo=kofi&logoColor=white"
    />
  </a>
</p>

<p align="center">
  <b>Your local music library, presented like art that it is</b>
</p>

<p align="center">
  Elovaire is Android native music player for people who still love owning their music — minimalistic UI focused around album artwork, customizable playlists and refined audio controls. All designed so you can take full advantage of listening offline
</p>

---

## About

Elovaire turns listening to music into elegant experience - as it should be

It gives your albums, artists and favorite songs an organized place. Intuitive UI feels calm, delivering classy visuals. Artwork is treated as essential part of the experience, the now-playing screen feels spacious so you can listen without interruptions. No ads, no random notifications. Your music under your control

Browse by songs, albums, artists, genres and playlists. Instantly search even through large collection of music, open lyrics when you feel like a superstar, and shape playback with built-in audio controls

#### Efficient architecture supports all features across devices running Android 11 through 17

## Highlights

- Offline-first playback for music stored directly on your Android device
- Beautiful artwork-led library
- Full player and compact mini-player for quick control while browsing
- Lyrics support with local and online lookup paths
- 18-band equalizer with built-in presets and additional features
- Smooth Compose UI with frosted blur, animated transitions and adaptive visual details
- Fast library scanning through Android MediaStore and local folder observation
---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/2.png"
    width="49%"
  />
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/3.png"
    width="49%"
  />
</p>

## Features

### Library and browsing

- Browse your local collection by songs, albums, artists, genres and playlists
- Artwork-rich album and playlist views
- Fast search with recent history and expandable song results
- Favorites, play counts and recent playback awareness
- Folder-aware library scanning with auto refresh handling

### Playback

- Full now-playing screen with queue, lyrics overlay, volume control, repeat, shuffle and playback actions
- Compact now-playing bar for quick access from the rest of the app
- Playback recovery handling for unexpected idle/player states
- USB DAC and direct-output awareness for cleaner output paths where supported by the device

Supported audio format handling is based on Android Media3/ExoPlayer, Android platform decoders and Elovaire's own container validation. Bit depth applies to lossless or PCM sources; lossy codecs do not expose a meaningful playback bit depth. Bitrate support is stream and device dependent unless the codec defines a fixed mode range.

| Format | Extensions | Supported codec/container handling | Bit depth support | Bitrate support |
| --- | --- | --- | --- | --- |
| MP3 | `.mp3` | MPEG audio playback through platform decoder support | Not applicable for lossy MP3 | CBR/VBR MP3 streams, commonly up to 320 kbps |
| AAC / ADTS | `.aac` | Raw AAC/ADTS audio where supported by the device | Not applicable for lossy AAC | Codec/profile dependent AAC bitrates |
| MP4 audio | `.m4a`, `.mp4`, `.m4b` | Audio-only MP4-family containers, including AAC and supported ALAC where the device decoder is available | AAC: not applicable; ALAC: source lossless bit depth where supported by the device | AAC lossy bitrate or ALAC lossless stream bitrate, metadata/device dependent |
| FLAC | `.flac` | Native FLAC playback | Lossless source bit depth, typically 16-bit or 24-bit when accepted by the platform decoder | Lossless stream bitrate, metadata dependent |
| WAV | `.wav` | WAV/PCM-style audio accepted by the platform decoder | PCM source bit depth where supported by the device | Uncompressed PCM rate based on sample rate, bit depth and channels |
| Ogg Vorbis | `.ogg`, `.oga` | Ogg container with Vorbis audio | Not applicable for lossy Vorbis | Vorbis CBR/VBR stream bitrate |
| Ogg Opus | `.ogg`, `.oga` | Ogg container with Opus audio | Not applicable for lossy Opus | Opus stream bitrate, codec/device dependent |
| Ogg FLAC | `.ogg`, `.oga` | Ogg container with FLAC audio where validated and supported | Lossless source bit depth where supported by the device | Lossless stream bitrate, metadata dependent |
| Opus | `.opus` | Standalone Opus audio where supported by the platform decoder | Not applicable for lossy Opus | Opus stream bitrate, codec/device dependent |
| AMR | `.amr` | AMR audio for supported voice recordings | Not applicable for AMR speech codec | AMR-NB/WB fixed mode bitrates, device dependent |
| 3GP audio | `.3gp` | Audio-only 3GP containers; video-containing files are excluded | Codec dependent; usually not applicable for AMR/AAC | Contained audio codec bitrate, metadata/device dependent |
| Matroska audio | `.mka` | Audio-only Matroska containers; video-containing files are excluded | Codec dependent, for example FLAC source bit depth when supported | Contained audio codec bitrate, metadata/device dependent |

### Audio controls

- 18-band equalizer with visual editing
- Preset support and manual shaping
- Bass and treble controls
- Spaciousness modes for a wider stereo presentation
- Adjustable reverb effect

### Interface

- Light, dark and system theme modes
- Adjustable text-size
- Frosted glass effects and backdrop blur surfaces
- Smooth route transitions, animated player surfaces and artwork-driven visual accents

---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/4.png"
    width="100%"
  />
</p>

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/5.png"
    width="100%"
  />
</p>

---

## Built With

Elovaire is built for Android based on optimized architecture

- Kotlin
- Jetpack Compose
- Compose Navigation
- Android Media3 / ExoPlayer
- Android MediaStore
- Android Storage Access Framework
- Haze for frosted glass and backdrop blur surfaces
- Gradle Kotlin DSL

---

<p align="center">
  <img
    src="https://raw.githubusercontent.com/droidbeauty/elovaire-music/refs/heads/main/renders/6.png"
    width="100%"
  />
</p>

---

## Support

Elovaire is a personal project made in pursuit of a beautiful, focused alternative to streaming-first music apps. Support is optional, but always appreciated.

<p align="center">
  <a href="https://ko-fi.com/droidbeauty">
    <img
      alt="Support Elovaire on Ko-fi"
      src="https://img.shields.io/badge/Leave%20a%20tip-Ko--fi-ff5f5f?style=for-the-badge&logo=kofi&logoColor=white"
    />
  </a>
</p>
