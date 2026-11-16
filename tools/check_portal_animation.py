#!/usr/bin/env python3
"""Regression gate for Paleness's real 16x16 animated bright-white gateway."""
from pathlib import Path
import json, statistics, sys
from PIL import Image

root=Path("src/main/resources/assets/paleness/textures/block")
texture=root/"paleness_portal.png"
metadata=root/"paleness_portal.png.mcmeta"
im=Image.open(texture).convert("RGB")
assert im.width == 16 and im.height == 16*24, f"Expected 24 frames of 16x16, got {im.size}"
meta=json.loads(metadata.read_text())["animation"]
assert meta["frametime"] == 1 and meta["interpolate"] is True
assert meta["frames"] == list(range(24)), "Invalid playback sequence"
frames=[im.crop((0,i*16,16,(i+1)*16)) for i in range(24)]
def average_change(a,b):
    pa=list(a.getdata());pb=list(b.getdata())
    return sum(abs(u-v) for x,y in zip(pa,pb) for u,v in zip(x,y))/(len(pa)*3)
changes=[average_change(frames[i], frames[(i+1)%24]) for i in range(24)]
assert max(changes)>0.5, "Portal is not really animated"
assert changes[-1] < max(changes)*1.20, (
    f"Visible animation loop discontinuity: wrap={changes[-1]:.2f}, max={max(changes):.2f}")
assert changes[-1] < 7.0, f"Portal loop cut too sharply ({changes[-1]:.2f})"
print(f"[Paleness Portal] 24x 16x16 frames, seam-wrapped animation: "
      f"last->first {changes[-1]:.2f}, median {statistics.median(changes):.2f}")
