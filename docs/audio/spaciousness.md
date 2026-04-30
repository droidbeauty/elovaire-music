# Spaciousness DSP

## Purpose

Elovaire's spaciousness stage is an app-owned stereo enhancement processor that lives in the local PCM playback pipeline. It is designed to change width, depth, ambience, and openness without turning the sound harsh, phasey, or washed out.

## Modes

- `Off`
  Exact bypass.
- `StereoWidth`
  Mid/side widening with the bass region kept mostly centered.
- `CrossfeedDepth`
  Gentle opposite-channel feed with short delay and low-pass shaping for a more speaker-like headphone image.
- `EarlyReflectionRoom`
  Short early reflections only, with no long tail or feedback wash.
- `HaasSpace`
  Conservative micro-delay decorrelation for width and space.
- `HarmonicAir`
  High-frequency side and ambience emphasis for airy openness without a crude global treble lift.

## DSP approach

- Internal processing stays in float.
- All modes are stereo-frame aware and reset their delay/filter state safely on seek, track change, stop, and sample-rate change.
- Delay-based modes use reusable circular buffers with no allocations in the render loop.
- Parameter changes are smoothed with a short time constant.

## Amount mapping

- Public control is `amountNormalized` from `0.0 .. 1.0`.
- The DSP uses a curved mapping so low values stay subtle and high values remain controlled.
- `0.0` is exact bypass.

## Mono compatibility

- `StereoWidth` keeps low bass mostly centered.
- `CrossfeedDepth` is intentionally subtle and does not collapse the image.
- `HaasSpace` and `HarmonicAir` only decorrelate higher-frequency content.
- Mono input is bypassed instead of creating unstable fake stereo.

## Headroom strategy

- Spaciousness adds automatic gain compensation based on the active mode and amount.
- The EQ processor's transparent safety limiter still sits later in the chain, but the spaciousness stage aims to avoid pushing it unnecessarily.

## Playback chain

1. Decode PCM
2. ReplayGain / normalization if active
3. EQ preamp and headroom
4. 24-band EQ
5. Bass boost
6. Spaciousness
7. Transparent safety limiter
8. Output conversion
9. Audio sink

## Bit-perfect interaction

- Spaciousness is bypassed completely during bit-perfect playback.
- Delay lines and filter state are reset when bit-perfect bypass engages.

## Known limitations

- Multichannel content currently bypasses spaciousness instead of attempting unsafe front-channel-only processing.
- Mono expansion is intentionally not attempted in this version.

## Manual listening checklist

- Centered vocal track
- Acoustic guitar or piano
- Jazz cymbals
- Dense rock or metal
- Bass-heavy electronic track
- Orchestral recording
- Very wide stereo recording
- Old hard-panned stereo recording
- Mono recording

Listen for:

- distortion
- clipping
- harshness
- hollow or phasey mids
- unstable center vocals
- weakened bass
- excessive reverb wash
- metallic reflections
- mono collapse
- limiter pumping
- clicks or pops during live changes
