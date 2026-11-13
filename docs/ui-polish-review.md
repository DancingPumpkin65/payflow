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

Implementation and screenshot review in progress. No reviewer has approved the overhaul. Do not infer visual quality from green compilation or the launch-only CI screenshot.

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
