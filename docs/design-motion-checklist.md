# Calendar design and motion checklist

App source: `3ad430c`.

This pass follows the user's two 20-rule reference images. Rules are applied to
the relevant interaction, rather than adding every effect to every control.
The standard Android switch, date picker and text input keep platform behavior.

## Visible changes

- Opening + leaves the calendar brightness unchanged. Its transparent outside-tap
  target dismisses the actions without drawing a dark overlay.
- Removed All time above Earned, Spent and Net.
- Rebuilt the empty-calendar illustration with layered pages, a folded front
  sheet, grey header and rounded binding rings, closer to Huawei Calendar.
- Replaced the generic dropdown with a compact dark, rounded menu with inset
  separators and comfortable rows. It contains Payflow's real destinations,
  rather than copying unrelated account/printing features.
- Filled up/down triangles replace thin header chevrons. The editor date row uses
  a filled directional triangle, with RTL mirroring.
- Removed previous/next-year buttons. The three-by-four year view pages through
  years by horizontal swipe, following the finger directly.
- The selected month still expands from its measured tile. The day/event area,
  empty state, totals and + move upward into place on entry and downward on exit.

## Animation rules

| Reference rule | Application |
| --- | --- |
| 1. Ease-out entrances | Agenda, menu and add actions use ease-out entrances. |
| 2. Dropdowns 150–250ms | Menu entrance is 220ms; exit is 176ms. |
| 3. Stagger rows by 30ms | The two newly revealed add actions stagger by 30ms. Existing saved rows do not replay decorative entry animations. |
| 4. Scale to 0.97 on press | Custom date and add controls respond at 0.97 immediately, then return on release. |
| 5. Stretch moving knobs 12% | No new custom slider/knob is introduced. The standard Worked switch retains native geometry rather than a partial imitation of its thumb. |
| 6. Stagger as details settle | Add actions reveal in sequence. The event area settles as one coordinated panel with the month transition. |
| 7. Arcs for object movement | Add actions take a shallow lateral arc while travelling from/to +. |
| 8. Pulse to confirm success | Actual Room work/event/shop changes trigger a restrained 2% confirmation pulse in totals; initial loading does not. |
| 9. Ease-in-out moves | The month transform uses ease-in-out movement; year swiping is finger-driven. |
| 10. Popovers start at 0.95 | The anchored menu and add actions start at 0.95 scale. |
| 11. Enter from trigger | The menu pivots from the top-right trigger; actions rise from +; the month uses its selected thumbnail. |
| 12. Exit to trigger | Each reverses toward the same origin. |
| 13. No everyday bounce | No spring overshoot or bounce is added. |
| 14. Do not tween while held | Press compression snaps to its held value. Drag previews and year paging follow the finger directly. Release/settling may animate. |
| 15. Exits 20% shorter | Month/agenda 320→256ms, menu/main add 220→176ms, secondary action 200→160ms. |
| 16. Overlap old/new states | Prepared year/month layers coexist during the transform; agenda movement is coordinated on the same navigation change. |
| 17. Masks for reveals/color | Screen clipping contains sliding content; rounded surfaces and circular date feedback clip their own drawing. |
| 18. Crossfade icons and blur | + and Close crossfade. Blur is deliberately omitted on the Android 10 Huawei rather than adding a costly simulated blur. |
| 19. Rubber-band past bounds | Paging retains platform-bounded behavior; no custom rubber-band or free-position dragging is introduced. |
| 20. Reuse layoutId | Compose retains one prepared month layer and measured thumbnail bounds. This is the native equivalent of retaining shared-element identity, not a web layoutId API. |

## Design rules

| Reference rule | Application |
| --- | --- |
| 1. Delete filler badges | No decorative status badges. |
| 2. Drop redundant labels | Removed All time; empty metadata is not repeated alongside No events. |
| 3. Remove random italics | Regular native type throughout. |
| 4. Mono only when needed | No decorative monospace. Numbers use tabular features in the existing sans font. |
| 5. Use a design system | Shared dark Material theme, blue accent, 24dp content margins and consistent spacing. |
| 6. Show actual product | Verification uses the real installed app and persisted records, not mockups. |
| 7. Enlarge main feature | Calendar remains primary; full 48dp date rows and three-by-four year grid remain. |
| 8. Group with spacing | Day data, empty state and totals have separate space without dashboard cards. |
| 9. Specific headings | Event and Shop income pages retain task-specific titles. |
| 10. Emphasize CTA | Blue + remains the primary action, above the totals. |
| 11. Accent color | Consistent blue; neutral empty-state illustration. |
| 12. Consistent icons | Filled directional arrows, consistent Material action icons and accessible icon names. |
| 13. Remove embellishments | No added onboarding, gradients, motivational copy or decorative bounce. |
| 14. Nested radii | Visible rounded containers keep coherent insets. Flat same-color fields do not add competing inner card boundaries. |
| 15. Text contrast | Light text on dark surfaces, readable subdued adjacent dates/weekends, no dim overlay when opening +. |
| 16. Wrap headings | Weighted headers and native text layout; no forced decorative line breaks. |
| 17. Optical alignment | Shared margins with icon padding accounted for; equal calendar columns. |
| 18. Tabular numbers | Retained for dates, amounts and year thumbnails. |
| 19. Loading states | Retained data-backed startup state; no artificial delays on local writes. |
| 20. Useful feedback | Circular ripples, immediate press feedback, drag preview, actual-save confirmation and meaningful navigation motion. |

## Verification and limits

- Local build, unit tests and lint are rerun before delivery.
- On the connected Huawei, sampled calendar pixels were identical with + closed
  and open: mean RGB difference 0 in the unchanged calendar region.
- Swiping the year from 2026 to 2027 and selecting March opens March 2027.
- Real phone screenshot bursts contain intermediate frames of both zoom
  directions. Capture calls take roughly 0.35–0.47s, so they establish placement
  and direction, not measured smoothness or 60fps.
- Phone screenshots and timing samples remain local under
  `app/build/outputs/motion-review/` because they contain existing financial data.
- Hardware blur, a custom stretched switch thumb and a custom rubber-band are
  not claimed. Full TalkBack validation remains unverified.
