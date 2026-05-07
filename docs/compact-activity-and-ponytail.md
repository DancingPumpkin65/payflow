# Compact activity and focused optimization

App source: `a028723`. Local APK: `Downloads/Payflow-a028723.apk`.

The activity view now uses compact rounded squares, month headings and weekday
guides in one horizontal year grid. It keeps the blue theme and binary
worked/not-worked meaning; no invented intensity levels. Tapping a square
still opens that date. A full-size Open date picker and screen-reader date
actions provide alternatives to the dense graphic. Year, selection and scroll
state persist when returning from Calendar.

Range feedback retains 650ms timing, direct selection and the same release
behavior. Both participating fills now stretch/compress more strongly and
pull toward their connecting neck. Only fills deform; date text and circular
press feedback remain stable. The + menu and Worked toggle were not retuned.

## Ponytail review

Installed the official [Ponytail](https://github.com/DietrichGebert/ponytail)
skill from `skills/ponytail` into the host's `~/.codex/skills/ponytail` and applied
its default full mode. No application dependencies or third-party runtime code
were added.

- Calendar markers previously scanned every event and shop entry for each of
  42 dates. A remembered date set replaces those repeated scans with membership
  lookups. Empty notes and zero-income records still produce markers.
- Liquid-cell indices and direction are now computed when the pair changes,
  rather than recalculated in animated drawing. Timing and deformation math
  were preserved during this optimization step.
- Startup/reboot refresh skips events without both a time and reminder. Editing
  or disabling an event still calls the cancellation path directly; eligible
  reminders still schedule normally.

## Verification

- Host unit tests, lint and debug assembly: 24 tests, zero failures, no lint
  errors. Existing advisory lint warnings remain.
- API 37 emulator, with temporary year activity and 600 untimed-event fixtures:
  the calendar crop was pixel-identical before/after the optimization step.
- Compact-grid tap selected January 6, 2025; the accessible picker selected
  January 7. Both opened the correct Calendar details.
- Normal-speed horizontal and vertical captures show stronger deformation and
  separation while still held. No global animation settings were changed.
- One timed reminder still scheduled alongside 600 untimed events. Original
  emulator data was restored; all five original table hashes match.
- Captures remain local in `app/build/outputs/compact-activity-review/`.
  No physical-phone, GPU/FPS or full TalkBack benchmark is claimed.
