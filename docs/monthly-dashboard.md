# Monthly dashboard

Open Dashboard from the calendar's top-right menu. Swipe horizontally to move
between months. Work pay and Shop revenue are separate views; they are never
added together. Work pay subtracts Personal expenses; Shop revenue subtracts
Shop expenses. Choose the ledger from the expense list or expense editor
dropdown. Existing expenses migrate to Personal without changing their values.

- The summary ring draws from zero with a 650ms ease-out animation. Coral is
  spending and teal is the positive remainder. When spending exceeds income,
  the ring is fully coral and the net remains a signed negative amount. An
  empty month has a neutral ring, zero totals and an explicit empty state.
- Grouped daily bars compare income (blue) and expenses (coral), with a common
  zero baseline and a maximum daily amount. Each month uses its real day count.
- Money remains integer cents until formatting. Work pay applies the effective
  historical rate on each worked date. Multiple expenses on a date are summed;
  events do not imply financial entries.
- Standard Compose animation duration scaling applies. No artificial loading
  delay, external chart library, network access or database migration is added.
- Menu exit and dashboard entrance share the existing navigation motion;
  Back returns to the saved calendar state.

Calculation tests cover boundaries, effective-rate changes, exact cents,
separation of shop revenue, leap days, negative net and empty months.

## Rendered verification

- Local unit tests, lint and debug assembly pass: 12 tests, no failures and no
  lint errors. Existing advisory lint warnings remain.
- Huawei disconnected before this pass, so rendered checks used the installed
  Android Studio API 37 emulator (1080×2400, density 420), not the phone.
- Verified menu entry, both income tabs, November → December swipe, real
  30/31-day labels, populated and empty states, and Back to Calendar.
- Temporary emulator-only fixtures produced revenue 5,720.00 DH, expenses
  1,220.00 DH and net 4,500.00 DH. Work pay remained separately 1,500.00 DH.
- Eight slowed captures show the ring and bars growing through intermediate
  states. The animation scale was restored to its original value afterward.
- Removed all fixtures by restoring the emulator's original database; all five
  table hashes match the starting snapshot. Phone data was not touched.
- Captures are local under `app/build/outputs/dashboard-review/`. These checks
  do not certify frame rate, full TalkBack behavior or large-font layouts.
