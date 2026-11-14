"""Verify calendar inspection cannot edit work records on the review emulator.

Run with the month calendar visible and pass two dates from its visible grid.
The switch test restores its original value, including when an assertion fails.
"""

import argparse
from contextlib import closing
from datetime import date
import json
import os
from pathlib import Path
import re
import sqlite3
import subprocess
import tempfile
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("first", type=date.fromisoformat)
parser.add_argument("second", type=date.fromisoformat)
parser.add_argument("--serial", default="emulator-5554")
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
if not args.serial.startswith("emulator-"):
    parser.error("This check is limited to an emulator.")
adb = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platform-tools/adb.exe"
package = "com.dancingpumpkin.payflow"


def command(*parts):
    return subprocess.check_output([str(adb), "-s", args.serial, *parts])


def tap(description):
    for _ in range(3):
        result = command("shell", "uiautomator", "dump", "/sdcard/payflow-check.xml")
        if b"dumped to:" not in result:
            continue
        root = ET.fromstring(command("shell", "cat", "/sdcard/payflow-check.xml"))
        nodes = [node for node in root.iter("node") if node.get("content-desc") == description
                 and node.get("bounds") != "[0,0][0,0]"]
        if len(nodes) != 1:
            raise RuntimeError(f"Expected one visible {description!r}; got {len(nodes)}")
        left, top, right, bottom = map(int, re.findall(r"\d+", nodes[0].get("bounds")))
        command("shell", "input", "tap", str((left + right) // 2), str((top + bottom) // 2))
        # Wait for a new idle hierarchy before reading the database.
        command("shell", "uiautomator", "dump", "/sdcard/payflow-check.xml")
        return
    raise RuntimeError("No fresh UI hierarchy")


def work_days():
    with tempfile.TemporaryDirectory(prefix="payflow-selection-") as directory:
        db = Path(directory) / "payflow.db"
        for suffix in ("", "-wal"):
            result = subprocess.run([str(adb), "-s", args.serial, "exec-out", "run-as",
                                     package, "cat", f"databases/payflow.db{suffix}"], capture_output=True)
            if result.returncode and not suffix:
                raise RuntimeError(result.stderr.decode())
            if not result.returncode:
                Path(str(db) + suffix).write_bytes(result.stdout)
        with closing(sqlite3.connect(db)) as connection:
            return {row[0] for row in connection.execute("SELECT date FROM work_days")}


def full_date(value):
    return value.strftime("%A, ") + str(value.day) + value.strftime(" %B %Y")


before = work_days()
tap(full_date(args.first))
assert work_days() == before, "Selecting the first date changed work records"
tap(full_date(args.second))
assert work_days() == before, "Selecting another date changed work records"
switch = f"Worked on {args.second.day}" + args.second.strftime(" %B %Y")
try:
    tap(switch)
    assert work_days() == before.symmetric_difference({args.second.isoformat()}), "Switch edited an unexpected date"
finally:
    if work_days() != before:
        tap(switch)
assert work_days() == before, "Work records were not restored"
result = {"serial": args.serial, "dates": [str(args.first), str(args.second)],
          "selection_did_not_mutate": True, "switch_changed_only_selected_date": True,
          "original_records_restored": True}
args.output.parent.mkdir(parents=True, exist_ok=True)
args.output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
print(json.dumps(result))
