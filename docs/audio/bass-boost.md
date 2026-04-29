# Bass boost

Elovaire uses a deterministic low-end shaping model instead of Android's stock `BassBoost` effect. The current implementation projects a modeled low-shelf response into the app's session `Equalizer`, which keeps behavior consistent, testable, and easy to tune while still bypassing cleanly for bit-perfect USB playback.

## DSP approach

- Bass amount is stored as a normalized value from `0.0` to `1.0`.
- The normalized amount is mapped with a nonlinear curve to a conservative maximum boost.
- A low-shelf biquad model provides the target low-frequency response.
- A light mud-trim dip centered around the low mids keeps the maximum setting from sounding boomy.
- A proportional pregain is applied across the projected response to preserve headroom and reduce clipping risk.

## Tuning

- Maximum boost: `7.5 dB`
- Shelf corner frequency: `96 Hz`
- Shelf slope: `0.85`
- Mud-trim center: `220 Hz`
- Maximum mud trim: `1.4 dB`
- Pregain ratio: `56%` of the current bass boost
- Smoothing time: `140 ms`

All of these values live in `BassBoostConfig` inside `/Users/dave/Desktop/Droid Beauty/Elovaire Music/app/src/main/java/com/elovaire/music/data/playback/BassBoostProcessor.kt`.

## Processing order

Within the current playback architecture the ordering is:

1. Media decode
2. Media3/Android audio session creation
3. User EQ bands
4. Bass boost projection
5. Spaciousness and treble shaping
6. Android output sink

This is not a raw PCM in-app audio processor yet. Instead, the app computes a repeatable target response and applies it to the local playback session equalizer. That keeps the feature local to Elovaire playback and avoids relying on OEM-specific `BassBoost` behavior.

## Bit-perfect interaction

- Bass boost is bypassed entirely whenever bit-perfect USB playback is active.
- When bypass is active the controller releases its audio effects instead of leaving them enabled at zero.

## Headroom strategy

- The shelf boost is never applied without compensation.
- Pregain scales with the selected boost amount.
- Mid and high frequencies stay close to flat except for the deliberate headroom reduction and optional spaciousness/treble shaping.
- The goal is stronger low end without obvious clipping, mud, or sudden tonal jumps.

## Known limitations

- The current design uses the Android session `Equalizer` as the final sink-facing stage, so exact OEM rendering can still vary slightly.
- Multichannel bass management is not explicitly implemented; the app follows the platform equalizer session behavior.
- There is no dedicated limiter stage yet. Headroom is handled with pregain and conservative maximum boost instead.

## Manual validation

Use these checks after tuning changes:

- acoustic bass material
- kick-heavy electronic tracks
- dense rock or metal
- quiet jazz or classical
- already bass-heavy modern pop
- low-volume and high-volume listening
- headphones, USB-C dongles, and speakers
- quick jumps between `0%` and `100%`
- slow slider sweeps
- track changes across `44.1 kHz`, `48 kHz`, `96 kHz`, and `192 kHz`

Listen for:

- clipping
- crackling
- pumping
- muddy low mids
- sudden tonal jumps
- clicks or pops during adjustment
- channel imbalance
