package ua.hades7432.paleness;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Paleness.ID)
@PrefixGameTestTemplate(false)
public final class PalenessGameTests {
    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyWorldAndRecipes(GameTestHelper h) {
        h.assertTrue(h.getLevel().registryAccess().registryOrThrow(
            net.minecraft.core.registries.Registries.DIMENSION_TYPE).containsKey(
            ResourceLocation.fromNamespaceAndPath(Paleness.ID,"paleness")),
            "Missing Paleness dimension type");
        h.assertTrue(h.getLevel().registryAccess().registryOrThrow(
            net.minecraft.core.registries.Registries.BIOME).containsKey(
            ResourceLocation.fromNamespaceAndPath(Paleness.ID,"stone_wastes")),
            "Missing Paleness biome");
        for(String id:new String[]{"paleness_infuser","super_pale_crystal",
            "pale_metal_block","pale_metal_ingots_from_block",
            "pale_metal_ingot_smelting","pale_metal_ingot_blasting"}) {
            h.assertTrue(h.getLevel().getRecipeManager().byKey(
                ResourceLocation.fromNamespaceAndPath(Paleness.ID,id)).isPresent(),
                "Missing recipe: "+id);
        }
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyPalenessValues(GameTestHelper h) {
        h.assertTrue(PalenessValues.calculate(new ItemStack(Items.SCULK)) < 0,
            "Sculk must remain negative");
        ItemStack quartz=new ItemStack(Items.QUARTZ_BLOCK);
        int natural=PalenessValues.calculate(quartz);
        h.assertTrue(natural>0&&natural<2000,
            "Natural quartz must have positive Paleness below 2.000");
        quartz.set(PaleContent.INFUSED.get(),1800);
        h.assertTrue(PalenessValues.calculate(quartz)==natural+1800,
            "Stored Paleness infusion must be preserved");
        h.assertTrue(PalenessValues.calculate(new ItemStack(PaleContent.SUPER_CRYSTAL.get()))==2000,
            "Super-pale crystal is exactly 2.000");
        h.assertTrue(PalenessValues.calculate(new ItemStack(PaleContent.SUPER_QUARTZ_ITEM.get()))>2000,
            "Super-pale quartz must be above 2.000");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyHorizontalPortal(GameTestHelper h) {
        ServerLevel level=h.getLevel();
        BlockPos northwest=h.absolutePos(new BlockPos(2,2,2));
        for(int x=0;x<5;x++)for(int z=0;z<5;z++){
            if((x==0||x==4)&&(z==0||z==4))continue;
            BlockPos pos=northwest.offset(x,0,z);
            boolean border=x==0||x==4||z==0||z==4;
            level.setBlock(pos,border?PaleContent.SUPER_QUARTZ.get().defaultBlockState():
                    Blocks.AIR.defaultBlockState(),3);
        }
        h.assertTrue(PalePortalLogic.light(level,northwest.offset(0,0,2)),
            "Horizontal 5x5 frame without corners must light");
        for(int x=1;x<=3;x++)for(int z=1;z<=3;z++)
            h.assertTrue(level.getBlockState(northwest.offset(x,0,z)).is(PaleContent.PORTAL.get()),
                "All nine portal tiles must activate");
        BlockPos inner=northwest.offset(2,0,2);
        h.assertTrue(PalePortalLogic.validPortal(level,inner),
            "The active portal should remain lit with its border intact");
        h.succeed();
    }

    @GameTest(template="empty",timeoutTicks=150)
    public static void verifyPaleInfuser(GameTestHelper h){
        ServerLevel world=h.getLevel();
        BlockPos pos=h.absolutePos(new BlockPos(1,2,1));
        world.setBlock(pos,PaleContent.INFUSER.get().defaultBlockState(),3);
        InfuserEntity e=(InfuserEntity)world.getBlockEntity(pos);
        h.assertTrue(e!=null,"Missing infuser block entity");
        e.inventory.setStackInSlot(0,new ItemStack(PaleMetalContent.PALE_METAL_INGOT.get()));
        e.inventory.setStackInSlot(1,new ItemStack(Items.QUARTZ_BLOCK));
        h.assertTrue(e.start(),"Infuser must accept pale ingot and quartz");
        for(int i=0;i<40;i++)InfuserEntity.tick(world,pos,world.getBlockState(pos),e);
        h.assertTrue(e.inventory.getStackInSlot(0).isEmpty(),"Infuser must consume one ingot");
        h.assertTrue(e.inventory.getStackInSlot(1).is(Items.QUARTZ_BLOCK),
            "Quartz should retain first infusion");
        e.inventory.setStackInSlot(0,new ItemStack(PaleMetalContent.PALE_METAL_INGOT.get()));
        h.assertTrue(e.start(),"Infuser must support repeat treatments");
        for(int i=0;i<40;i++)InfuserEntity.tick(world,pos,world.getBlockState(pos),e);
        h.assertTrue(e.inventory.getStackInSlot(1).is(PaleContent.SUPER_QUARTZ_ITEM.get()),
            "Two ingots must saturate the quartz");
        h.succeed();
    }
}
