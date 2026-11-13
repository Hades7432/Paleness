package ua.hades7432.paleness;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Paleness.ID)
@PrefixGameTestTemplate(false)
public final class PalenessGameTests {
    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyWorldAndRecipes(GameTestHelper h) {
        h.assertTrue(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.LEVEL_STEM).containsKey(
            ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness")),
            "The Paleness dimension was not registered");
        for(String id: new String[]{"paleness_analyzer","paleness_condenser",
                "paleness_infuser","super_pale_crystal"}) {
            h.assertTrue(h.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(Paleness.ID,id)).isPresent(),
                "Missing recipe: "+id);
        }
        h.succeed();
    }
}
