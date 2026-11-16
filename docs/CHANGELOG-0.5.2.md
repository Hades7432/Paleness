# Paleness 0.5.2-alpha — portal visual reconstruction

- Restores the bright cloudy white/lavender appearance of the 0.4 portal; 24 temporally interpolated 16x16 frames, cyclic interpolation.
- Corrects the 3x3 UV mapping: every tile draws its specific contiguous ninth of the SAME sprite, so tile edges meet precisely.
- Two full-bright translucent depth planes with camera-dependent parallax provide a view-responsive illusion. This is an approximation of the End-portal gaze effect, **not** Mojang's exact End portal shader.
- Animates END_ROD particles above the visible portal surface rather than beneath it.
- Preserves 0.5.1 stable horizontal frame, safe destination, cooldown, and the FOUR already approved metal textures.
- Adds GameTest coverage ensuring each portal tile resolves to the same center.
- Existing saves should be backed up before testing this alpha.

Automated QA proves registration and screenshot generation, not the user's preferred aesthetic: inspect the client screenshots and the loop manually.
