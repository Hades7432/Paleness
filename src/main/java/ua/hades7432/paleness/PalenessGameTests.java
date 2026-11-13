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
        // GameTest runs a stripped-down overworld-only WorldStem and does not
        // instantiate data-pack worlds. Verify the registries it does load.
        h.assertTrue(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DIMENSION_TYPE).containsKey(
            ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness")),
            "Missing Paleness dimension type");
        h.assertTrue(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).containsKey(
            ResourceLocation.fromNamespaceAndPath(Paleness.ID,"stone_wastes")),
            "Missing Paleness biome");
        for(String id: new String[]{"paleness_analyzer","paleness_condenser",
                "paleness_infuser","super_pale_crystal"}) {
            h.assertTrue(h.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(Paleness.ID,id)).isPresent(),
                "Missing recipe: "+id);
        }
        h.succeed();
    }
}
