#!/usr/bin/env python3
"""Install byte-verified, user-approved Paleness 0.6 textures & Blockbench source.

Assets are stored as a single original ZIP in ci/ to guarantee exact image bytes.
This tool is deterministic and safe to run in CI or manually before Gradle.
"""
from pathlib import Path
import hashlib,zipfile,struct,json,sys
archive=Path("ci/paleness-0.6-approved-assets.zip")
expected="f086d5a908814cc78b62833efe8ac1b25e6a02ecdeaee41c092159ce361e9605"
if not archive.is_file():raise SystemExit("Missing approved Paleness 0.6 asset pack in ci/")
actual=hashlib.sha256(archive.read_bytes()).hexdigest()
if actual != expected:raise SystemExit(f"Approved 0.6 asset pack SHA256 mismatch: {actual}")
with zipfile.ZipFile(archive) as z:
    if z.testzip():raise SystemExit("Approved 0.6 ZIP corrupted")
    names=z.namelist()
    count=0
    for name in names:
        path=Path(name)
        if path.is_absolute() or ".." in path.parts:raise SystemExit(f"Unsafe asset path: {name}")
        if name.startswith("assets/paleness/"):
            dest=Path("src/main/resources")/path
        elif name=="blockbench/soul_tagger_D.bbmodel":
            dest=Path("art")/path
        else:raise SystemExit(f"Unexpected file in approved pack: {name}")
        blob=z.read(name)
        if name.endswith(".png"):
            if not blob.startswith(b"\x89PNG\r\n\x1a\n"):raise SystemExit(f"Bad PNG signature: {name}")
            w,h=struct.unpack(">II",blob[16:24])
            if (w,h)!=(16,512) and (w,h)!=(16,16):
                raise SystemExit(f"Non-vanilla pixel size {name}: {w}x{h}")
        dest.parent.mkdir(parents=True,exist_ok=True)
        dest.write_bytes(blob)
        count+=1
print(f"[Paleness 0.6] Installed {count} byte-verified approved art files.")
