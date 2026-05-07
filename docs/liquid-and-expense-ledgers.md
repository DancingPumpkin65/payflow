# Expense ledgers and liquid interactions

Expenses now have a persisted Personal or Shop category. The dropdown appears
only in add/edit and can move a record between ledgers without changing its ID.
The main list shows both ledgers with a small category label; + starts Personal.
Work-pay
totals subtract only Personal expenses; Shop revenue subtracts only Shop
expenses. Room version 3 migrates older expenses to Personal with a non-null
default and preserves every existing field.

The user's explicit liquid-motion request applies to three controls:

- Long-press workday ranges briefly merge only the latest adjacent pair, with a
  650ms squish-and-separate pulse. Connections disappear while the finger is
  still held; selected dates do not form persistent chains. Selection is limited
  to the displayed month, including both preview and commit. Cancellation
  removes the preview without saving. Week boundaries never draw across unrelated
  rows.

Both participating fills stretch/compress and move slightly toward their neck;
the date glyphs stay stable. The 650ms timing is unchanged by the optimization.

Visual neighbors include the cells directly above/below and diagonally adjacent.
The stretch follows the direction of movement. A week-wrap never draws a long
connector across the row. Releasing early lets the current pulse finish rather
than cutting it off; cancelling the gesture still removes the preview.
- The + actions grow from their origin through temporary curved necks, stretch
  while travelling, then settle as separate buttons using restrained springs.
  Secondary actions retain their 30ms stagger and accessible labels.
- The Worked toggle uses a rounded liquid thumb with a 12% mid-travel stretch,
  spring settling, standard Switch semantics and a 64×48dp touch target.

The effects use Canvas paths and native Compose springs rather than blur or
external animation dependencies. Navigation keeps its existing slide/fade.
System animation-duration scaling remains supported. The custom startup logo,
spinner, loading text and entrance fade are removed; the calendar renders
immediately, with workday mutations enabled once stored data is available.

## Verification

- Local unit/lint/debug checks pass: 24 tests, no failures, no lint errors.
  The added calculation test proves Personal and Shop spending do not mix.
- On the API 37 emulator, upgraded a version-2 database containing a 12.34 DH
  expense. Room validated the version-3 schema; the record retained its ID,
  date, title and 1234 cents, with category Personal.
- Changed that record to Shop via its editor dropdown, saved it, filtered the
  list to Shop and checked the Shop Dashboard. It remained one record, with
  the same ID and amount.
- Captured intermediate + expansion, liquid toggle and long-press range frames.
  The range joined adjacent circles while held and saved all five dates
  (November 16–20). The toggle retained Switch semantics and its existing label.
- Restored the original emulator database and verified all five table hashes
  matched the pre-test snapshot. Animation speed was restored too. No phone
  was connected; captures are under `app/build/outputs/liquid-review/`.
- Sampling proves intermediate geometry, not a measured frame rate. Full
  TalkBack speech and real-phone performance remain unverified for this pass.

## Range refinement checks

On the API 37 emulator, explicit held touch events captured the anchor circle,
the adjacent pair merging, and two separate selected circles before touch-up.
An out-of-month long press left all tables unchanged. Starting on January 1
and dragging back into December changed only January 1. Two new calculation
tests cover both ends of a displayed month's boundary. The main expense list
had no dropdown, while New expense retained it. Original records and animation
scale were restored after testing; all five table hashes match.

The slower 650ms refinement was also checked at normal animation speed on the
API 37 emulator: November 16→23 formed a vertical neck and a vertical stretch,
while November 24→25 formed a horizontal neck. Both pairs separated before
touch-up and the selected dates persisted. Geometry tests cover upward,
downward and diagonal neighbors, row wraps and distant cells. Evidence is
under `app/build/outputs/slower-range-review/`.
