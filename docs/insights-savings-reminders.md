# Categories, savings, activity and reminders

App source: `71f4fc4`. Local APK: `Downloads/Payflow-71f4fc4.apk`.

## Expense categories

The Personal/Shop ledger remains separate from the spending category. Expense
forms offer Transport, Food, Shopping, Bills, Uncategorized and custom names.
Custom names are trimmed and reused without case-only duplicates. Dashboard
category amounts and proportions include only the selected ledger and month.
The expense list still has no dropdown. Existing records become Uncategorized
without changing their ledger, ID, title, date or amount.

## Savings

Each month and ledger has its own target. Progress can use positive net income
or recorded savings transfers. Available net is income minus expenses; it is
not a bank balance. Recorded savings is the sum of the transfers the user logs.
The two are displayed separately and never added together. Transfers are not
expenses and do not reduce net. Negative net produces zero net-based progress;
progress caps at 100% while actual amounts remain visible.

Targets can be edited or removed. Transfers have dated, exact-cent amounts;
tap to edit or swipe past 80% to delete through the existing shared card.

## Activity and date ranges

Dashboard's activity action opens a compact Monday-first year grid with month
and weekday guides. Swipe horizontally through its rounded squares; tap a
square to open its date. The full-size Open date picker and screen-reader date
actions provide accessible alternatives. Year changes handle leap years and
padding outside the year. System Back returns to activity and restores scroll.

The date-range action offers trailing 7, 15 and 90 days, or explicit start and
end dates. Both boundaries are inclusive. Reversed dates show an error instead
of misleading results. Historical pay rates apply on each worked date. Net,
expenses, category proportions and recorded savings use the selected ledger.

## Event reminders

Events default to All day with no reminder. Turn All day off to set a 24-hour
time. Lead-time options are at event time, 5/10/30 minutes, 1 hour or 1 day
before. A new reminder must be in the future; an unchanged past reminder does
not prevent editing an existing event's notes.

Reminders use local AlarmManager alarms and a private notification channel.
Notification permission is requested when reminders are enabled on Android 13+.
On Android 12+, the editor offers precise-alarm access when unavailable;
without it the app uses an inexact alarm and explains that timing may be delayed.
Editing replaces the prior alarm, disabling/deleting cancels it, and reboot,
clock/timezone changes and precise-alarm access grants reschedule future events.
Tapping a notification opens the matching date and event. Pending-intent data
uses the full event ID, avoiding request-code collisions. No network permission
or remote notification service is added.

Android permission and scheduling behavior follows the
[official alarm guidance](https://developer.android.com/develop/background-work/services/alarms).
Device battery policies and force-stop can affect delivery; exact timing without
the required access is not promised.

## Data upgrade and verification

Room version 4 adds spending categories, nullable event time/reminder fields,
monthly goals and savings transfers. The explicit 3→4 migration preserves
existing rows; the previous 1→2→3 upgrade path remains registered.

- Host unit tests, lint and debug assembly pass: **24 tests, no failures and no
  lint errors**. Existing advisory lint warnings remain.
- Calculation tests cover inclusive ranges, ledger/category isolation, rate
  history, transfer double-counting, negative/zero goals, leap-year heatmaps,
  reversed ranges and daylight-saving reminder times.
- API 37 emulator upgraded a version-3 database containing legacy expense and
  event fixtures; Room validated version 4 and preserved all original values.
- Created a Health category and saved 12.50 DH through the expense form. Changed
  a goal's basis to Recorded transfers and saved a 12.50 DH transfer. Database
  rows retained their exact cents and selected category/basis.
- Exercised 7/15/90-day views and custom date pickers, including reversed dates
  followed by November 20–30. The empty period showed zero totals.
- Tapped January 6, 2025 in activity and reached January 2025 in Calendar.
- A real scheduled event notification was delivered, then tapped to open the
  correct event. Enabling a future reminder created an active alarm; disabling
  it left no active Payflow alarm. Completed events remain editable.
- A keyed editor and guarded close callback keep a prior editor's exit from
  dismissing a different incoming event. Closing callbacks finish even if their
  composition is replaced, so a pending save is not silently dropped.
- Repeated the rapid close/incoming-event path with notification-style intent
  flags: the incoming event stayed open and its unchanged past reminder did
  not block editing. The final lazy heatmap also navigated to January 6, 2025;
  system Back returned to its saved activity view.
- Restored the emulator's original database and notification/alarm permissions.
  All five original table hashes match and both new savings tables are empty.

Private emulator evidence is under `app/build/outputs/insights-review/`.
These checks do not certify physical-phone battery behavior, actual reboot
delivery, full TalkBack speech, or a measured animation frame rate.
