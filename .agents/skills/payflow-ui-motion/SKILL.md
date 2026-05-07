---
name: payflow-ui-motion
description: Apply Payflow's screenshot-derived design and animation rules whenever implementing or reviewing its screens, controls, navigation, editors, or visual feedback.
---

# Payflow UI and motion

Apply this skill to every Payflow UI change, including small fixes. Use the
Android specialist skills for platform details. These are the user's two
20-rule screenshot references, translated to native Compose behavior.

## Design rules

1. Delete filler badges.
2. Remove redundant labels and instructional filler.
3. Avoid arbitrary italic emphasis.
4. Use monospace only when the content requires it.
5. Use the shared dark design system and blue accent.
6. Inspect the running product, not only mockups or code.
7. Give the calendar the primary space.
8. Group related content with spacing.
9. Use specific, task-based headings.
10. Make the primary action clear. Calendar + floats over content; no footer panel.
11. Keep a coherent accent color.
12. Keep icons consistent, including compact rounded filled disclosure arrows.
13. Remove decorative embellishments.
14. Derive nested corner radii from outer radius minus inset where applicable.
15. Check text and control contrast on every actual dark surface.
16. Let headings wrap without clipping or hiding actions.
17. Check optical alignment, shared side margins and equal calendar columns.
18. Use tabular figures for dates and money.
19. Show loading only for real waiting, without artificial delays.
20. Animate feedback that communicates an action or state change.

## Motion rules

1. Ease out entrances.
2. Keep dropdown entrances within 150–250ms.
3. Stagger newly revealed action/list rows by 30ms when a sequence helps.
4. Custom press feedback compresses to 0.97.
5. Stretch a custom moving knob by 12% only when introducing that interaction.
6. Let details settle after their parent; avoid competing animations.
7. Use arcs for travelling objects such as + actions when helpful.
8. Confirm successful writes with restrained feedback tied to real success.
9. Ease in and out for movements between positions.
10. Start popovers at 0.95 scale.
11. Enter from the trigger or a consistent navigation direction.
12. Exit toward the trigger or reverse navigation direction.
13. No bounce in everyday navigation. The user explicitly requests liquid,
    spring-based morphing for workday range selection, + expansion and Worked
    toggle only. Use connected necks, restrained stretching and surface tension
    there; keep held range geometry directly finger-driven.
    For ranges, morph only the newly entered pair, then separate within 650ms
    even while held. Never leave all selected days joined. Limit preview and
    commits to the displayed month. Expense category dropdowns belong only in
    the add/edit form; the main list shows both categories.
    Use visual grid adjacency for horizontal, vertical and diagonal travel;
    do not substitute consecutive calendar dates for the actual pointer path.
    Deform the bubble fills while keeping date glyphs stable. Activity uses
    compact mobile heatmap squares, with a full-size date-picker alternative.
14. Held/dragged controls follow the finger directly, without tween lag.
15. Exits are 20% shorter than entrances.
16. Overlap outgoing/incoming screens instead of replacing them instantly.
17. Clip reveals to their intended bounds; clip ripples to control shapes.
18. Crossfade changing icons. Blur is conditional on measured device support.
19. Retain platform overscroll; custom rubber bands require a relevant gesture.
20. Preserve shared element identity and measured bounds across transitions.

## Required implementation and verification

- Audit the whole affected journey: menu → destination → editor → save/cancel
  → Back, plus system Back and rapid repeat taps. Do not fix only one entrance.
- Keep screens at stable full-window bounds while moving their drawing. Do not
  animate destination container size or mix horizontal and vertical travel for
  one navigation step. Let an anchored menu finish exiting before navigation.
- Keep outgoing state composed until its exit finishes. Save and cancel must
  both finish the editor exit before removing its window. Prevent double saves.
- Preserve date, query, draft and scroll state; never reset storage to test UI.
- Respect Android's animation duration scale, including disabled animations.
- Run build/unit/lint checks and inspect the actual transition on a connected
  device or emulator. Capture intermediate frames, not only settled screenshots.
- Check clipping, position reversals, keyboard/inset jumps and unexpected dark
  panels. Restore any temporary device animation settings after inspection.
- Report which paths were exercised and what could not be verified. Compilation
  and historical council scores do not prove visual or motion correctness.
- Apply conditional rules only to relevant controls. Record a specific reason
  for omissions; never add knobs, blur, bounce or decoration merely to tick a box.
