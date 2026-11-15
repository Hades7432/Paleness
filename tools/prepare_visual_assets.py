#!/usr/bin/env python3
"""Restore exact, user-approved Paleness assets before a visual CI build."""
import hashlib, json
from pathlib import Path
from zipfile import ZipFile
bundle=Path("ci/paleness-approved-visual-assets-0.4.zip")
root=Path("src/main/resources")
if not bundle.exists():
    print("[Paleness Lab] Approved asset archive not committed yet; collecting diagnostics.")
else:
    with ZipFile(bundle) as z:
        assert z.testzip() is None, "Corrupt asset ZIP"
        files=json.loads(z.read("paleness_visual_manifest.json"))["files"]
        for name,metadata in files.items():
            p=Path(name)
            assert not p.is_absolute() and ".." not in p.parts and p.parts[:2]==("assets","paleness")
            blob=z.read(name)
            assert hashlib.sha256(blob).hexdigest()==metadata["sha256"], name
            dest=root / p
            dest.parent.mkdir(parents=True,exist_ok=True)
            dest.write_bytes(blob)
        print(f"[Paleness Lab] Restored {len(files)} approved visual assets")
