package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Paleness.ID)
@PrefixGameTestTemplate(false)
public final class PalenessGameTests {

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyRecipesAndRegistry(GameTestHelper h){
        var level=h.getLevel();
        for(String id:new String[]{"pale_alloy","pale_igniter","pale_metal_block",
            "pale_metal_ingots_from_block","pale_metal_ingot_smelting","pale_metal_ingot_blasting"}){
            h.assertTrue(level.getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(Paleness.ID,id)).isPresent(),"Missing recipe: "+id);
        }
        h.assertTrue(PaleMetalContent.PALE_ORE.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
            "Pale Ore must be mineable with a pickaxe");
        h.assertTrue(PaleMetalContent.PALE_ORE.get().defaultBlockState().is(BlockTags.NEEDS_IRON_TOOL),
            "Pale Ore must require iron tier");
        h.assertTrue(PaleContent.PALE_QUARTZ.get().defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE),
            "Pale Quartz must be mineable");
        h.assertTrue(level.registryAccess().registryOrThrow(Registries.BIOME)
            .containsKey(ResourceLocation.fromNamespaceAndPath(Paleness.ID,"stone_wastes")),
            "Paleness dimension biome must exist");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyOreWiredIntoOverworldBiomes(GameTestHelper h){
        var registry=h.getLevel().registryAccess();
        var biome=registry.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        var placed=registry.registryOrThrow(Registries.PLACED_FEATURE).getHolderOrThrow(
            ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Paleness.ID,"pale_ore")));
        h.assertTrue(biome.value().modifiableBiomeInfo().get().generationSettings().hasFeature(placed.value()),
            "Configured pale ore must be present in actual plains features");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyOreReplacesDeepslate(GameTestHelper h){
        ServerLevel level=h.getLevel();
        var registry=level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        var feature=registry.get(ResourceLocation.fromNamespaceAndPath(Paleness.ID,"pale_ore"));
        h.assertTrue(feature!=null,"Configured feature missing");
        BlockPos seed=h.absolutePos(new BlockPos(10,1,10));
        BlockPos center=new BlockPos(seed.getX(),-52,seed.getZ());
        for(int x=-5;x<=5;x++)for(int y=-5;y<=5;y++)for(int z=-5;z<=5;z++)
            level.setBlock(center.offset(x,y,z),Blocks.DEEPSLATE.defaultBlockState(),2);
        boolean placed=false;
        for(int i=0;i<8;i++) {
            placed|=feature.place(level,level.getChunkSource().getGenerator(),
                RandomSource.create(i+9941L),center);
        }
        h.assertTrue(placed,"Pale Ore must be able to generate at Y=-52 inside deepslate");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyConnectedQuartz(GameTestHelper h){
        ServerLevel level=h.getLevel();
        BlockPos a=h.absolutePos(new BlockPos(2,2,2));
        BlockPos b=a.east();
        level.setBlock(a,PaleContent.PALE_QUARTZ.get().defaultBlockState(),3);
        level.setBlock(b,PaleContent.PALE_QUARTZ.get().defaultBlockState(),3);
        h.assertTrue(level.getBlockState(a).getValue(PaleQuartzBlock.EAST),"Quartz east edge should merge");
        h.assertTrue(level.getBlockState(b).getValue(PaleQuartzBlock.WEST),"Quartz west edge should merge");
        level.removeBlock(b,false);
        h.assertTrue(!level.getBlockState(a).getValue(PaleQuartzBlock.EAST),"Quartz edge must return after breaking neighbor");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifySoulMark(GameTestHelper h){
        var pig=h.spawn(EntityType.PIG,new BlockPos(4,2,4));
        SoulTaggerItem.mark(pig);
        h.assertTrue(SoulTaggerItem.isMarked(pig),"Mob needs 10s active soul marker");
        h.assertTrue(pig.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),"Mob should visibly glow");
        pig.getPersistentData().putLong(SoulTaggerItem.EXPIRES,pig.level().getGameTime()-1);
        h.assertTrue(!SoulTaggerItem.isMarked(pig),"Expired markers cannot yield pale souls");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=200)
    public static void verifyHorizontalPortal(GameTestHelper h){
        ServerLevel level=h.getLevel();
        BlockPos corner=h.absolutePos(new BlockPos(2,2,2));
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            if((x==0||x==4)&&(z==0||z==4))continue;
            BlockPos pos=corner.offset(x,0,z);
            level.setBlock(pos,x==0||x==4||z==0||z==4
                ?PaleContent.PALE_QUARTZ.get().defaultBlockState()
                :Blocks.AIR.defaultBlockState(),3);
        }
        h.assertTrue(PalePortalLogic.canLight(level,corner.offset(0,0,2)),
            "Pale Quartz 5x5 horizontal frame must be ignitable without corner blocks");
        h.assertTrue(PalePortalLogic.light(level,corner.offset(0,0,2)),
            "Portal ignition failed");
        BlockPos center=corner.offset(2,0,2);
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)
            h.assertTrue(center.equals(PalePortalLogic.centerOf(level,corner.offset(x,0,z))),
                "Nine portal tiles must share a center");
        level.removeBlock(corner.offset(0,0,2),false);
        h.runAfterDelay(2,()->{
            for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)
                h.assertTrue(!level.getBlockState(corner.offset(x,0,z)).is(PaleContent.PORTAL.get()),
                    "Gate must extinguish after breaking its frame");
            h.succeed();
        });
    }

    @GameTest(template="empty",timeoutTicks=300)
    public static void verifyDestinationOnGround(GameTestHelper h){
        ServerLevel level=h.getLevel();
        BlockPos near=h.absolutePos(new BlockPos(11,2,11));
        BlockPos portal=PalePortalLogic.makeDestination(level,near);
        h.assertTrue(PalePortalLogic.validPortal(level,portal),"Auto-created destination portal invalid");
        BlockPos feet=PalePortalLogic.safeLanding(level,portal);
        h.assertTrue(Math.max(Math.abs(feet.getX()-portal.getX()),Math.abs(feet.getZ()-portal.getZ()))>=3,
            "Landing should be at least three blocks away");
        h.assertTrue(level.getBlockState(feet.below()).isSolid()
            && level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir(),
            "Landing must be grounded and have headroom");
        h.succeed();
    }
}
