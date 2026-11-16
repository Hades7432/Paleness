package ua.hades7432.paleness;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * One 3x3 world-anchored animated WHITE portal, even though the game stores it
 * as nine separate blocks. All nine renderers use the same portal center and
 * absolute camera position, so their UVs meet without tile seams.
 *
 * There are two depth layers. The upper layer shifts subtly with viewing
 * angle to approximate End-portal parallax without reusing the End's dark,
 * starry shader. The underlying sprite preserves Paleness's bright artwork.
 */
public final class PalePortalRenderer implements BlockEntityRenderer<PalePortalEntity> {
    private static final ResourceLocation SPRITE =
        ResourceLocation.fromNamespaceAndPath(Paleness.ID, "block/paleness_portal");
    private static final float UV_MARGIN = 0.018f;
    public PalePortalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PalePortalEntity entity, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        if (entity.getLevel() == null) return;
        BlockPos tile = entity.getBlockPos();
        BlockPos center = PalePortalLogic.centerOf(entity.getLevel(), tile);
        if (center == null) return; // Never draw orphaned portal remnants.

        // The nine cells share exactly ONE UV interval. No per-block repeats
        // and no tile-specific temporal phase: all adjacent edges align.
        int cellX = tile.getX() - center.getX() + 1;
        int cellZ = tile.getZ() - center.getZ() + 1;
        if (cellX < 0 || cellX > 2 || cellZ < 0 || cellZ > 2) return;

        TextureAtlasSprite sprite = Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SPRITE);
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double distanceY = Math.max(2.0, Math.abs(camera.y - (center.getY() + 0.77)));
        float gazeX = Mth.clamp((float)((camera.x - (center.getX() + 0.5)) / distanceY), -1, 1);
        float gazeZ = Mth.clamp((float)((camera.z - (center.getZ() + 0.5)) / distanceY), -1, 1);
        float t = (entity.getLevel().getGameTime() + partialTick) * 0.055f;

        VertexConsumer c = buffers.getBuffer(RenderType.translucent());
        Matrix4f matrix = pose.last().pose();

        // Base is a white continuous fabric, not nine independent sprites.
        layer(c, matrix, sprite, cellX, cellZ, 0.770f, 252, 0, 0);
        // A soft shallower layer catches the player's gaze as the camera moves;
        // the small UV offset is global, keeping the entire 3x3 membrane seamless.
        float driftX = gazeX * 0.022f + (float)Math.sin(t) * 0.005f;
        float driftZ = gazeZ * 0.022f + (float)Math.cos(t) * 0.005f;
        layer(c, matrix, sprite, cellX, cellZ, 0.792f, 78, driftX, driftZ);
    }

    private static float uv(TextureAtlasSprite sprite, float normalized, boolean horizontal) {
        float safe = Mth.clamp(normalized, UV_MARGIN, 1f - UV_MARGIN);
        return horizontal ? Mth.lerp(safe, sprite.getU0(), sprite.getU1())
                          : Mth.lerp(safe, sprite.getV0(), sprite.getV1());
    }

    private static void layer(VertexConsumer c, Matrix4f matrix, TextureAtlasSprite sprite,
                              int x, int z, float height, int alpha,
                              float shiftU, float shiftV) {
        // Single continuous 0..1 coordinate system across ALL 9 portal cells.
        float loU = uv(sprite, 0.03f + (x / 3f) * 0.94f + shiftU, true);
        float hiU = uv(sprite, 0.03f + ((x + 1) / 3f) * 0.94f + shiftU, true);
        float loV = uv(sprite, 0.03f + (z / 3f) * 0.94f + shiftV, false);
        float hiV = uv(sprite, 0.03f + ((z + 1) / 3f) * 0.94f + shiftV, false);

        vertex(c, matrix, 0, height, 0, loU, loV, alpha, 1);
        vertex(c, matrix, 0, height, 1, loU, hiV, alpha, 1);
        vertex(c, matrix, 1, height, 1, hiU, hiV, alpha, 1);
        vertex(c, matrix, 1, height, 0, hiU, loV, alpha, 1);

        vertex(c, matrix, 1, height - 0.002f, 0, hiU, loV, alpha, -1);
        vertex(c, matrix, 1, height - 0.002f, 1, hiU, hiV, alpha, -1);
        vertex(c, matrix, 0, height - 0.002f, 1, loU, hiV, alpha, -1);
        vertex(c, matrix, 0, height - 0.002f, 0, loU, loV, alpha, -1);
    }

    private static void vertex(VertexConsumer c, Matrix4f m, float x, float y, float z,
                               float u, float v, int alpha, float normalY) {
        c.addVertex(m, x, y, z).setColor(255, 255, 255, alpha)
         .setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
         .setLight(LightTexture.FULL_BRIGHT).setNormal(0, normalY, 0);
    }
}
