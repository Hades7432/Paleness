# Paleness — Minecraft 1.21.1 NeoForge

**Status:** 0.4.0-alpha gameplay prototype; test lab under setup.
JDK 21, NeoForge 21.1.x, optional Create 6.0.10 integration via item capabilities.

Paleness is a techno-magical project centered around numeric whiteness /
Paleness values. All Paleness magic should have a distinct **bright white**
identity. Minecraft/Create-like pixels, deliberately readable silhouettes
and simple geometry are approved over excessive geometric complexity.

## Current implemented gameplay
- Analyzer: non-consuming one-slot scan.
- Condenser: four input slots make a numeric Paleness shard.
- Shard grades: five graphical tiers; sculk is negative.
- Infuser/Обліднювач: a Paleness shard saturates quartz;
  above 2.000 Paleness it becomes super-pale quartz.
- Super-pale crystal: shapeless special recipe from >= 2.000 Paleness
  of normal shards, one-time igniter.
- Portal: 4×5 super-pale quartz frame, 2×3 internal portal.
- Paleness dimension: bare stone terrain, separate biome, fixed daylight
  and no sun/moon/stars intended.
- English/Ukrainian/Russian languages.
- Baseline NeoForge GameTest server and real graphical CI test lab.

## Developer fast path
1. Add **approved existing art** into `ci/paleness-approved-visual-assets-0.4.zip`
   (see [ci/README.md](ci/README.md)). It is a **one-time bootstrap**
   because the earlier approved 0.4 playable JAR was assembled outside Git.
2. Every release build executes `tools/prepare_visual_assets.py`
   followed by `tools/validate_assets.py`. No more builds with missing
   purple/black textures are distributed as if complete.
3. GitHub Actions → **Verify Paleness world** runs GameTest server checks.
4. GitHub Actions → **Paleness Visual Test Lab** launches a real Minecraft
   client with full assets and software OpenGL, records 6 screenshots, a
   nearest-neighbor texture sheet and machine-readable QA.
5. Inspect `paleness-contact-sheet.png`, `visual-review.md` and
   `visual-review.json`. Automated PASS is **not** proof that art looks good.
6. For additional development consult [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

**Rule for new art:** generate five concepts for approval before adding
new mob, item or block models/textures. Previously approved assets can be
bug-fixed without replacing their style without approval.

## Installation
Remove previous Paleness JARs from `mods`. Use the artifact only from a
successful build with approved resources, and test portals in a new
world/save backup first. Gradle build (or an earlier CI artifact without
the art bundle) may be technically compiled but graphically incomplete.

## Limitations and next work
Graphical CI uses **software OpenGL**, which can differ from a user's GPU
in glass blending, fancy lighting, shadows and shaders. The test lab
does not inspect every camera angle automatically or test Create kinetics.
Full portal travel and return should be exercised with interactive
client tests; current server GameTest verifies registration and recipes.
