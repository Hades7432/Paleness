package ua.hades7432.paleness;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
@EventBusSubscriber(modid=Paleness.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientRegistration {
    private ClientRegistration() {}
    @SubscribeEvent public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PaleContent.INFUSER_MENU.get(),InfuserScreen::new);
    }
}
