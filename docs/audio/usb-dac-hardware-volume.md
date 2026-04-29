# USB DAC Hardware Volume

## System Volume vs DAC Volume

- Android system media volume (`STREAM_MUSIC`) controls the phone or tablet media stream.
- USB DAC hardware volume controls the DAC's own USB Audio Class feature-unit volume endpoint.
- They are intentionally treated as separate layers.
- The hardware-volume path never uses `AudioManager.setStreamVolume(...)`, `adjustStreamVolume(...)`, or `adjustVolume(...)`.

## Supported Implementation Path

- The app first detects whether the active external USB output has a matching USB host audio device.
- If permission is available, it opens the device only long enough to inspect raw USB descriptors and issue Audio Class control transfers on the control pipe.
- The implementation looks for USB Audio Class feature-unit volume controls:
  - UAC1 feature units
  - UAC2 feature units where the control bitmap exposes readable or writable volume controls
- Master volume is preferred when available.
- If only per-channel controls exist, the same requested value is written to all exposed output channels.
- If no writable feature-unit volume control is found, the app falls back to the existing Android/system-volume behavior.

## Known Android and API Limitations

- Android does not expose a universal public API for independent per-device USB DAC hardware volume that leaves `STREAM_MUSIC` untouched, so this implementation uses USB host Audio Class control requests when the DAC exposes them.
- USB permission is requested only when needed, typically when the user actually tries to adjust DAC volume through the existing app volume surface.
- Some DACs expose playback through Android's routed audio path but do not expose a writable USB Audio Class feature unit, or reject control requests. Those devices are treated as unsupported.
- Some devices provide malformed descriptors or read-only controls. Those are detected and ignored safely.

## USB Audio Class Notes

- The implementation uses the control endpoint only and does not claim streaming interfaces, which helps avoid disrupting Android's playback routing.
- Volume range reads use standard `GET_MIN`, `GET_MAX`, `GET_RES`, and `GET_CUR` style feature-unit control requests.
- Volume writes use `SET_CUR` on the detected feature unit and interface.
- If the DAC exposes only read-only or unusable controls, hardware volume is marked unsupported or unavailable and the app returns to the normal path.

## Safety Behavior

- The app never forces a DAC to maximum volume.
- Hardware volume requests are clamped to the DAC's reported min/max range.
- Requests are snapped to the DAC's reported hardware step resolution.
- Stored per-device values are only keyed from stable USB identity fields and are not blindly auto-applied when the current DAC volume cannot be read.
- Bit-perfect playback stays software-gain-free. DAC hardware volume is allowed because it changes device-side hardware volume rather than PCM gain.

## Debug Verification

- Use a debug build and watch logcat for `UsbDacVolume`.
- Useful log fields include:
  - USB identity key
  - Android API level
  - support or unsupported status
  - min/max/step
  - master vs per-channel control
  - current DAC volume
  - requested and applied DAC volume
  - system media volume before and after a DAC hardware-volume write
- Hardware-volume success criteria:
  - the DAC volume changes
  - `STREAM_MUSIC` stays exactly the same before and after the write

## Files Changed

- `app/src/main/java/com/elovaire/music/data/playback/UsbDacHardwareVolumeModels.kt`
- `app/src/main/java/com/elovaire/music/data/playback/UsbAudioClassVolumeParser.kt`
- `app/src/main/java/com/elovaire/music/data/playback/UsbDacHardwareVolumeManager.kt`
- `app/src/main/java/com/elovaire/music/data/playback/PlaybackManager.kt`
- `app/src/main/AndroidManifest.xml`
- `app/src/test/java/elovaire/music/app/data/playback/UsbDacHardwareVolumeMathTest.kt`
- `app/src/test/java/elovaire/music/app/data/playback/UsbAudioClassVolumeParserTest.kt`
- `app/src/test/java/elovaire/music/app/data/playback/UsbDacHardwareVolumeImplementationTest.kt`
- `app/src/androidTest/java/elovaire/music/app/data/playback/UsbDacHardwareVolumeManagerInstrumentationTest.kt`

## Manual Test Steps

1. Connect a USB DAC that exposes hardware volume through USB Audio Class feature units.
2. Start playback with Android system media volume set to a known value such as 40%.
3. Move the app's existing volume slider.
4. Confirm the DAC output level changes without Android's `STREAM_MUSIC` value changing.
5. Check debug logs for `UsbDacVolume` and verify the logged system volume before and after the hardware write are identical.
6. Repeat with a DAC that does not expose writable hardware volume and confirm the app falls back to normal behavior without crashes.
7. Unplug the DAC during playback and confirm playback and volume behavior recover cleanly.
