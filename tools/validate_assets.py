#!/usr/bin/env python3
"""Static gate: catch missing models/textures before any Minecraft client launch."""
from pathlib import Path
import json
import struct
import sys

base=Path("src/main/resources/assets/paleness")
errors=[]
models=list((base/"models").rglob("*.json"))
pngs=list((base/"textures").rglob("*.png"))

def complain(msg): errors.append(str(msg))
def path_for(ref,kind,extension):
    if not isinstance(ref,str) or ref.startswith("#"): return None
    namespace,sep,name=ref.partition(":")
    if sep and namespace!="paleness":return None
    return base/kind/((name if sep else namespace)+extension)
def require(ref,kind,extension,source):
    path=path_for(ref,kind,extension)
    if path is not None and not path.is_file():complain(f"{source}: missing {ref}")
def read(path):
    try:return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:complain(f"Invalid JSON {path}: {exc}");return {}
def models_in(node):
    if isinstance(node,list):
        for entry in node:yield from models_in(entry)
    elif isinstance(node,dict) and "model" in node:yield node["model"]

for bs in (base/"blockstates").glob("*.json"):
    data=read(bs)
    for val in data.get("variants",{}).values():
        for name in models_in(val):require(name,"models",".json",bs.name)
    for entry in data.get("multipart",[]):
        for name in models_in(entry.get("apply",{})):require(name,"models",".json",bs.name)

for model in models:
    data=read(model)
    if "parent" in data:require(data["parent"],"models",".json",model.name)
    tex=data.get("textures",{})
    for ref in tex.values():require(ref,"textures",".png",model.name)
    for over in data.get("overrides",[]):
        require(over.get("model"),"models",".json",model.name)
    for element in data.get("elements",[]):
        for face in element.get("faces",{}).values():
            ref=face.get("texture")
            if isinstance(ref,str) and ref.startswith("#") and ref[1:] not in tex:
                complain(f"{model.name}: undefined face {ref}")
for img in pngs:
    raw=img.read_bytes()
    if len(raw)<24 or raw[:8]!=b"\x89PNG\r\n\x1a\n":complain(f"Invalid PNG: {img}")
    else:
        w,h=struct.unpack(">II",raw[16:24])
        if min(w,h)<1 or max(w,h)>4096:complain(f"Bad PNG dimensions {w}x{h}: {img}")

for machine in ("paleness_analyzer","paleness_condenser","paleness_infuser"):
    for phase in range(5):
        if not (base/"models/block"/f"{machine}_phase_{phase}.json").is_file():
            complain(f"Missing approved 3D model: {machine}, phase {phase}")
for item in ("super_pale_crystal","paleness_shard","super_pale_quartz"):
    if not (base/"models/item"/f"{item}.json").is_file():
        complain(f"Missing approved item model: {item}")
if not (base/"textures/block/paleness_portal.png").is_file():
    complain("Paleness portal texture missing")
if len(models)<25 or len(pngs)<60:
    complain(f"Insufficient approved assets: {len(models)} models / {len(pngs)} textures")
Path("build").mkdir(exist_ok=True)
Path("build/paleness-assets-check.json").write_text(json.dumps(
    {"models":len(models),"textures":len(pngs),"errors":errors},indent=2),encoding="utf-8")
print(f"[Paleness Assets] {len(models)} JSON models, {len(pngs)} PNG textures, {len(errors)} issues")
for e in errors[:40]:print("::error::"+e,file=sys.stderr)
if errors:raise SystemExit(1)
