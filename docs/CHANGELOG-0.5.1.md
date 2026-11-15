# Paleness 0.5.1-alpha (visual and destination hotfix)

- Repainted all core pale-metal art in native 16×16 pixels.
- Pale Ore uses layered dark deepslate-style strata with shaded pale veins.
- Raw Pale Metal and Pale Metal Ingot use more readable silhouettes and vanilla-like lighting.
- Pale Metal Block has a polished silver finish with subtle curved dark seams.
- Reduced the detector's bulky head, reshaped it into a handheld remote/scanner and added a small HUD screen.
- The metal block inherits the exact vanilla Iron Block sound type (verified by GameTest).
- When entering the Paleness dimension, the return portal now seeks a reasonably flat **natural surface** and places only its 12 frame blocks and 9 portal tiles. No artificial 15×15 platform is created.
- Arrival tries natural solid ground 3 to 6 blocks away, never directly above the portal. If no safe location is found, only a single support square is placed.
- Retains the atlas-safe UV correction for animated horizontal portal textures from 0.5.
- Adds GameTest coverage for surface destination placement and safe landing.

## Testing / limitations
Build and tests are handled by GitHub Actions on `feature/paleness-0.5.1-polish`. A successful automated render does not replace manual checks with shaders/resource packs or actual player cross-dimension teleportation. Existing saves should be backed up before running alpha builds.
