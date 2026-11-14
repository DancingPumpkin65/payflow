# Payflow comparative UI review

This work is stacked on PR #1 in [PR #2](https://github.com/DancingPumpkin65/payflow/pull/2).

## What changed

The calendar previously changed a work record when someone tapped a date to
inspect it. It also gave little indication of selection, swipe navigation or
the month picker. A tap now selects a date. The selected day's Worked switch
records work explicitly. A separate ring, filled worked circle and bold today
number distinguish those states, including a selected worked day.

The month heading has a disclosure icon, Today is visible, and the selected-day
summary reports events and shop income. The all-time figures have one scope
label. Day details show personal pay separately from shop revenue and its notes.
Forms use specific actions and respect keyboard insets. Compact windows scroll
instead of compressing calendar targets.

Editors now use the phone window, with Close and a named save action above
the keyboard. At 360×640dp and 130% text, expense title, amount and date fit
together while the numeric keyboard is open. Cancelling day-event or shop-income
edits discards their draft when reopened. Saved records remain unchanged.

The supplied design and animation checklists guide the details: one warm accent,
consistent Material icons, spacing instead of decorative cards, tabular numbers,
readable dark surfaces and useful loading feedback. Calendar movement follows
the selected month tile with ease-in-out timing. Entering takes 320ms and returning
takes 256ms. Startup uses ease-out timing. Native control feedback is retained;
decorative bounce, blur and stagger are unnecessary for these tasks.

The app has an original calendar/check mark used by the launcher and startup.
Loading feedback lasts only while local data is being read. The first calendar
does not show false zero balances before that read completes.

The year and month remain measured during navigation. One prepared month layer
scales and moves to the selected thumbnail; surrounding controls fade on the
same timeline. This replaces lazy shared-element setup and changing parent
geometry that produced a visible snap and footer overlap.

## Evidence

- [Fresh baseline captures](screenshots/comparative-before/) are from the parent
  revision `a225387`.
- [Round-three captures and recordings](screenshots/comparative-final/) cover calendar
  selection, worked selection, year navigation, January, populated day details,
  events, shop income, rates, expenses, date picker, menu and keyboard states.
- [Interim captures](screenshots/comparative-after/) document failures found
  during the review. They are not the final verification set.
- [Round-four captures](screenshots/comparative-refined/) add the more compact
  home, readable year tiles, form scrolling cues and large-amount layouts. The
  selected date, work control and totals now fit together at 360×640dp and
  130% text with populated review data. Event previews use extra room in the
  taller window. Very large amounts switch the totals to labelled rows rather
  than breaking numbers across columns; scrolling remains a fallback.
- Round four uses a measured minimum page height and 48–56dp date rows, rather
  than stretching the calendar to consume every spare pixel. The day summary
  keeps its work switch alongside the selected date; all-time totals remain at
  the bottom. The year uses 11sp dates with font-scaled row spacing.
- [Consistency-pass captures](screenshots/comparative-consistent/) align the
  month, year and date picker to the same device-locale week start, preserve
  weekend styling by actual weekday, add an empty-day action and soften form
  scroll boundaries. The emulated US locale uses Sunday first throughout;
  locales whose week starts Monday use Monday throughout.
- Motion recordings have adjacent JSON files containing installed APK and video
  hashes, source commit, dirty-state check and device configuration.
- [Phone-editor captures](screenshots/comparative-phone-editor/) verify full-window
  forms, save, cancellation and reopening. The consistency overview's empty-day
  pane was recaptured and rebuilt after an earlier duplicate populated image
  was found. Historical interim captures are retained as review history.

The round-three launch recording was found to be 1080×1920 even though its
metadata queried the display after recording and reported 1080×2340. That
metadata is now explicitly labelled as post-capture and is not used to establish
viewport or font scale. The updated recorder waits for the display resize,
records its starting configuration and checks the encoded MP4 dimensions. The
new round-four recordings supersede the legacy launch evidence.

The Android Studio emulator runs API 37 with Radeon hardware graphics. Normal
captures use 1080×2340 pixels at 480 dpi, a 360×780dp window and 115% text.
Compact captures use 1080×1920 pixels, 360×640dp and 130% text. The emulator's
clock crossed to 14 November during the session; review fixtures include 12 and
13 November. Fixture values differ between the baseline and populated final
captures. The income algorithm was not changed by this PR.

### Checks

- `testDebugUnitTest assembleDebug` passes locally with JDK 17 and Gradle 8.9.
- The device test reads the actual Room database before and after selecting two
  dates. Selection leaves work records unchanged. The switch changes only its
  selected date, and the test restores the original records.
- An event with notes and shop income with notes were saved and displayed.
- A temporary billion-dirham daily rate exercised long figures at both window
  sizes. Values remained exact and unbroken; the smaller window used scrolling
  as fallback. The original review database was backed up and restored after
  this test.
- At 360×640dp and 130% text, the expense form scrolls to its date field while
  the keyboard remains open. Its action remains visible; saving reaches the
  expense list. The review-only expense is then deleted.
- January and November have real recordings of both zoom directions. These
  establish placement and continuity, not a 60fps guarantee.
- Main text contrast against black is 19.12:1, secondary text 8.17:1, accent
  9.00:1 and adjacent-month dates 5.42:1. Accent text on the selected dark fill
  is 6.16:1; dialog text on its dark surface is 14.13:1.

The Huawei was unavailable for this pass. Android 10 runtime behavior, TalkBack
speech and predictive Back were not verified. Accessibility labels, selection
and switch state were inspected through the UI hierarchy and source. This is
not a full accessibility certification or a user study.

## References and comparison scope

| Reference | Useful comparison | Applied to Payflow |
| --- | --- | --- |
| Cron | Calendar hierarchy, picker disclosure and focused date | Explicit picker, selected date and useful day summary |
| Exoplan | Dark surfaces and coherent identity | Readable dark dialogs and restrained startup mark |
| Joi | Clear instructions and named actions | Explicit work control and form action labels |
| Givingli | Brand consistency and form hierarchy | Consistent mark, type and primary action treatment |
| FocusPomo | Identity across launch and product | One mark and warm accent, without decorative onboarding |
| Fossify Calendar | Android month, agenda, editor and settings | Native controls and task-focused day details |

The [user-provided reference sheet](screenshots/references/user-provided.jpg)
contains the supplied thumbnails. The
[Fossify sheet](screenshots/references/fossify-publisher.jpg) contains eight
[official publisher screenshots](https://github.com/FossifyOrg/Calendar/tree/main/fastlane/metadata/android/en-US/images/phoneScreenshots),
also added locally under `thecomparison/fossify` with attribution. These are
reference images, not emulator captures made during this review.

[Flownato's bottom-sheet examples](https://www.flownato.com/patterns/elements/bottom-sheet)
and [TickTick description flow](https://www.flownato.com/flows/ticktick/adding-a-task-description)
were consulted for contextual details and editing. The web reader exposes the
flow structure, but does not provide complete runtime interaction verification.

[Flowscope](https://github.com/0050piyush/flowscope) provides original fictional
screens rather than missing screens of the supplied commercial apps. Its
[Noted task-creation sequence](screenshots/references/flowscope-fictional.jpg)
was reviewed as a design example: context, labelled input, picker, explicit
action and confirmation. It is not ranked as evidence of a real competitor's
runtime behavior. No reference graphics were used as app assets.

Scores below are independent assessments of Payflow's work/pay-tracking UI.
Unrelated gifting and onboarding screens cannot establish a universal ranking
of whole calendar apps. Reference screenshots also cannot establish their
touch behavior or accessibility. Comparative strengths are retained even where
a reference is better suited to its own task.

## Council review

Full rounds: 5, plus one partial interim feedback pass.

| Delegate | Baseline | Interim feedback | Core fixes `76d12a7` | Compact layout `932dc72` | Consistency `8e907bb` | Phone editor `7e115af` |
| --- | --- | --- | --- | --- | --- | --- |
| GPT Sol | 7.3/10 | 8.7/10 | 9.0/10 | 9.1/10 | 9.1/10 | 9.1/10 |
| GPT Terra | 5.5/10 | No score | 7.9/10 | 8.4/10 | 8.6/10 | 9.0/10 |
| GPT Astra | 7.1/10 | No score | 9.1/10 | 9.2/10 | 9.2/10 | 9.3/10 |

The final independent review used GPT-6.1 Sol, GPT-6.1 Terra and GPT-6 Astra,
as requested. All meet the overall 9/10 threshold. Category scores retain their
disagreements, including Terra's typography score below 9.

| Final category | Sol | Terra | Astra |
| --- | --- | --- | --- |
| Typography | 9.0 | 8.8 | 9.1 |
| Layout and spacing | 9.1 | 9.0 | 9.2 |
| Color and contrast | 9.3 | 9.2 | 9.4 |
| Navigation | 9.1 | 9.0 | 9.2 |
| Forms | 9.2 | 9.1 | 9.4 |
| Brand and motion | 9.0 | 9.0 | 9.3 |

Remaining tradeoffs are small year-view dates, a persistent swipe hint and
year-change controls that scroll out of view. Reviewers did not find these
blocking for the captured tasks. Cron retains a stronger scheduling hierarchy,
Exoplan a more distinctive illustrated identity, and Fossify broader agenda
views. Payflow makes work status, personal pay and shop revenue clear together.
The references do not support claiming that it beats every app in every category.

The interim pass identified the invisible selected-worked outline, month
transition reflow and keyboard obstruction. Round three closed those defects,
but Terra held below the requested threshold on density, year legibility and
form scrolling cues. Round four addresses those points without replacing the
native forms or adding onboarding. Terra's next review found an inconsistent
week start between the custom grid and the native picker. The consistency pass
fixes that mismatch and improves empty and scrolling states. Reviewers assess
the resulting tradeoffs independently. Scores are not rounded up to the
requested threshold. The interim feedback was a partial pass; the other four
columns are complete comparative council rounds. A fifth full round reviews
the phone editor, including the compact keyboard state and cancellation fix.

## Repeat the device checks

On Windows, start an Android Studio emulator and open Payflow's month calendar.
Use the existing `scripts/phone-review.ps1` for labelled control selection and
PNG captures. With two dates visible, run:

```powershell
python scripts/check-day-selection.py 2026-11-13 2026-11-12 --output selection-check.json
python scripts/capture-emulator-motion.py hierarchy --month "November 2026" --output november-motion.mp4
python scripts/capture-emulator-motion.py launch --output launch.mp4
```

Both Python checks are restricted to emulator serials. The selection test uses
the installed debug app's database, not a mocked repository.
