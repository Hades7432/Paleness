#!/usr/bin/env python3
"""Make a readable nearest-neighbor sprite atlas for Paleness art review."""
import io
from pathlib import Path
import zipfile
from PIL import Image, ImageDraw
jar=next(Path("build/libs").glob("paleness-*.jar"),None)
if jar is None: raise SystemExit("No Paleness build found")
parts=[]
with zipfile.ZipFile(jar) as z:
    for filename in sorted(z.namelist()):
        if not filename.startswith("assets/paleness/textures/") or not filename.endswith(".png"):
            continue
        try:
            im=Image.open(io.BytesIO(z.read(filename))).convert("RGBA")
            parts.append((filename.split("/")[-1].removesuffix(".png"),im.copy()))
        except Exception as e:
            print(f"Skipped {filename}: {e}")
width=8
cellw,cellh=124,133
rows=(len(parts)+width-1)//width
sheet=Image.new("RGB",(cellw*width,cellh*max(1,rows)),(30,31,36))
d=ImageDraw.Draw(sheet)
for n,(label,img) in enumerate(parts):
    x=(n%width)*cellw
    y=(n//width)*cellh
    scale=min(90/img.width,90/img.height)
    thumb=img.resize((max(1,int(img.width*scale)),max(1,int(img.height*scale))),Image.Resampling.NEAREST)
    block=Image.new("RGBA",(98,98),(85,87,94,255))
    block.alpha_composite(thumb,((98-thumb.width)//2,(98-thumb.height)//2))
    sheet.paste(block.convert("RGB"),(x+13,y+5))
    d.text((x+6,y+110),label[:18],fill=(245,245,245))
out=Path("run/screenshots/paleness-texture-sheet.png")
out.parent.mkdir(parents=True,exist_ok=True)
sheet.save(out)
print(f"[Paleness Visual Lab] Texture sheet: {len(parts)} PNGs -> {out}")
