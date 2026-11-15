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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Light-filled horizontal membrane, with a small camera-dependent parallax.
 * Uses the animated 16-frame atlas sprite instead of hardcoded Nether swirls.
 */
public final class PalePortalRenderer implements BlockEntityRenderer<PalePortalEntity> {
    private static final ResourceLocation SPRITE =
        ResourceLocation.fromNamespaceAndPath(Paleness.ID,"block/paleness_portal");
    public PalePortalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(PalePortalEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int overlay) {
        if(be.getLevel()==null)return;
        TextureAtlasSprite sprite=Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(SPRITE);
        Vec3 camera=Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double t=(be.getLevel().getGameTime()+partialTick)*0.06;
        // Deliberately within the sprite's 16-pixel UV area to prevent atlas bleeding.
        float parallaxX=(float)(Math.sin(t+camera.x*0.08)*0.7);
        float parallaxZ=(float)(Math.cos(t*0.9+camera.z*0.08)*0.7);
        // TextureAtlasSprite UVs are normalized: never pass 16px coordinates
        // directly to getU/getV or the quad samples unrelated sprites in the atlas.
        float u0=sprite.getU0()+(sprite.getU1()-sprite.getU0())*(1.5f+parallaxX)/16f;
        float u1=sprite.getU0()+(sprite.getU1()-sprite.getU0())*(14.5f+parallaxX)/16f;
        float v0=sprite.getV0()+(sprite.getV1()-sprite.getV0())*(1.5f+parallaxZ)/16f;
        float v1=sprite.getV0()+(sprite.getV1()-sprite.getV0())*(14.5f+parallaxZ)/16f;
        VertexConsumer c=buffers.getBuffer(RenderType.translucent());
        Matrix4f m=poseStack.last().pose();
        float y=0.76f;
        quad(c,m,y,u0,u1,v0,v1,false);
        quad(c,m,y-0.002f,u0,u1,v0,v1,true);
    }

    private static void v(VertexConsumer c,Matrix4f m,float x,float y,float z,
                          float u,float v,float normal) {
        c.addVertex(m,x,y,z).setColor(255,255,255,250).setUv(u,v)
            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT)
            .setNormal(0,normal,0);
    }
    private static void quad(VertexConsumer c,Matrix4f m,float y,
                             float u0,float u1,float v0,float v1,boolean underside) {
        if(!underside) {
            v(c,m,0,y,0,u0,v0,1);
            v(c,m,0,y,1,u0,v1,1);
            v(c,m,1,y,1,u1,v1,1);
            v(c,m,1,y,0,u1,v0,1);
        } else {
            v(c,m,1,y,0,u1,v0,-1);
            v(c,m,1,y,1,u1,v1,-1);
            v(c,m,0,y,1,u0,v1,-1);
            v(c,m,0,y,0,u0,v0,-1);
        }
    }
}
