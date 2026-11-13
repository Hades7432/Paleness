#!/usr/bin/env python3
"""Bake Paleness into integers from original Minecraft texture pixels.
No texture files are redistributed. Values are generated from Mojang's client jar.
Requires Pillow; run from the repository root.
"""
import io
import json
import math
import pathlib
import urllib.request
import zipfile
from collections import defaultdict
from PIL import Image

def fetch_json(url):
    with urllib.request.urlopen(url, timeout=45) as response:
        return json.load(response)

def get_client_jar():
    manifest = fetch_json("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
    item = next(v for v in manifest["versions"] if v["id"] == "1.21.1")
    meta = fetch_json(item["url"])
    with urllib.request.urlopen(meta["downloads"]["client"]["url"], timeout=120) as r:
        return r.read()

def value_from_png(raw):
    image = Image.open(io.BytesIO(raw)).convert("RGBA")
    pixels = image.getdata()
    total = 0.0
    alpha_sum = 0.0
    for r, g, b, a in pixels:
        af = a/255.0
        if af < 0.05:
            continue
        lo, hi = min(r, g, b), max(r, g, b)
        white = max(0.0, (lo-5)/250.0)**2.8 * (1.0-(hi-lo)/255.0)
        total += white*af
        alpha_sum += af
    return round(1000*total/alpha_sum) if alpha_sum else 0

SUFFIXES = ("_side", "_top", "_bottom", "_front", "_back", "_end",
            "_north", "_south", "_east", "_west", "_left", "_right",
            "_inner", "_outer", "_on", "_off", "_lit")

def name_variants(name):
    names = {name}
    for suffix in SUFFIXES:
        if name.endswith(suffix):
            names.add(name[:-len(suffix)])
    return names

def main():
    groups = defaultdict(list)
    with zipfile.ZipFile(io.BytesIO(get_client_jar())) as z:
        for name in z.namelist():
            if not name.endswith(".png"):
                continue
            prefix = "assets/minecraft/textures/"
            if not name.startswith(prefix):
                continue
            short = name[len(prefix):]
            if not (short.startswith("block/") or short.startswith("item/")):
                continue
            texture_name = pathlib.PurePosixPath(short).stem
            try:
                score = value_from_png(z.read(name))
            except Exception:
                continue
            for key in name_variants(texture_name):
                groups["minecraft:"+key].append(score)
    values = {key: round(sum(arr)/len(arr)) for key, arr in sorted(groups.items()) if arr}
    target = pathlib.Path("src/generated/resources/assets/paleness/paleness_index.json")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(values, ensure_ascii=False, separators=(",",":")), encoding="utf-8")
    print(f"Baked {len(values)} canonical texture-based Paleness values into {target}")

if __name__ == "__main__":
    main()
