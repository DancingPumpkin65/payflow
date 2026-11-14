# Full checkup, 14 November 2026

Checked the original specification, subsequent interaction requests, Android
design skills and the two design/motion checklists against the app. UI code
remains the council-reviewed implementation. This pass adds four calculation
tests and explicit Android backup/transfer exclusions.

## Results

| Requirement | Result and evidence |
| --- | --- |
| Native, simple, local-only app | Kotlin, Compose, Room, ViewModel and Flow. No backend, authentication or network permission in the packaged APK. |
| Exact pay history | Six passing unit tests cover one rate, weekends, effective-date boundaries, removing work, expenses, edits/deletion, monthly filtering and dates before the first rate. Money uses integer cents. |
| Persistent records | In airplane mode, saved a new event, force-stopped and relaunched. Actual Room rows in all five tables matched before/after, including the new event. The temporary event was then deleted. [Result](screenshots/checkup/offline-persistence.json). |
| Explicit work interaction | Selecting dates leaves work rows unchanged; the Worked switch changes only the selected date. Test restores original rows. [Result](screenshots/checkup/selection-check.json). |
| Calendar hierarchy and swipe | Current month, adjacent dates, readable weekends, today/selection/worked states, horizontal paging and spatial month/year navigation remain in the reviewed implementation. Both directions for January/November have [recordings](screenshots/comparative-phone-editor/). |
| Events and shop income | Local records and notes are separate from personal earnings. Prior save/cancel/reopen checks retained; a new event was saved offline in this pass. |
| Compact form validation | At 360×640dp and 130% text, 1.001 DH shows an error and disables the actual save button. Scrolling exposes the full date control above the keyboard. Cancel leaves no expense draft in the database. [Screens](screenshots/checkup/overview.jpg). |
| Dark UI and restrained motion | One warm accent, dark Material surfaces, consistent icons and tabular figures. No bottom navigation. Movement uses 320ms entering, 256ms returning, and the selected tile as origin/destination. Loading reflects the local data read. |
| Backup policy | `allowBackup=false`, legacy `fullBackupContent=false`, and Android 12+ rules excluding every supported app-data domain from cloud backup and device transfer. Verified those attributes and rules in the built APK. |
| Build and lint | `testDebugUnitTest lintDebug assembleDebug` passes. Six tests, zero failures; lint has zero errors and 29 warnings, mostly dependency-version suggestions plus icon/resource recommendations. No dependency upgrade was required. |
| Installable APK | APK v2 signature verifies. Installed over the existing emulator build without losing records and cold-launched successfully. No entries in the emulator crash buffer after launch. |

Android's separate backup and device-transfer rules follow the
[official backup documentation](https://developer.android.com/identity/data/autobackup).
No actual cloud backup or device-transfer service was exercised.

## Differences and remaining limits

- The original document requested tap-to-toggle. The reviewed design deliberately
  uses tap-to-select and an explicit Worked switch to avoid accidental pay changes.
- Income sits below the month heading rather than in a separate top-right block.
  The selected Quiet ledger direction and council reviews use that composition.
- The image checklists are applied where useful. Native controls keep their
  platform feedback; decorative bounce, blur and stagger are not added everywhere.
- Small year-view dates, year controls that scroll out of view, and a persistent
  swipe hint remain recorded design tradeoffs. The optional monochrome launcher
  icon is not supplied. Overall council scores were 9.1, 9.0 and 9.3; this does not
  imply every category scores above 9 or every competitor is beaten.
- Huawei hardware, Android 10 runtime, reboot persistence, TalkBack speech,
  predictive Back and v1-to-v2 migration execution were not verified in this pass.
  Local emulator checks use API 37; the previous CI also passed API 35 launch.
- This is a signed debug APK for direct installation, not a Play Store release.

## APK

App source: `4fbb038`. Package `com.dancingpumpkin.payflow`, version 1.0,
minimum Android 8.0 / API 26. APK size 17,063,988 bytes.

File: `app/build/outputs/apk/debug/Payflow.apk`.

SHA-256: `cb8e13506a5f4fb0103dcc9743bcdcfd6f2aea5a44197393de3871d915416f49`.

Airplane mode and the normal 360×780dp / 115% text emulator profile were restored.
