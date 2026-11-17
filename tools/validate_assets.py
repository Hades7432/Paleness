#!/usr/bin/env python3
"""Validate Paleness resource references and 0.6 connected-block state coverage.
The 0.6 binary artwork is shipped as a separate approved asset pack until
all exact PNGs are checked into the repository; missing PNGs are reported.
"""
from pathlib import Path
import json,struct
root=Path("src/main/resources/assets/paleness")
models=list((root/"models").rglob("*.json"))
errors=[]; warnings=[]
for p in (root/"blockstates").glob("*.json"):
    try:data=json.loads(p.read_text())
    except Exception as e:errors.append(f"{p}: {e}");continue
    for state,entry in data.get("variants",{}).items():
        if isinstance(entry,dict):
            model=entry.get("model","")
            if model.startswith("paleness:"):
                target=root/"models"/(model.split(":",1)[1]+".json")
                if not target.exists():errors.append(f"{p}: missing {target}")
for p in models:
    try:d=json.loads(p.read_text())
    except Exception as e:errors.append(f"{p}: {e}");continue
    for value in d.get("textures",{}).values():
        if not value.startswith("paleness:"):continue
        t=root/"textures"/(value.split(":",1)[1]+".png")
        if not t.exists():warnings.append(f"{p}: missing approved PNG {t}")
    for element in d.get("elements",[]):
        for face in element.get("faces",{}).values():
            tex=face.get("texture","")
            if tex.startswith("#") and tex[1:] not in d.get("textures",{}):
                errors.append(f"{p}: undefined texture {tex}")
q=json.loads((root/"blockstates/pale_quartz.json").read_text())
if len(q.get("variants",{}))!=64:errors.append("Pale Quartz needs exactly 64 connection states")
print(f"[Paleness 0.6 Assets] {len(models)} models; errors={len(errors)}; pending PNG files={len(warnings)}")
for s in errors:print("ERROR:",s)
for s in warnings[:4]:print("NOTICE:",s)
if errors:raise SystemExit(1)
