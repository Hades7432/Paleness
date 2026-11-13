# Paleness — 0.1.0-alpha

Minecraft **1.21.1** · **NeoForge >=21.1.219** · Java 21 · optional Create **6.0.10**.

Initial functional prototype for the approved [concept art](https://github.com/Hades7432/Paleness). The designs are *inspired by* the reference, not yet pixel-perfect. The images are concept art, not actual game screenshots.

## In game
- **Paleness Analyzer**: one inventory slot, Assess Paleness, 80-tick scan, non-destructive result.
- **Paleness Condenser**: four input slots, central output, 100-tick operation, consumes one item from each input, outputs one shard with a data-component value equal to their sum (up to 100000).
- **Sculk**: all vanilla sculk items/blocks have negative Paleness; pure sculk cannot yield a Paleness shard.
- **Texture scanning**: uses pixels from an available canonical PNG in a mod JAR, otherwise deterministic vanilla fallback values. Does not inspect installed resource packs.
- **Crafting**: Analyzer: iron corners, polished andesite edge centers, gold center; Condenser: polished andesite corners, iron top/bottom, diamonds left/right, quartz block center.
- UA/EN/RU translations; dedicated-server game logic.

## Build
GitHub Actions builds the actual JAR on push. Download from the successful workflow's **Artifacts** section. Local: `gradle build` with JDK 21.

## Known limitations (transparent)
- No in-game runtime test was possible in the authoring environment; CI compilation must succeed before installing.
- Dedicated vanilla servers may omit client textures; curated default values are used for them.
- The 3D shapes, white emissive aesthetic and GUI are a first pass; post-build screenshots are needed for visual refinement.
- Create is optional; advanced native Create kinetics/animations are not yet implemented.

Design target: the Ukrainian concept board chosen by the user.
