# Calendar spacing and entry actions

App source: `44114d7`. This replaces the tabbed editor introduced in the previous
Huawei update. Earlier council scores remain historical and do not rate this revision.

## Layout and behavior

- Calendar header, day summary, totals and form content use matching 24dp side
  insets. Rate/expense pages use the same content margins. Removed the extra
  right gutter that made scrolling form content asymmetrical.
- The date grid retains full 48dp date rows and seven equal columns.
- The + button sits above the full-width Earned / Spent / Net row, rather than
  consuming the last column's width.
- Tapping + reveals two icon-only actions above its origin. Event and Shop income
  each open their own page without tabs. Icons have accessible names. Outside
  tap, Back or the rotated + closes the actions. Entrance is 220ms and exit 176ms.
- Removed the old day-details bottom sheet, its chevron and its clickable summary
  route. Existing events, delete actions and shop notes appear directly below the
  calendar, so removing the popup does not hide saved data.
- Added an original neutral calendar illustration when the selected day has no
  events. It adapts to the available space. Empty metadata is not repeated in
  the day summary.
- Adjacent-month dates and their worked markers have reduced emphasis; weekends
  remain readable and selectable. The current month keeps stronger contrast.
- Date cells share their press interaction with a circular, clipped ripple. The
  whole cell remains tappable, without drawing a square ripple over the grid.

## Checks

- `testDebugUnitTest lintDebug assembleDebug` passes. Nine unit tests pass and
  lint has no errors. Existing dependency/icon/resource warnings remain.
- Installed and exercised the updated interface on the connected Huawei Nova 5T.
  Inspected home, expanded actions and both separate editor pages. Back dismisses
  the expanded actions. The old popup and tab controls are absent from the source.
- Dragged March 9–20 and checked Room: all twelve dates were marked, then cleared
  again. The original work records matched afterward.
- Inspected screenshots showing all three financial totals, space above them for
  +, the empty-events illustration and equal editor insets. Private phone
  screenshots and the gesture receipt remain under
  `app/build/outputs/layout-review/`, outside version control.
- APK signature and installation are checked before delivery. Phone video capture
  and measured frame-rate verification remain unavailable.

The installable debug APK is copied to Downloads as `Payflow-44114d7.apk`.
