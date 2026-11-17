# Paleness — Minecraft 1.21.1 / NeoForge

**Development version:** 0.6.0-alpha  
**Active branch:** `feature/paleness-0.6-souls-alloy` (the default `main` branch is the older 0.4 prototype).  
**Toolchain:** JDK 21, NeoForge 21.1.x. Create 6.0.10 integration is a design target, not a verified gameplay feature.

Paleness is an evolving bright-white techno-magical Minecraft mod. Models and art follow a deliberately simple, pixelated Minecraft/Create visual language. Existing approved art must be preserved; new art follows the five-concept approval rule.

## Gameplay currently implemented in 0.6

- **Pale Ore** in the Overworld: two generation attempts per chunk, Y -60 through -46, veins up to three blocks, 25% chance to discard exposed ore.
- **Pale Metal**: raw material, ingot and storage block, with smelting/blasting and crafting recipes.
- **Paleness Detector**: right-click a block to read its calculated numeric Paleness. Sculk is the special negative exception.
- **Pale Alloy**: shapeless craft with four Pale Metal Ingots and four Iron Ingots. Right-click a vanilla Quartz Block to turn it into Pale Quartz, consuming one alloy in Survival.
- **Pale Quartz**: a bright frame material with silver borders that connect across neighboring blocks (64 block states and face-specific textures).
- **Soul Tagger**: reusable hand-held device using the approved D model. Right-click a living mob at close range or use a 16-block line-of-sight raycast; it glows for 200 ticks (10 seconds). If killed before the mark expires, the mob drops one Pale Soul instead of XP.
- **Pale Igniter**: eight Pale Souls surrounding Flint and Steel produce a single-use portal igniter.
- **Paleness Portal**: an optional-corner, horizontal 5×5 Pale Quartz frame with a 3×3 animated bright-white portal interior, based on approved animation B (32 frames, 16×16 per frame), leading to the Paleness dimension.
- English, Ukrainian and Russian localization.

**Removed in the 0.5/0.6 redesign:** the old Analyzer, Condenser, Paleness Shards, Infuser, Super-Pale Quartz, and Super-Pale Crystal. These still appear in the legacy `main` branch, but are *not* registered in 0.6.

## Building and checking

The approved resources are committed as `ci/paleness-0.6-approved-assets.zip`, with a checksum verified by the extraction script. The normal build and visual workflows restore them before compiling. Do not distribute a JAR if resource preflight fails.

GitHub Actions workflows on the active feature branch:
1. **Build Paleness**: restore approved assets, verify textures and 32-frame animation, compile, and upload the JAR.
2. **Verify Paleness world**: run NeoForge GameTests, including ore generation, recipes, quartz neighbor updates, soul mark/drop, horizontal portal lifecycle and safe-landing logic.
3. **Paleness Visual Test Lab**: launch the actual Minecraft client, capture seven screenshots and generate `visual-review.json`, `visual-review.md`, and `paleness-contact-sheet.png`.

For local development see [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) and [docs/CHANGELOG-0.6.md](docs/CHANGELOG-0.6.md).

## Install and world compatibility

Use the JAR from a successful **Build Paleness** workflow on the **0.6 feature branch**, not an older `main` build. Remove other Paleness JAR versions from the mods folder. **Back up your world first** or test in a fresh world: removed block/item registry entries from the earlier prototype may not migrate cleanly.

Automatic GameTest and software-OpenGL screenshot success does not prove that portal round-trip travel, art fidelity on a real GPU, interaction with Create contraptions, every camera angle, or performance are fully verified. Review the actual screenshots and test those behaviors in-game before treating a release as stable.
