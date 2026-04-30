# Equalizer DSP

Elovaire uses an app-owned PCM DSP stage for equalization instead of relying on Android's device-dependent session equalizer.

## Design

- Core stage: 24-band graphic/parametric-style EQ built from a cascade of stable biquad peaking filters.
- Band frequencies: 20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1k, 1.25k, 1.6k, 2k, 2.5k, 3.15k, 4k.
- Gain range: normalized UI values map to `-12 dB .. +12 dB`.
- Internal processing: float-domain sample math, with output written back in the playback format.
- Filter voicing: adjacent bands use a bandwidth-aware peaking design instead of a single narrow Q everywhere, which keeps broad curves smoother and reduces the hollow/phasey character that can happen when neighboring boosts stack too aggressively.
- Macro controls:
  - `Bass`: low-shelf lift with pregain compensation from the bass boost model.
  - `Treble`: high-shelf shaping.
  - `Spaciousness`: gentle low-mid trim plus presence/air shaping.

## Smoothing

- Band and macro targets are smoothed in the processor.
- Coefficients are updated in short frame blocks to avoid zipper noise without allocating on the audio thread.

## Headroom

- The processor estimates the peak positive spectral lift of the whole EQ curve, then applies automatic headroom before the filter cascade.
- Bass pregain compensation is applied before the EQ stages.
- A light safety limiter remains after the EQ, but it is intentionally the fallback rather than the main protection path.

## Bypass rules

- Flat EQ is mixed fully dry.
- Bit-perfect USB playback bypasses the entire DSP path.
- Unsupported or Nyquist-unsafe bands are disabled automatically for the current sample rate.

## DSP order

1. Decode PCM
2. EQ preamp and automatic headroom
3. 24-band EQ
4. Bass shelf
5. Treble and spaciousness shaping
6. Safety limiting
7. Output conversion

## Quality checks

- Flat EQ remains mathematically dry-mixed so it matches bypass within floating-point tolerance.
- Headroom grows when adjacent positive boosts stack, instead of only reacting to the single loudest band.
- Nyquist-unsafe bands are disabled automatically after sample-rate changes.
- Debug-only diagnostics expose sample rate, computed headroom, active filter count, limiter peak reduction, limiter hit count, and whether DSP is bypassed.

## Manual listening checklist

- Sweep a kick-heavy electronic track from flat to boosted bass.
- Check a dense rock mix for low-mid muddiness after broad positive boosts.
- Verify quiet acoustic material stays clean when treble and spaciousness change slowly.
- Verify no clicks or pops on track changes, seeks, and rapid EQ gestures.
- Verify bit-perfect USB mode stays unaffected when enabled.
