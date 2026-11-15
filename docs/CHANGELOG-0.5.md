# Paleness 0.5.0-alpha — overhaul branch

**Branch:** `feature/paleness-0.5-overhaul`. The stable 0.4 branch remains untouched.

## Gameplay removed
- Stationary Paleness Analyzer, Paleness Condenser and ordinary Paleness Shards.
- Their recipe serializers, screens, block entities, item components and registrations.
- All recipe references and tool tags to the removed blocks.

## New features
- 3D handheld Paleness Detector; right click a block to show Paleness on the hotbar briefly.
- Pale Ore, much rarer than diamond deposits, generating from y=-60 to -46.
- Raw Pale Metal obtained from mining and smeltable into a Pale Metal Ingot.
- 9 ingots form a Pale Metal Block; one block unpacks into nine ingots.
- Pale Metal ingots can now feed the Infuser to produce super-pale quartz.
- The Super-Pale Crystal remains a single-use igniter: provisional recipe uses 4 pale ingots and one quartz.
- Horizontal 3×3 portal inside a 5×5 outline of 12 super-pale quartz blocks; corners are unnecessary.
- Improved portal return spawn safety, cooldown, stronger particles and 16-frame seamlessly-looping texture.

## Art
Approved 5-concept selection: A for ore, raw metal and scanner; D for ingot and metal block. All new surface and sprite textures are 16×16, scanner is a small 3D held item. Portal has 16 frames of 16×16.

## Known limitations
- A true End-Portal-style perspective-dependent depth shader is not part of this iteration yet; current horizontal portal plane uses the approved animated white pixel pattern.
- GameTest verifies recipes, portal structure, Paleness readings and infusing, but it does not simulate a complete player trip between dimensions.
- Automated software-OpenGL screenshots are not the same as gaming with a GPU and shaders; the textures still need a visual review.
- Existing worlds containing old blocks will have missing mappings after removal. **Back up saves and test in a new world first.** All 0.4 work remains on `backup/pre-overhaul-2026-11-15`.
- The provisional super-crystal recipe and infuser balance can be changed once the later soul-based resource system is designed.
