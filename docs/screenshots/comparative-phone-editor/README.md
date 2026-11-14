# Final phone-editor verification

Source `7e115af` includes the day-editor cancellation fix. Captures 01 and
03–08 were made on `6eac2c1`; the subsequent change resets discarded drafts
without changing their layout. Captures 09–15 and all three recordings use
`7e115af`. Adjacent recording JSON files verify the installed APK hash,
clean app source, starting display configuration and encoded dimensions.

- 01: 360×640dp, 130% text, numeric keyboard open. All expense fields and
  the named save action fit.
- 03: the temporary 12.50 DH expense saved successfully. It was deleted
  before further checks.
- 04: cancelling another expense leaves only the original transport expense.
- 05–08: populated calendar, day details and full-window event/income editors.
- 09: type an event draft, cancel and reopen. The title is blank.
- 10: append to income notes, cancel and reopen. Saved notes remain unchanged.
- 11–15: January, year selection, empty day, rate editor and dark date picker.
- `selection-check.json`: selecting dates does not edit work records; the
  explicit switch changes only its date and the original rows are restored.
- January and November videos show both zoom directions. Launch shows the
  original calendar/check mark, real loading state and calendar entrance.

Normal captures use 360×780dp and 115% text. Review fixtures differ from the
parent revision, so before/after balances are not an income-algorithm change.
Videos and frame samples establish continuity, not 60fps performance.
