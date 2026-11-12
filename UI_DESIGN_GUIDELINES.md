# UI design and refinement guidelines

Use this guide as a reference for future UI redesigns and refinements. The user's specific brief and Elovaire's established design system always take precedence. Treat these as working principles, not as a mandate to redesign unrelated parts of the app.

## Design stance

- Finish the requested surface end to end: visual hierarchy, interaction states, content, accessibility, and verification all matter.
- Choose the right level of ambition. For an existing app surface, refine with precision and restraint; preserve recognizable patterns and make behavior predictable. Make a larger visual departure only when the brief calls for a redesign.
- Make a few coherent decisions instead of accumulating decorative effects. Every visual treatment should clarify hierarchy, state, or function.
- Preserve product meaning, content, and behavior unless the brief explicitly changes them. Keep out-of-scope areas intact.

## Before changing a surface

1. Read the request closely and identify what must change, what must remain, and where the UI lives in the user's actual workflow.
2. Inspect the existing screen, neighboring screens, shared components, assets, and design tokens. Treat the running app and current code as the source of truth; do not invent a parallel design language.
3. Select the smallest coherent change that satisfies the brief. Reuse project patterns and platform conventions where they fit.
4. Check the complete surface and its states, not just the first screenshot: default, pressed/focused, selected, disabled, loading, empty, and error states as applicable.

## Visual craft

- Build hierarchy with intentional alignment, spacing, typography, and contrast. Use spacing to show grouping: related content belongs closer together than separate groups.
- Keep text readable at its actual shipped size. Use a restrained type scale and consistent weights; reserve expressive treatments for places where they improve the experience.
- Keep color semantic and consistent. Check foreground/background contrast, selected and disabled states, and accent use. As a baseline, target at least 4.5:1 contrast for normal text and 3:1 for large text and meaningful UI graphics, while respecting the app's established palette.
- Use surfaces, borders, shadows, blur, and gradients only when they help distinguish layers or improve legibility. Depth should feel intentional and should blend with the actual content beneath it.
- Use a consistent icon family, scale, stroke, and alignment. Icons should communicate real actions or categories, not serve as unexplained ornament.
- Keep content concise and actionable. Empty, loading, and error states should explain the state and offer a sensible next step where one exists.
- Check touch targets, text scaling, screen-reader labels, focus order, and interaction feedback. Visual polish must not make controls harder to use.

## Interaction and motion

- Make controls behave as their appearance promises. Keep actions discoverable, state changes legible, and repeated patterns consistent.
- Animate to explain a change of state, continuity, or spatial relationship—not merely to decorate. Keep motion brief, smooth, and interruptible where appropriate; around 150–250 ms is a useful starting range for ordinary UI transitions, not a fixed rule.
- Prefer native Android and Jetpack Compose motion and interaction patterns. Avoid excessive springing, stacked transitions, and expensive per-frame work.
- Verify gestures alongside nearby gestures and scrolling. Resolve touch conflicts without shrinking important targets or changing placement unless requested.

## Elovaire-specific guardrails

- Existing Elovaire screens, Geist typography, semantic accent/color logic, spacing conventions, blur/surface treatment, and established control styles are the local design system. Reuse them before introducing new motifs.
- Do not add card containers, gradients, blur, animation, or accent color by default; use each only when it improves the requested hierarchy or behavior.
- Preserve existing functionality and data behavior during visual work. For shared components, check all affected callers.
- Follow project localization and UI-copy rules for every new user-facing string, including accessibility descriptions.
- Respect explicit exclusions in the brief, including screens the user asks not to redesign.

## Verification

- Run the narrowest relevant build or test supported by the repository.
- Inspect the rendered result at realistic device sizes and configurations. For responsive or tablet work, also check landscape and expanded layouts.
- Exercise the requested interactions, edge cases, and neighboring touch/scroll behaviors on an emulator or device when available.
- Make one focused visual QA pass after implementation and correct concrete inconsistencies or bugs found. Stop when the requested outcome is coherent and verified; avoid open-ended polish outside scope.

## Source and attribution

This project guide distills principles from [Impeccable](https://github.com/pbakaus/impeccable), especially its [core skill](https://github.com/pbakaus/impeccable/blob/main/plugin/skills/impeccable/SKILL.md), [craft floor](https://github.com/pbakaus/impeccable/blob/main/skill/reference/craft-floor.md), [Operate mode](https://github.com/pbakaus/impeccable/blob/main/skill/reference/operate.md), and [Polish guidance](https://github.com/pbakaus/impeccable/blob/main/skill/reference/polish.md). It is an original, project-specific summary rather than a copy of the upstream skill. Impeccable is distributed under the Apache-2.0 license; consult its repository for the current source and license.
