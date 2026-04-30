# Equalizer Quality Notes

## Goal

Keep Elovaire's in-app EQ clean, open, and controlled under both subtle and stronger curves without relying on hard clipping or an always-working limiter.

## Refinements in the current design

- The 24-band stage uses bandwidth-aware peaking filters so adjacent bands feel more like a graphic EQ curve and less like stacked narrow resonances.
- Automatic headroom is based on the estimated combined spectral lift of the whole active curve, including bass, treble, and spaciousness shaping.
- The post-EQ limiter is only a safety net. Its threshold and knee are tuned to stay mostly out of the way during normal listening.
- Flat EQ remains dry-mixed and bit-perfect playback still bypasses the whole DSP path.

## What to listen for

- Female and male vocals should stay forward and intelligible after mild presence or treble boosts.
- Broad bass boosts should sound weightier without turning low mids cloudy.
- Strong but reasonable smile and V-shape curves should stay punchy without obvious crunching.
- Classical and acoustic tracks should keep transient attack and stereo image.
- Rapid live EQ gestures should not click or zipper.

## Debug checks

During development, verify:

- computed headroom stays negative when multiple adjacent boosts are active
- limiter peak reduction remains small for normal curves
- flat EQ reports DSP bypass
- no NaN or Infinity values are produced in tests

## Manual checklist

- Flat
- Gentle smile curve
- Bass boost curve
- Vocal clarity curve
- Treble lift
- Mid cut
- Strong but reasonable V-shape
- Adjacent low-band boost
- Adjacent high-band boost
- All bands +6 dB
- All bands -6 dB
- Alternating boosts and cuts
