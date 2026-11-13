# Paleness — 0.2.0-alpha

Minecraft **1.21.1**, **NeoForge 21.1.x**, Java 21. Compatible with Create 6.0.10 for item logistics.

## Features implemented in the Java sources

- **Paleness Analyzer:** one input, 4-second scan without consuming the item, status/result GUI.
- **Paleness Condenser:** four ingredient slots, 5-second condensation, one data-bearing Paleness Shard.
- **Paleness** based on canonical texture-pixel whiteness index, with special negative values for sculk.
- **Five machine animation phases:** `MachineBlock.PHASE = 0..4` advances during operation, changing in-world block models.
- Machines face the player on placement, change emitted light level during operation, and preserve their inventories.
- **Five shard tiers:** item model overrides at 0.2, 0.4, 0.6 and 0.8 based on `paleness_value / 4000`.
- Ukrainian, Russian and English translations, creative tab, recipes, basic Create item capability interop.
- Analyzer progress bar no longer obscures the status label.

## Visual assets — important

The **complete 0.2.0-alpha JAR** distributed through the conversation contains the approved v0.2 model/texture pack. **This GitHub source branch currently includes the Java integration but does not yet include the 0.2 binary visuals themselves**, because they were authored as a separate package. Therefore, **do not use the GitHub Actions artifact alone as the complete visual release**.

To rebuild the visually complete release from this repository, first extract the separately distributed `Paleness-v0.2.0-Source-Assets.zip` into the repository root (the ZIP has `src/main/resources/assets/...` and `art/...`). Then run `gradle build` with JDK 21.

## Notes

Visual phases are **discrete** model changes; they are not smooth skeletal animation or shader-based bloom. The source model assets are editable in Blockbench. In-game Minecraft client testing is still required after CI compilation. Create kinetics and advanced moving contraption support are not yet implemented.

The recommended test is: replace the old 0.1 mod JAR with the distributed 0.2 JAR, place each machine, scan an item, then condense four ingredients into a shard. Check the five model phases and shard tier differences.
