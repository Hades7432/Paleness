package ua.hades7432.paleness;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
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
    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyPalenessValues(GameTestHelper h) {
        h.assertTrue(PalenessValues.calculate(new ItemStack(Items.SCULK)) < 0,
            "Sculk must be the negative Paleness exception");
        ItemStack quartz = new ItemStack(Items.QUARTZ_BLOCK);
        int natural = PalenessValues.calculate(quartz);
        h.assertTrue(natural > 0 && natural < 2000,
            "Natural quartz must start below the super-pale threshold");
        quartz.set(PaleContent.INFUSED.get(), 1800);
        h.assertTrue(PalenessValues.calculate(quartz) == natural + 1800,
            "Stored infusion must survive the Paleness calculation");
        ItemStack shard = new ItemStack(Paleness.SHARD.get());
        shard.set(Paleness.PALENESS_VALUE.get(), 1234);
        h.assertTrue(PalenessValues.calculate(shard) == 1234,
            "Shard data-component Paleness must remain intact");
        h.assertTrue(PalenessValues.calculate(new ItemStack(PaleContent.SUPER_CRYSTAL.get())) == 2000,
            "Super-pale crystals must have fixed Paleness of exactly 2");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyPalenessPortalFrame(GameTestHelper h) {
        ServerLevel level = h.getLevel();
        BlockPos corner = h.absolutePos(new BlockPos(1, 1, 1));
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 4; x++) {
                BlockPos pos = corner.offset(x, y, 0);
                boolean border = x == 0 || x == 3 || y == 0 || y == 4;
                level.setBlock(pos, border
                    ? PaleContent.SUPER_QUARTZ.get().defaultBlockState()
                    : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        h.assertTrue(PalePortalLogic.light(level, corner),
            "4x5 super-pale quartz frame must ignite successfully");
        h.assertTrue(level.getBlockState(corner.offset(1, 1, 0)).is(PaleContent.PORTAL.get()),
            "Portal interior must contain Paleness portal blocks");
        h.assertTrue(PalePortalLogic.validPortal(level, corner.offset(1, 2, 0),
                net.minecraft.core.Direction.Axis.X), "Portal must have a valid frame");
        h.succeed();
    }

}
