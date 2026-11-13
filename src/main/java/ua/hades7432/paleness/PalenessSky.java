package ua.hades7432.paleness;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

@EventBusSubscriber(modid=Paleness.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class PalenessSky {
    @SubscribeEvent public static void register(RegisterDimensionSpecialEffectsEvent event){
        event.register(ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness"),
            new DimensionSpecialEffects(Float.NaN,true,DimensionSpecialEffects.SkyType.NONE,true,true){
                @Override public Vec3 getBrightnessDependentFogColor(Vec3 fog,float brightness){
                    return new Vec3(.84,.84,.86);
                }
                @Override public boolean isFoggyAt(int x,int y){return false;}
            });
    }
}