# Navigation motion check

The destination switch previously removed the current screen immediately.
Selecting Expenses or Pay rates also removed the calendar-owned popup before
its 176ms exit could finish. This explains the abrupt handoff; a separate
pre-fix recording of the reported zigzag was not obtained.

## Changes

- Destinations now overlap during a fixed-window horizontal slide and fade.
  Entrance is 300ms, exit 240ms. Back reverses direction. Moving layers retain
  fixed bounds rather than interpolating different destination sizes.
- Menu selection waits for the anchored popup's exit before changing screens.
  A pending selection blocks repeated menu taps.
- Event, Shop income, Expense and Pay rate editors share a 300ms vertical
  entrance and 240ms exit. Cancel, system Back and Save finish the exit before
  removing the dialog. A closing guard prevents repeated save callbacks.
- Calendar, query and list state remain owned by their existing saved-state
  providers. No navigation timers or artificial data-loading delay were added.
- The calendar's totals footer remains removed. The + overlays a full-height
  agenda; trailing scroll padding lets the last event clear the button.

## Standing rules

`.agents/skills/payflow-ui-motion/SKILL.md` contains the user's two screenshot
rule sets and the required journey-level verification procedure. Root
`AGENTS.md` requires the skill for every Payflow UI or interaction change.
Conditional effects such as blur and custom knob stretching require a relevant
control and device support, rather than adding decoration to satisfy a count.

## Verification

- Local unit tests, lint and debug assembly passed (nine tests, no failures;
  no lint errors, existing advisory warnings remain).
- Installed on Huawei Nova 5T / Android 10. Exercised Expenses and Pay rates
  from the menu, Back to Calendar, and opening/cancelling their editors.
- Opened Event and Shop income through + and dismissed with Cancel/system Back.
- Sampled seven frames of Calendar → Expenses with Android animator duration
  scale temporarily set to 5. The popup closes before the two full-height
  screens overlap; the incoming screen travels horizontally without a sampled
  vertical reversal. Restored the original scale, 1.0.
- Captures are private under `app/build/outputs/search-review/`. Sampling
  verifies intermediate states, not FPS or an exhaustive absence of jitter.
- No personal records were edited by these navigation checks. Save uses the
  existing validators and callbacks; a new write was not performed in this pass.
