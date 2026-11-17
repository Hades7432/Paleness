# Paleness 0.6.0-alpha — New progression

- Approved Pale Alloy item from four Pale Metal Ingots and four Iron Ingots.
- Right-click a vanilla Quartz Block with Pale Alloy to create Pale Quartz (consumes one Alloy in Survival).
- Pale Quartz joins its silver border across adjacent blocks; 64 neighborhood states and face-specific 16x16 patterns.
- The Soul Tagger (Soul Brand device) is reusable, marks living mobs for 200 ticks (10s) with Glowing. Kills before expiry drop one Pale Soul and no experience.
- Soul Tagger is craftable with Pale Alloy, Iron, Redstone, Glass Pane and Stick.
- Eight Pale Souls around Flint and Steel make a single-use Pale Igniter, used to light the horizontal 5x5 portal frame.
- Super-Pale Crystal, Super-Pale Quartz and the obsolete Infuser machine are no longer registered or craftable.
- Pale Ore: 2 attempts per chunk, Y=-60 to -46, veins up to 3, 25% air-discard (75% open-air chance).
- Portal B animation (32 frames), approved 16x16 resources, and the editable Soul Tagger D Blockbench model are SHA256-locked in ci/paleness-0.6-approved-assets.zip and automatically extracted for builds.
- Back up existing worlds: removing registered blocks/items can leave old block IDs unmapped.

Development branch: feature/paleness-0.6-souls-alloy. NeoForge 21.1 / Minecraft 1.21.1.

- Soul Tagger now performs a 16-block line-of-sight raycast when used in the air; point-blank right-click remains supported.
- GameTest additionally verifies that marked mob death actually drops exactly one Pale Soul.
- Visual QA screenshot timing tightened to finish seven captures before runtime shuts down.

- Improved first-person scale of Soul Tagger D without changing its approved Blockbench cuboids or PNG textures; moved portal Visual Test camera so it captures the 3x3 surface instead of the floor.
