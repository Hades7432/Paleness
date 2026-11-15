#!/usr/bin/env python3
"""Restore the exact approved 0.4 visuals until they are all checked into Git."""
from pathlib import Path
from zipfile import ZipFile, BadZipFile
import hashlib
import json
import sys

root=Path("src/main/resources")
assets=root/"assets/paleness"
bundle=Path("ci/paleness-approved-visual-assets-0.4.zip")
# Future-proofing: once the approved visuals are checked in as regular files,
# they remain the source of truth; do not overwrite newer revisions with old art.
sentinels=[
  assets/"models/block/paleness_infuser_phase_0.json",
  assets/"models/block/paleness_analyzer_phase_4.json",
  assets/"models/block/paleness_condenser_phase_4.json",
  assets/"textures/block/paleness_portal.png",
  assets/"models/item/super_pale_crystal.json",
]
if all(p.exists() for p in sentinels):
    print("[Paleness Assets] Found source-controlled models; skipping legacy bundle")
    raise SystemExit(0)
if not bundle.is_file():
    print("::error title=Approved art not yet in repository::Upload "
          "paleness-approved-visual-assets-0.4.zip to ci/ in GitHub. "
          "Until then, builds with reliable graphics are deliberately blocked.",
          file=sys.stderr)
    raise SystemExit(2)
try:
    with ZipFile(bundle) as archive:
        if archive.testzip() is not None:
            raise ValueError("Corrupt asset archive")
        manifest=json.loads(archive.read("paleness_visual_manifest.json"))
        files=manifest.get("files",{})
        if len(files)!=103:
            raise ValueError(f"Expected 103 approved files, got {len(files)}")
        for name,meta in sorted(files.items()):
            sub=Path(name)
            if sub.is_absolute() or ".." in sub.parts or sub.parts[:2]!=("assets","paleness"):
                raise ValueError(f"Unsafe path: {name}")
            blob=archive.read(name)
            if hashlib.sha256(blob).hexdigest()!=meta["sha256"]:
                raise ValueError(f"SHA-256 mismatch: {name}")
            dest=root/sub
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(blob)
except (BadZipFile,KeyError,ValueError) as ex:
    print(f"::error title=Invalid approved asset archive::{ex}",file=sys.stderr)
    raise SystemExit(2)
print(f"[Paleness Assets] Restored {len(files)} approved files; SHA-256 verified")
