# Playback progress architecture

Elovaire uses a single progress owner in the playback layer.

## Rules

- `PlaybackManager` owns the only active progress update loop for a player session.
- `PlaybackProgressController` is the single source of truth for:
  - current position
  - display position
  - duration
  - buffered position
  - scrub state
  - active media id
  - progress generation
- UI renders from `PlaybackProgressState`.
- UI does not run its own independent progress timer.
- Scrubbing never seeks on every pointer move.
- While scrubbing:
  - player polling keeps updating the actual position internally
  - the displayed thumb position comes from the scrub preview
  - the player only receives one final `seekTo(...)` on scrub release
- On track changes:
  - scrub state is cleared
  - pending seek state is discarded
  - generation increments
  - stale updates from the previous media item are ignored

## Lifecycle

- `PlaybackManager` starts one progress job during construction.
- That job is cancelled in `release()`.
- The player listener is added once and removed once.
- UI progress collection is scoped to the components that actually need it, rather than the whole app root.

## Why this avoids jank

- The UI no longer fights player polling while the user drags.
- A track transition cannot reuse a stale scrub preview from the old song.
- Root-level recomposition from progress ticks is avoided for the compact dock.
- Progress state updates are centralized and testable.
