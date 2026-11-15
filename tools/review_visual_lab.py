#!/usr/bin/env python3
"""Visual QA: log/texture loading checks, screenshots, contact sheet."""
from pathlib import Path
from PIL import Image,ImageDraw
import re
root=Path("run")
screens=sorted((root/"screenshots").glob("paleness-*.png"))
log=(root/"logs"/"latest.log")
lines=log.read_text(errors="replace").splitlines() if log.exists() else []
bad=re.compile(r"Unable to load model|Missing textures in model|missing model for variant|Exception loading blockstate definition",re.I)
errors=[line.strip() for line in lines if bad.search(line) and "paleness:" in line]
metrics=[]
thumbs=[]
for file in screens:
    with Image.open(file) as image:
        img=image.convert("RGB")
        n=sum(r>175 and b>160 and g<90 for r,g,b in img.getdata())
        ratio=n/(img.width*img.height)
        metrics.append((file.name,img.size,ratio))
        img.thumbnail((640,360))
        tile=Image.new("RGB",(660,390),(26,28,34))
        tile.paste(img,((660-img.width)//2,0))
        ImageDraw.Draw(tile).text((15,366),file.name,fill="white")
        thumbs.append(tile)
if thumbs:
    contact=Image.new("RGB",(660,390*len(thumbs)),(24,25,30))
    for i,tile in enumerate(thumbs): contact.paste(tile,(0,390*i))
    contact.save(root/"screenshots"/"paleness-contact-sheet.png")
failed=bool(errors or not screens or any(item[2]>.0002 for item in metrics))
report=["# Paleness Visual Lab", "", "Automated checks: **"+("FAIL" if failed else "PASS")+"**",
        "", "## Screenshots"]
for name,size,ratio in metrics:
    report.append(f"- {name}: {size[0]}x{size[1]}, magenta-pixel fraction {ratio:.3%}")
report.extend(["","## Missing models/textures",f"{len(errors)} warnings"])
report.extend("- "+e[:320] for e in errors[:60])
report.extend(["","## Human art review","- Geometry, proportions, composition vs approved references",
               "- 16x16 material readability and consistent Create-like style",
               "- Bright white Paleness, particles and coherent glow",
               "- Z-fighting, translucency, GUI and five animation phases",
               "- Screenshot review remains necessary: numeric metrics cannot judge beauty"])
(root/"visual-review.md").write_text("\n".join(report)+"\n",encoding="utf-8")
print("\n".join(report[:15]))
raise SystemExit(1 if failed else 0)
