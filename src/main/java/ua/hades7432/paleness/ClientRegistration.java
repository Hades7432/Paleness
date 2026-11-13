package ua.hades7432.paleness;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

public final class ClientRegistration {
    private ClientRegistration() {}
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Paleness.MACHINE_MENU.get(), MachineScreen::new);
    }
}
