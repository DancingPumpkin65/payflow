# Event search, editing and creation controls

App source: `b16c730`.

## Changes

- Removed Go to today from the calendar menu.
- Enlarged the filled arrows and rounded their triangle corners. Disclosure
  arrows mirror in RTL layouts.
- Aligned the Search and four-dot menu targets to the Huawei Calendar reference:
  56dp toolbar, 48dp touch targets, with centers at 276dp and 324dp on the
  360dp-wide phone. The four-dot drawing uses a 28dp area.
- Added a search control immediately left of the menu. The original header
  fades/moves out as the pill search field slides down into place. Entrance is
  220ms; exit is 176ms. Focus and the software keyboard appear automatically.
- Search matches event titles and notes across all dates, ignoring letter case.
  Returning from the event editor preserves the search query and results.
  Results show their stored dates. The calendar behind search is read-only, and
  hidden calendar nodes are removed from the results' accessibility tree.
- Events use rounded grey cards with an accent bar. Tapping opens the existing
  event for editing; saving updates its original ID. Swiping right deletes it;
  leftward swipes do not. An accessibility delete action is available too.
- Event-card ripple drawing is clipped to its rounded boundary. Stable event-ID
  keys prevent a deleted card's swipe state from transferring to another card.
- The day agenda scrolls independently above the + action and financial totals,
  including when several events have multiline notes.
- Expenses and Pay rates now use bottom-right + actions rather than header Add
  buttons. Their editors use the same rounded sections, icon-led inputs, date
  rows and save-check controls as events.

No event start/end times are invented. Existing events store a date, title and
notes; the reference screenshot guides their card styling.

## Huawei checks

Tested on the connected Huawei Nova 5T, Android 10 / API 29:

1. Created a temporary January 2027 event and edited its title and notes. Reading
   Room confirmed that the ID stayed the same and no duplicate was inserted.
2. Selected another date and searched by lower-case title text, then by note
   text. Both found the event from its original date.
3. Confirmed the input-method service reported the keyboard shown immediately
   after entering search. Inspected the search header and resulting cards.
4. Swiped left and confirmed the event remained. Swiped right and confirmed it
   was removed, with a No matching events result afterward.
5. Added a temporary 13.25 DH expense. Room stored exactly 1325 cents. Deleted
   the test expense through its existing actions menu.
6. Opened Pay rates through its + action. A zero rate showed a validation error
   and could not be saved; cancelling left rate history unchanged.
7. Compared all five data tables with a pre-test snapshot after cleanup. Every
   original record matched. No database reset or uninstall was used.
8. Opened an existing event from search and cancelled; its query and results
   remained. Scrolled the three-event agenda and confirmed totals stayed visible.

`testDebugUnitTest lintDebug assembleDebug` passes: nine unit tests, no failures,
and no lint errors. Existing advisory lint warnings remain. The final APK's
signature and installation are verified before delivery.

Private screenshots and the database-check receipt remain under
`app/build/outputs/search-review/`. The direct-install debug APK is copied to
Downloads as `Payflow-b16c730.apk`. Full TalkBack speech, RTL rendering and
frame-rate performance are not certified by these checks. Earlier council
scores refer to earlier revisions; this revision has not been rescored.
