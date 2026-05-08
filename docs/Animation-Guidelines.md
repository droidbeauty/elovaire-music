# Animation Guidelines

Elovaire animations should use `elovaire.music.app.ui.motion.ElovaireMotion` instead of raw `tween`, `spring`, or easing values in screen code. Shared timings, easings, springs, enter/exit transitions, and common animation specs live there so motion stays consistent across the app.

Use the shared wrappers for common lifecycle animations:

- `ElovaireAnimatedVisibility` for show/hide content.
- `ElovaireAnimatedContent` for state-driven content swaps.
- `ElovaireCrossfade` for simple fade transitions.
- `Modifier.elovaireAnimateContentSize()` for layout size changes.

When several animated values depend on the same state, prefer a single `updateTransition` with a clear label instead of separate independent `animate*AsState` calls. This keeps surfaces such as now playing, queue, lyrics, and chrome from drifting out of sync.

Always add meaningful animation labels, for example `NowPlayingBarVisibility`, `QueueOverlayTransition`, `LyricsOverlayAlpha`, or `PlaybackControlsVisibility`. Labels should describe the UI behavior, not just the property type.

Avoid independent child exit animations inside a parent `AnimatedVisibility` unless they are coordinated through the parent `AnimatedVisibilityScope.transition`. Otherwise child content can be removed before its exit motion finishes.

Keep animation frame work lightweight. Do not animate large bitmaps directly, avoid per-frame image requests or expensive measurement, prefer `graphicsLayer` for alpha/scale/translation, and avoid new infinite animations unless the UI explicitly needs continuous motion.

Before merging animation changes, check light and dark themes, top/bottom chrome, queue open/close, lyrics overlay, and now-playing expand/minimize paths.
