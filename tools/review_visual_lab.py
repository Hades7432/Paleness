#!/usr/bin/env python3
"""Machine-readable and human-readable QA for a REAL Minecraft screenshot run."""
from pathlib import Path
from PIL import Image,ImageDraw
import re
import json

root=Path("run")
shots=root/"screenshots"
shots.mkdir(parents=True,exist_ok=True)
expected=[
  "paleness-01-gallery.png",
  "paleness-02-analyzer-idle.png",
  "paleness-03-condenser-idle.png",
  "paleness-04-infuser-idle.png",
  "paleness-05-infuser-active.png",
  "paleness-06-analyzer-gui.png"
]
log=(root/"logs/latest.log")
lines=log.read_text(errors="replace").splitlines() if log.exists() else []
bad=re.compile(r"Unable to load model|Missing textures in model|missing model for variant|Exception loading blockstate definition|FileNotFoundException: paleness:",re.I)
warnings=list(dict.fromkeys(line.strip() for line in lines if bad.search(line) and "paleness:" in line))
missing=[name for name in expected if not (shots/name).is_file()]
reports=[]
tiles=[]
for name in expected:
    p=shots/name
    if not p.is_file():continue
    with Image.open(p) as frame:
        im=frame.convert("RGB")
        pixels=list(im.getdata())
        magenta=sum(r>175 and b>160 and g<90 for r,g,b in pixels)
        fraction=magenta/len(pixels)
        reports.append({"file":name,"size":[im.width,im.height],"magenta_ratio":round(fraction,6)})
        im.thumbnail((640,360),Image.Resampling.LANCZOS)
        tile=Image.new("RGB",(664,394),(26,28,34))
        tile.paste(im,((664-im.width)//2,2))
        ImageDraw.Draw(tile).text((12,370),name,fill=(245,245,245))
        tiles.append(tile)
if tiles:
    sheet=Image.new("RGB",(664,394*len(tiles)),(26,28,34))
    for i,tile in enumerate(tiles):sheet.paste(tile,(0,i*394))
    sheet.save(shots/"paleness-contact-sheet.png")
has_magenta=any(item["magenta_ratio"]>0.0002 for item in reports)
passed=not (warnings or missing or has_magenta)
summary={"result":"PASS" if passed else "FAIL","expected_screenshots":len(expected),
    "real_screenshots":len(reports),"missing_screenshots":missing,
    "model_warnings":warnings[:100],"screenshots":reports}
(root/"visual-review.json").write_text(json.dumps(summary,indent=2),encoding="utf-8")
out=["# Paleness Visual QA", "",f"Status: **{summary['result']}**",
     f"Real game screenshots: **{len(reports)}/{len(expected)}**", "",
     "## Game captures"]
for item in reports:
    out.append(f"- {item['file']}: {item['size'][0]}×{item['size'][1]}, "
               f"missing-texture-magenta {item['magenta_ratio']:.2%}")
out.extend(["","## Missing captures"]+["- "+x for x in missing])
out.extend(["","## Model loading warnings"]+["- "+x[:320] for x in warnings[:80]])
out.extend(["","## Artist review — still requires actual image inspection",
 "- Compare silhouettes with the approved Paleness references",
 "- Check front/back/angled geometry, proportions and Minecraft/Create fit",
 "- Compare texture resolution; the portal must use 16×16 pixels per frame",
 "- Look for z-fighting, transparent overlap, jagged crystal silhouettes",
 "- Check emissive-looking WHITE Paleness, particles and phase progression",
 "- Inspect GUI label alignment, localization and item tier transitions",
 "- Automated PASS is technical only; it does NOT certify aesthetics"])
(root/"visual-review.md").write_text("\n".join(out)+"\n",encoding="utf-8")
print("\n".join(out[:20]))
if not passed:raise SystemExit(1)
