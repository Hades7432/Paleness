# Paleness visual assets: one-time source synchronization

The playable 0.4.0-alpha JAR contains the approved models and textures, but
these binary assets were not committed with the earlier source code. This caused
missing purple-and-black models in the first real GitHub CI screenshots.

**One-time action:** upload the file named
`paleness-approved-visual-assets-0.4.zip` into this `ci/` folder
using GitHub's **Add file → Upload files** interface.

The file is extracted and SHA-256 checked by
`tools/prepare_visual_assets.py` before the visual CI build. It contains
103 verified PNG, model and blockstate files copied from the known playable
0.4.0-alpha JAR. This ensures screenshots test the approved appearance, not
placeholders. New visual source changes should be committed to normal
`src/main/resources/` paths in the future.

Check **Actions → Paleness Visual Test Lab** for screenshots and
`run/visual-review.md`. CI status FAIL means a genuine art loading problem
or a missing screenshot. A PASS never substitutes for reviewing the aesthetics.
