# Paleness portal — asset and rendering contract (0.5.2)

- 24 actual frames, 16x16 per frame, in one 16x384 PNG.
- Animation derived by periodic Catmull–Rom interpolation of the **original 0.4 white cloudy 4-frame portal**. New artwork is not an unrelated blurry swirl.
- Frames are displayed once each tick in an infinitely cyclic sequence, with vanilla sprite frame interpolation enabled.
- Rendering divides the **same image across the whole 3x3 portal**, rather than repeating it separately for each block.
- 2 translucent white layers with camera-relative global UV displacement make the surface visually responsive when viewing from different positions.
- Uses native block atlas and RenderType.translucent, not a dark End sky shader.
- This is a lightweight perspective/parallax **approximation**, not a reimplementation of Minecraft's proprietary multi-layer End Portal shader.
- No changes to the four approved Pale Metal texture hashes.
- A headless Minecraft screenshot cannot prove subjective parallax appearance; manual inspection in first- and third-person remains essential.

Build gate: `python tools/check_portal_animation.py`.
