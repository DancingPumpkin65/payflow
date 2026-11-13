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
