# USB Bit-Perfect Playback

Elovaire uses a layered USB playback capability path instead of assuming a single Android version or a single DAC behavior.

## Routing strategy

The playback manager evaluates the active USB output device and the current track format, then chooses the strongest safe route:

1. `MixerAttributesBitPerfect`
   Android 14+ with `AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT` available for the requested PCM format.
2. `ExactFormatDirect`
   Exact-format USB path with direct playback support and DSP bypass.
3. `BestEffortPreferredDevice`
   Exact-format USB `AudioTrack` probe succeeds and the player is routed to the USB device, but the platform cannot guarantee full bit-perfect behavior.
4. `None`
   Normal playback fallback.

## Internal states

- `NoUsbDevice`
- `UsbDetected`
- `ProbingCapabilities`
- `ExactFormatSupported`
- `BitPerfectAvailable`
- `BitPerfectActive`
- `ExactFormatUsbActive`
- `BestEffortUsbActive`
- `UnsupportedAndroidVersion`
- `UnsupportedDevice`
- `UnsupportedFormat`
- `FallbackNormalPlayback`
- `ErrorRecoverable`

Only `BitPerfectActive` and `ExactFormatUsbActive` bypass app DSP and software gain.

## Capability probing

For each USB device and track format the app:

- maps the decoded track format to a PCM output format
- checks advertised USB sample rates and PCM encodings when present
- probes exact-format `AudioTrack` initialization off the main thread
- uses direct-playback support APIs when the platform provides them
- uses Android 14+ preferred mixer attributes when supported by the selected USB device

Successful probe results are cached per `deviceId + sampleRate + channelMask + encoding` and invalidated on USB route changes.

## DSP bypass rules

When the route is truly bit-perfect or exact-format active, the app bypasses:

- equalizer
- bass boost
- spaciousness processing
- software gain / fine-grained in-app volume

If exact or bit-perfect conditions cannot be met, playback falls back cleanly to normal output without breaking music playback.
