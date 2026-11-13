package ua.hades7432.paleness;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Paleness.ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRegistration {
    private ClientRegistration() {}

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Paleness.MACHINE_MENU.get(), MachineScreen::new);
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
            Paleness.SHARD.get(),
            ResourceLocation.fromNamespaceAndPath(Paleness.ID, "shard_tier"),
            (stack, level, holder, seed) -> {
                int value = stack.getOrDefault(Paleness.PALENESS_VALUE.get(), 0);
                return Math.min(1.0f, Math.max(0.0f, value / 4000.0f));
            }));
    }
}
