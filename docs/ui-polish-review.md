# UI polish overhaul

Goal: every major screen and form must receive at least 9/10 from each OpenAI council reviewer, based on actual rendered screenshots.

## Evidence required

- Huawei Nova 5T screenshots: month with worked dates, year, navigation menu, day agenda, event entry, shop income, rate history and form, expense list and add/edit form.
- Readable dark surfaces, consistent typography, usable enlarged text, keyboard-safe forms, no clipping.
- Runtime checks: January and November zoom in/out, swiping, selected-day consistency, Back, save/reopen, editor rotation, existing-data loading.
- Passing build and relevant tests; additive persistence migration.
- Open PR titled `UI polish overhaul`, with review rounds and reviewer scores.

## Council baseline

Round 1 reviewed b70ffcf primarily from source, not a complete screenshot set: Sol 7/10, Terra 6/10, Astra 6/10. All held approval. Key findings: stale invisible selected day, loading race in shop editor, clipped year dates with large text, overflowing amounts, fragile shared transition registration and lost editor drafts on rotation.

## Current status

Visual gate achieved in round 4. All three council reviewers approve every service at 9/10 or above, based on the evidence mapping below. Do not infer unobserved runtime behavior from screenshot approval.

## Round 2: Huawei screenshots of fe77244

Ten actual phone captures are in `docs/screenshots/round2/`. CI run 43343116616 passed. Sample records in the captures are test data, not real financial records.

| Reviewer | Overall | Lowest service | Verdict |
| --- | --- | --- | --- |
| GPT-6.1 Sol | 8.3 | Year 6.5 | Hold |
| GPT-6.1 Terra | 7.5 | Year 6 | Hold |
| GPT-6 Astra | 8.1 | Year 6.5 | Hold |

Agreed corrections: explicit compact thumbnail line height; remove month-specific footer from year mode; consistent neutral/gold sheet and add actions; quieter unrecorded income; singular event count. f570882 implements these corrections but requires fresh rendered evidence. Rates/expense row hierarchy, localized date entry, and form spacing remain under review. The overall goal is not achieved.

## Round 3: Huawei screenshots of e46d98e

Twelve phone captures are in `docs/screenshots/round3/`. The exact build and unit test job passed in run 43344362840. Each reviewer inspected all twelve images.

| Reviewer | Overall | Verdict |
| --- | --- | --- |
| GPT-6.1 Sol | 9.1 | Approve visual set, every service at least 9 |
| GPT-6.1 Terra | 8 | Hold: crowded year numerals; date controls resemble static metadata |
| GPT-6 Astra | 9.0 | Hold: rate/add expense/edit expense forms 8.5 due to date affordance |

625ae22 adds outlined date controls with calendar icons, adapts year thumbnails to two columns with enlarged text, brings the selected month into view, and normalizes calendar cents. Fresh round-four screenshots are required. Huawei is currently unavailable; local testing is switching to Android Studio's SDK/emulator. Emulator evidence must be labeled separately from the prior physical-device captures.

## Round 4: final visual approval

Source: 13da293. Revised surfaces were built locally with Gradle 8.9/JDK 17 and Android Studio's SDK; `testDebugUnitTest assembleDebug` passed. The two calculation tests report zero failures/errors. Installed and launched on Android Studio's API 37 emulator, configured to 1080x2340, 480 dpi and 1.15 font scale. This is a 360dp compact-screen check, not Android 10 device equivalence.

New captures in `docs/screenshots/round4-emulator/` cover month, two year-scroll positions, rate form, new/edit expense, and the themed date picker. The settled year-top image replaces a transient capture that omitted chrome. Unchanged agendas, event/shop forms, rate/expense lists and menu retain the actual Huawei evidence in round3; each reviewer explicitly retained their approval for those surfaces.

| Reviewer | Round 1 | Round 2 | Round 3 | Round 4 | Every service >=9 |
| --- | --- | --- | --- | --- | --- |
| GPT-6.1 Sol | 7 | 8.3 | 9.1 | 9.2 | Yes |
| GPT-6.1 Terra | 6 | 7.5 | 8 | 9 | Yes |
| GPT-6 Astra | 6 | 8.1 | 9.0 | 9.2 | Yes |

All three independently inspected screenshots and approve the final visual set. Terra's minimum score is 9 on every service. Sol's final service scores range from 9.1 to 9.3; Astra's range from 9.0 to 9.2.

## Validation limits

The earlier Huawei checks exercised swiping, Back, date entries, and save/reopen with sample data. The local emulator check exercised install/launch and date-picker entry/dismissal. Screenshots do not prove frame-by-frame motion, all keyboard layouts, migration safety, accessibility services or rotation paths. These remain engineering checks beyond this screenshot-design approval. The PR remains open for user review; no merge or production release is implied.
