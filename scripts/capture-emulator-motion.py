"""Record real Payflow launch or hierarchy motion on an already configured emulator."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("mode", choices=("launch", "hierarchy"))
parser.add_argument("--serial", default="emulator-5554")
parser.add_argument("--month", default="November 2026")
parser.add_argument("--output", type=Path, required=True)
args = parser.parse_args()
if not args.serial.startswith("emulator-"):
    parser.error("Use an emulator so review recording cannot interrupt a personal phone.")
adb = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platform-tools/adb.exe"
package = "com.dancingpumpkin.payflow"


def command(*parts):
    return subprocess.check_output([str(adb), "-s", args.serial, *parts])


def center(description):
    for _ in range(3):
        result = command("shell", "uiautomator", "dump", "/sdcard/payflow-motion.xml")
        if b"dumped to:" not in result:
            continue
        root = ET.fromstring(command("shell", "cat", "/sdcard/payflow-motion.xml"))
        nodes = [node for node in root.iter("node") if node.get("content-desc") == description
                 and node.get("bounds") != "[0,0][0,0]"]
        if len(nodes) != 1:
            raise RuntimeError(f"Expected one visible {description!r}; got {len(nodes)}")
        left, top, right, bottom = map(int, re.findall(r"\d+", nodes[0].get("bounds")))
        return str((left + right) // 2), str((top + bottom) // 2)
    raise RuntimeError("No fresh UI hierarchy returned")


if args.mode == "launch":
    command("shell", "am", "force-stop", package)
else:
    heading = center("Choose month and year")

remote = f"/sdcard/payflow-{args.mode}.mp4"
recorder = subprocess.Popen([str(adb), "-s", args.serial, "shell", "screenrecord",
                             "--time-limit", "10", "--bit-rate", "4000000", remote])
time.sleep(0.5)
if args.mode == "launch":
    command("shell", "am", "start", "-W", "-n", f"{package}/.MainActivity")
else:
    command("shell", "input", "tap", *heading)
    month = center(args.month)
    command("shell", "input", "tap", *month)
recorder.wait(timeout=20)
if recorder.returncode:
    raise RuntimeError(f"Screen recording failed: {recorder.returncode}")
args.output.parent.mkdir(parents=True, exist_ok=True)
command("pull", remote, str(args.output))
apk = command("shell", "pm", "path", package).decode().strip().removeprefix("package:")
metadata = {
    "package": package,
    "mode": args.mode,
    "serial": args.serial,
    "sdk": command("shell", "getprop", "ro.build.version.sdk").decode().strip(),
    "screen": command("shell", "wm", "size").decode().strip(),
    "density": command("shell", "wm", "density").decode().strip(),
    "font_scale": command("shell", "settings", "get", "system", "font_scale").decode().strip(),
    "animator_scale": command("shell", "settings", "get", "global", "animator_duration_scale").decode().strip(),
    "apk_sha256": command("shell", "sha256sum", apk).decode().split()[0],
    "recording_sha256": hashlib.sha256(args.output.read_bytes()).hexdigest(),
    "source_commit": subprocess.check_output(["git", "rev-parse", "HEAD"]).decode().strip(),
    "tracked_app_changes": bool(subprocess.check_output(["git", "diff", "HEAD", "--", "app"])),
}
args.output.with_suffix(".json").write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")
print(args.output.resolve())
