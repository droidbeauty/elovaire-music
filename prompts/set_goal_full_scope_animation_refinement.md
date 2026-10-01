set_goal

Refine animation completeness, motion quality, and runtime efficiency across the Elovaire Music Android app. Inspect the current repository and actual running app first, then implement and verify the smallest coherent set of changes that gives every screen and meaningful interaction appropriate motion while preserving existing behavior.

## Outcome

Bring motion coverage up to a complete, intentional standard across every route, screen, overlay, sheet, dialog, menu, player state, loading or empty state, and meaningful user interaction. Add animations where the interface currently changes abruptly and motion is appropriate. Refine the timing, easing, spring response, continuity, and rendering efficiency of existing animations wherever the result can be improved.

The visual direction is a more floaty, fluid, and composed feel throughout the app. Existing animations should retain their fundamental logic: preserve what moves, the direction and meaning of movement, the interaction outcome, navigation semantics, and state transitions. Improve the feel through restrained timing and motion-curve refinement rather than redesigning behavior. New animations must feel like they belong beside the strongest existing animations in visual quality and performance.

## 1. Inspect the current project and establish constraints

- Read the applicable `AGENTS.md` files and follow them. Inspect the current branch, working tree, and project structure before editing. Preserve the user's existing changes; do not reset, stash, overwrite, or reformat unrelated work.
- Verify the current Android build configuration and Compose/AndroidX versions from project files. Identify the existing motion system, tokens, hosts, transitions, interaction modifiers, reduced-motion handling, tests, and any motion inventory already in the repository. Search for existing symbols before relying on their names or locations.
- Treat the live implementation as the source of truth. Existing test-only surface catalogs and UI tests are useful starting points, not proof that every current screen or interaction is covered.
- Before implementing Compose motion changes, consult current official Android documentation relevant to the APIs and versions in this project. Start with [Compose animation customization](https://developer.android.com/develop/ui/compose/animation/customize), [animation quick guide](https://developer.android.com/develop/ui/compose/animation/quick-guide), [animation testing](https://developer.android.com/develop/ui/compose/animation/testing), [Compose performance phases](https://developer.android.com/develop/ui/compose/performance/phases), and [Compose performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices). Confirm API details against the project's actual dependency versions. Use official Android/AndroidX sources for any additional Android behavior. Do not upgrade SDKs, Gradle, Kotlin, AGP, Compose, or dependencies unless a verified need requires it.
- Do not create audit reports, inventories as standalone documents, summaries, changelogs, or other documentation. Keep any coverage map in working memory or test code; this prompt is the only requested artifact.

## 2. Build a complete motion and interaction map

Inspect navigation declarations, screen composables, shared components, overlays, state holders, and interaction handlers. Compare that source inventory with the app's current test/catalog inventory and running UI. Cover every reachable user-facing surface, including conditional and compact layouts and important states. For each surface, inspect its meaningful actions and state changes: entry and exit, selection, expansion and collapse, opening and dismissing, toggles, playback actions, seeking, list changes, loading completion, errors, empty states, and other visible feedback.

For each surface and action, decide whether it needs an entrance/exit transition, a state transition, brief interaction feedback, animated content replacement, or intentional immediate behavior. Make missing motion explicit in focused coverage: every surface/action should have an appropriate motion behavior or a clear reason to remain immediate. Reuse and extend existing test-only inventories (including any route/surface catalog already present) rather than inventing a competing catalog. Do not add a new general-purpose animation framework.

## 3. Preserve interaction feel, accessibility, and motion intent

- Preserve the existing interaction's meaning, navigation destination, layout, hit targets, focus behavior, semantics, and content. Do not add motion that delays a response, obscures status, makes a control harder to use, or changes playback or data behavior.
- Direct manipulation must stay tightly synchronized with the user's finger or input. Do not add tween lag to dragging, scrubbing, sliders, scrolling, text entry, or gesture tracking. Animate the release or settled result only when appropriate and consistent with current behavior.
- Respect the system animator scale and any app reduced-motion setting on every new and refined path. At zero scale, avoid introducing visible animated movement; preserve immediate state changes and essential feedback. Keep accessibility semantics and test behavior valid with motion disabled.
- Avoid animating system-owned transitions that the app cannot control. Avoid redundant parent-and-child transitions, competing animations on the same property, and entrance/exit effects that replay on routine recomposition.

## 4. Refine the motion language

First identify the best existing animation examples and the app's shared tokens/specifications. Keep its established motion architecture canonical. Fill gaps with existing hosts, modifiers, and specs where suitable; add or adjust shared tokens only when that reduces inconsistency. Avoid scattered hard-coded durations/easings and local one-off animation systems.

Refine toward a floatier feel with measured deceleration, gentle spring settling, and coherent continuity where the interaction benefits from it. Keep the response clear and controlled: do not make every animation slower, bouncy, overshooting, or decorative. Preserve relative timing hierarchy for micro-feedback, content changes, sheets, player surfaces, and navigation. Retain sensible interruption and reversal behavior when users act rapidly; animations should track the latest state without snapping to stale intermediate values. Do not change the fundamental logic of an existing animation just to make it more expressive.

## 5. Implement in dependency order

Use findings from the actual app to adjust this order where needed, while keeping shared foundations ahead of dependent surfaces:

1. Preserve and correct shared motion primitives, tokens, interruption behavior, and system/reduced-motion handling.
2. Complete app-shell and route transitions, including back navigation and rapid route changes.
3. Refine shared player and other app-wide components whose motion appears across multiple screens.
4. Cover modal surfaces, sheets, dialogs, menus, artwork/detail expansions, and other transient UI.
5. Add or refine per-screen state changes and interaction feedback across the full screen inventory.
6. Cover list and grid item insertion, removal, reordering, selection, and asynchronous content changes without introducing unstable identity or animating excessive off-screen content.
7. Revisit cross-surface journeys to ensure motion composes cleanly rather than stacking or conflicting.

Keep changes focused and project-consistent. Do not redesign screens, change unrelated features, or add dependencies for animation convenience.

## 6. Keep motion efficient

- Prefer Compose-native animation APIs and the existing project patterns. Keep expensive work out of composables and animation-frame callbacks. Avoid per-frame allocations, repeated state reads that trigger broad recomposition, and unnecessary animation of large collections.
- Use the appropriate Compose phase for visual-only properties when it reduces recomposition and preserves correctness. Keep state and semantics updates correct; do not move state reads between phases mechanically.
- Use stable lazy-list keys and existing item identity. Animate only visible or meaningfully affected content. Check that animation does not trigger avoidable image decoding, artwork reloads, media work, disk/network access, or full-list recomposition.
- Keep animation specs stable and shared where appropriate. Check rapid repeated input, interruption, cancellation, lifecycle changes, and navigation away during an animation for stale state or leaked work.
- Validate visual smoothness on a real device where available, using current project profiling/benchmark tooling if already set up. Avoid unsupported performance claims. Do not install a new profiling or benchmark framework unless an important requirement cannot be verified with existing tools.

## 7. Verify coverage and behavior

Use the narrowest verification supported by the repository, then broaden it for shared motion changes. Add or refine focused tests only within the project's existing test setup and conventions.

- Add/extend deterministic Compose animation tests using the existing test clock or equivalent project fixture. Cover key entrance/exit and state transitions, rapid interruption/reversal, stable end states, and zero animator scale or reduced-motion behavior.
- Ensure the motion inventory/test coverage exercises every current reachable screen and meaningful interaction identified in step 2, with intentional-immediate cases documented in test names or assertions. Do not use a test-only inventory as a substitute for inspecting the live source and UI.
- Run the project's applicable build, unit, and UI/instrumentation checks. Use commands and device workflows that actually exist in the repository; do not invent commands. If physical device testing is available, exercise real user journeys and inspect runtime output/logs for crashes, jank, wrong destinations, stale state, or animation conflicts. Check at normal system animation scale and with motion disabled.
- Where device testing or a specific verification path is unavailable, report that limitation accurately in the final response; do not imply it passed.
- Inspect the final diff for unrelated changes and verify that all new motion respects the original functional behavior and accessibility requirements.

Finish the implementation and verification for this goal. Do not stop after producing a list of recommendations or a proposed plan. Do not create a separate completion summary or documentation file.
