# Expense ledgers and liquid interactions

Expenses now have a persisted Personal or Shop category. The list's dropdown
filters the ledger, and + defaults to that selected ledger. The same dropdown
in add/edit can move a record between ledgers without changing its ID. Work-pay
totals subtract only Personal expenses; Shop revenue subtracts only Shop
expenses. Room version 3 migrates older expenses to Personal with a non-null
default and preserves every existing field.

The user's explicit liquid-motion request applies to three controls:

- Long-press workday ranges draw pinched, curved connections between adjacent
  selected dates within each week. The leading date stretches 12%; the range
  follows the finger directly and connections relax away on release. Cancellation
  removes the preview without saving. Week boundaries never draw across unrelated
  rows.
- The + actions grow from their origin through temporary curved necks, stretch
  while travelling, then settle as separate buttons using restrained springs.
  Secondary actions retain their 30ms stagger and accessible labels.
- The Worked toggle uses a rounded liquid thumb with a 12% mid-travel stretch,
  spring settling, standard Switch semantics and a 64×48dp touch target.

The effects use Canvas paths and native Compose springs rather than blur or
external animation dependencies. Navigation keeps its existing slide/fade.
System animation-duration scaling remains supported.

## Verification

- Local unit/lint/debug checks pass: 13 tests, no failures, no lint errors.
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
