# Huawei calendar interaction update

App revision: `1f42d1e`. This follows the user's new Calendar reference direction
and supersedes the earlier Quiet ledger styling. Previous council scores refer
to the older implementation; this revision has not been rescored.

## Changes

- Removed the swipe hint, monthly earnings subtitle and top-right Today button.
  Go to today remains in the menu.
- Replaced the vertical ellipsis with Calendar's four-dot menu treatment.
- Changed the accent and launcher mark to blue and uppercased weekday/month labels.
- The year uses three columns and four rows. Whole month tiles remain tappable;
  the miniature dates illustrate the month rather than acting as individual targets.
- Added a circular bottom-right + button opening a full-window editor.
- Event is the default tab; Shop income is the second. The selected tab has a
  short blue underline. Each has icon-led inputs, date selection, notes and a
  labelled accessible save icon. Switching tabs preserves separate drafts.
- Day-detail Add/Edit controls open the same editor. Cancelling disposes the draft.
- Long-press a day and drag to preview an inclusive work-date range. Lifting
  commits the range in one Room transaction. Starting on a worked day clears
  the range instead. Reversing direction recomputes the range from the original
  anchor and restores the original preview state outside the range. Cancellation
  discards the preview. Short swipes still page months; taps still inspect dates.

Range selection is limited to the visible six-week grid. It does not automatically
page to another month while the finger is held. The selected day's switch remains
available as a non-gesture way to change work status.

## Verification

Inspected Calendar on the connected Huawei Nova 5T, including its real new-event
screen, tab labels and cancel/save controls. No Calendar event was saved.

Tested Payflow on that phone:

- Updated the installed app without uninstalling or losing its records.
- Dragged March 9–20, 2026 and checked the actual Room rows. Exactly all twelve
  dates were added, including dates skipped by the pointer, with no unrelated
  work rows removed. Repeating from a worked date cleared the range.
- Created an event, switched away and back with its draft intact, saved it and
  deleted the temporary record.
- Saved a shop-income amount and note and checked the actual stored values.
- Restored the private pre-test database and compared every row across all five
  tables. The original work days, rates, expenses, events and shop income matched.
- Inspected the final month, three-by-four year grid and both editor tabs on the
  phone. Month/year navigation reaches the correct selected month.
- Build, nine unit tests and lint pass. Lint reports zero errors; existing
  dependency-update and icon/resource warnings remain.
- The final APK signature verifies and the exact APK was installed successfully.

Phone screenshots and the private test receipt are local under
`app/build/outputs/phone-review/`. They are not committed because they contain
the phone's existing financial records. The phone does not expose the shell
`screenrecord` executable, so a new phone video could not be captured. No phone
frame-rate or full TalkBack certification is claimed.

## APK

`Payflow-1f42d1e.apk` is copied to the user's Downloads folder to survive build
directory cleanup. It is a signed debug APK for direct installation.

SHA-256: `6b5b1bd32c6f7eefdb9fe20c9c465d63c19f9dab2f6fde5abe639be2559d3efc`.
