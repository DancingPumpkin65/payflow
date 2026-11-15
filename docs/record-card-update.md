# Shared record cards

Events, Expenses and Pay rates use the same rounded grey card, blue accent bar,
clipped ripple and immediate 0.97 press feedback. Tapping edits the original
record. Expenses no longer use a separate actions menu; rates now support
update and delete through Room without inserting replacement IDs.

A right swipe must exceed 80% of the card width before deletion is accepted.
The distance check also rejects high-velocity short flings that Material's
normal positional threshold alone can accept. Short swipes settle back. Each
list keys cards by database ID so swipe state stays with its record. Accessible
delete actions remain available without requiring a gesture.

Editing/deleting a rate changes the rate history used to calculate work-day
income. No work-day records are rewritten. Existing editor transitions,
positive-amount validation and exact-cent parsing remain.

## Huawei verification

- Created a temporary 12.34 DH expense (stored as 1234 cents). A fast swipe of
  about 64% retained it. Tapped and edited the same ID, then swiped beyond 80%
  to delete it; the existing expense below remained intact.
- Created a temporary 123.45 DH pay rate. A fast swipe of about 76% retained it.
  Tapping opened the prefilled editor; saving updated the original record.
  A swipe beyond 80% removed only the temporary rate.
- Created a temporary event. A fast swipe of about 64% retained it, and a swipe
  beyond 80% deleted it through the same shared component.
- After cleanup, hashes of all five data tables matched the snapshot taken at
  the start of this pass. No personal records were edited or reset.
- Local unit tests, lint and debug assembly passed: nine tests, no failures,
  no lint errors. Advisory lint warnings remain. Screenshots are private under
  `app/build/outputs/card-review/`; full TalkBack and RTL gesture testing remain
  outside these checks.
