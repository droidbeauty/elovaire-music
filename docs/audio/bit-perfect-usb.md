# Bit-Perfect USB Playback

## Supported Android Versions

- Primary implementation: Android 14 / API 34 and newer.
- Older Android versions fall back to the app's normal playback path without trying to force USB bit-perfect routing.

## Automatic Activation

- The playback layer listens for output-device changes through `AudioManager.registerAudioDeviceCallback`.
- When a USB audio sink is connected, the app silently checks whether the current track can be mapped to a matching PCM output format and whether the DAC advertises a matching bit-perfect mixer profile.
- If both checks succeed, the player prefers that USB device and requests Android's bit-perfect mixer behavior automatically.
- No setting, prompt, badge, toast, banner, or status indicator is shown to the user.

## Why Android 14+ Mixer Attributes Are Used

- The implementation uses Android's official mixer-attributes path instead of a custom USB host driver.
- On API 34+, `AudioManager.setPreferredMixerAttributes(...)` lets the app request `AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT` for a specific USB output device and playback `AudioAttributes`.
- This keeps routing inside the platform audio stack, which is safer for compatibility with normal playback, audio focus, notifications, and other Android audio routing behavior.
- Direct USB host audio is intentionally not used here because the app does not already ship a dedicated raw USB audio engine, and the platform path is the supported first choice on Android 14+.

## Known Limitations

- Bit-perfect activation depends on both Android and the connected DAC exposing a matching API 34 mixer profile for the exact sample rate, channel mask, and PCM encoding requested by the track.
- If the track format cannot be mapped cleanly, if the DAC does not advertise a matching format, or if the platform rejects the request, playback falls back to the normal path.
- The app only marks bit-perfect playback as active after the mixer request succeeds and the initialized `AudioTrack` matches the requested output format.
- Gapless transitions remain intact when adjacent tracks share the same output format. Format changes between tracks may require the output path to be recreated by the player.

## Verifying Behavior

- Use a debug build and watch logcat for the stable tag `BitPerfectUsb`.
- Useful log fields include:
  - USB device name/id
  - Android API level
  - requested sample rate
  - requested PCM encoding
  - requested channel mask
  - whether bit-perfect mixer behavior was requested
  - whether the request succeeded
  - the fallback reason when it did not
- For hardware validation, use a USB DAC with a front-panel sample-rate display and confirm that:
  - a 44.1 kHz track requests 44.1 kHz output
  - a 96 kHz track requests 96 kHz output when the DAC advertises that format
  - EQ/software volume changes do not alter the digital stream while bit-perfect mode is active
