# Paleness — development and QA protocol

## Design baseline
- Minecraft 1.21.1 / NeoForge / Java 21.
- Bright-white luminous Paleness, not recolored vanilla swirl textures.
- Low-complexity cube models matching Create. Prefer clear silhouettes.
- Portal: 16×16 per animation frame; particle emissions handled separately.
- Preserve approved simplified analyzer/condenser and basin-shaped infuser.
- For **new** textures/models, offer five alternatives before implementing.
- Native assets and source of truth belong in `src/main/resources/assets/paleness/`.

## Before writing a patch
1. Document behavior and edge cases (including min/max Paleness).
2. Add or extend GameTests of state, inventory and recipe behavior.
3. If art changes, get user approval and update committed sources.
4. Do not edit an already-built JAR as the sole release technique.
5. Ensure Create is optional and does not break standalone NeoForge.

## Mandatory gates
- **Java compilation** `gradle build`
- **Gameplay** `gradle runGameTestServer`
- **Resource preflight** `python tools/prepare_visual_assets.py`
  and `python tools/validate_assets.py`
- **Client launch** `Paleness Visual Test Lab` via GitHub Actions
- **Human visual assessment** of captured model angles, animation phases,
  inventories, white glow, 16×16 portal texture and all shard tiers.

## Diagnosis
- Source compiles but screenshot is black/pink: missing model or PNG.
  See `build/paleness-assets-check.json` and the `visual-review.md` report.
- GUI overdraw: capture GUI screenshot at 854×480 and compare text bounds.
- Z-fighting: test angled near and far views while camera moves.
- Transparency: watch for faces becoming transparent or popping.
- Client workflow stuck: inspect latest job logs; CI has a timeout and
  cancels older same-branch visual jobs.
- Shader lighting: not proven by software renderer; requires a later
  user's GPU screenshot as an acceptance step.

## Review questions
Can the player distinguish the machines quickly? Are details too dense
for Minecraft/Create? Is the Paleness white visually clean? Are glow
and particle effects subtle rather than noisy? Are all model states
recognizable and readable with ambient lighting?

## Current scope of automatic assurance
**Checked:** registered world type and biome, named recipes, compiled code,
resource paths, file integrity, and screenshots once art is uploaded.
**Not yet automatic:** successful round-trip player teleportation, exact
in-game Create contraption behavior, full animation timing, artistic
quality scoring or model performance on the user's graphics card.

Keep these limitations visible in releases and avoid overstating success.
