# Elovaire Media Compatibility Fixtures

This directory intentionally contains no copyrighted media.

For local emulator validation, place generated or personally owned test files in:

```text
/sdcard/Music/ElovaireCompat/
```

Minimum fixture names used by the compatibility matrix:

```text
mp3-id3v23.mp3
mp3-id3v24.mp3
flac-native.flac
m4a-aac.m4a
m4a-alac.m4a
m4b-aac.m4b
m4b-alac.m4b
aac-adts.aac
ogg-vorbis.ogg
ogg-opus.ogg
ogg-flac.ogg
standalone-opus.opus
wav-pcm.wav
wav-g711-alaw.wav
wav-g711-mulaw.wav
amr.amr
threegp-audio.3gp
mka-opus.mka
mka-vorbis.mka
mka-flac.mka
mp4-video.mp4
mkv-video.mkv
corrupt.mp3
corrupt.flac
wrong-extension.bin
missing-metadata.mp3
large-artwork.mp3
no-artwork.flac
multidisc-disc1-track1.flac
same-album-artist-a.flac
same-album-artist-b.flac
```

After pushing files, trigger indexing with:

```bash
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Music/ElovaireCompat/<file>
```
