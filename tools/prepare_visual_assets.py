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
            # Old analyzer/condenser and shard art must not leak into 0.5.
            if any(token in name for token in ("paleness_analyzer","paleness_condenser","paleness_shard")):
                continue
            dest=root/sub
            if not dest.exists():  # Newly approved source-controlled art has priority.
                dest.parent.mkdir(parents=True,exist_ok=True)
                dest.write_bytes(blob)
except (BadZipFile,KeyError,ValueError) as ex:
    print(f"::error title=Invalid approved asset archive::{ex}",file=sys.stderr)
    raise SystemExit(2)
print(f"[Paleness Assets] Restored {len(files)} approved files; SHA-256 verified")


# New approved 0.6 resources intentionally override previous 0.4 fallback art.
import subprocess
subprocess.run([sys.executable,"tools/unpack_approved_06_assets.py"],check=True)
