package ua.hades7432.paleness;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import java.util.List;

public final class PaleContent {
    private PaleContent() {}
    public static final DeferredHolder<DataComponentType<?>,DataComponentType<Integer>> INFUSED =
        Paleness.COMPONENTS.registerComponentType("infused_paleness", b ->
            b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredBlock<InfuserBlock> INFUSER =
        Paleness.BLOCKS.register("paleness_infuser", () -> new InfuserBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0f)
                .requiresCorrectToolForDrops().noOcclusion()
                .lightLevel(s -> s.getValue(InfuserBlock.PHASE)==0 ? 3 : 7+s.getValue(InfuserBlock.PHASE))));
    public static final DeferredBlock<SuperQuartzBlock> SUPER_QUARTZ =
        Paleness.BLOCKS.register("super_pale_quartz", () -> new SuperQuartzBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(4.0f)
                .requiresCorrectToolForDrops().lightLevel(s -> 11).randomTicks()));
    public static final DeferredBlock<PalePortalBlock> PORTAL =
        Paleness.BLOCKS.register("paleness_portal", () -> new PalePortalBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).noCollission().noOcclusion()
                .strength(-1.0f,3600000.0f).lightLevel(s -> 13).noLootTable()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PalePortalEntity>> PORTAL_BE =
        Paleness.BLOCK_ENTITIES.register("paleness_portal", () ->
            BlockEntityType.Builder.of(PalePortalEntity::new, PORTAL.get()).build(null));
    public static final DeferredItem<BlockItem> INFUSER_ITEM =
        Paleness.ITEMS.registerSimpleBlockItem("paleness_infuser", INFUSER);
    public static final DeferredItem<BlockItem> SUPER_QUARTZ_ITEM =
        Paleness.ITEMS.registerSimpleBlockItem("super_pale_quartz", SUPER_QUARTZ);
    public static final DeferredItem<Item> SUPER_CRYSTAL =
        Paleness.ITEMS.register("super_pale_crystal", () -> new Item(new Item.Properties().stacksTo(16)) {
            @Override public InteractionResult useOn(UseOnContext context) {
                if(context.getLevel().isClientSide) return InteractionResult.SUCCESS;
                if(!PalePortalLogic.light(context.getLevel(),context.getClickedPos()))
                    return InteractionResult.PASS;
                if(context.getPlayer()==null || !context.getPlayer().getAbilities().instabuild)
                    context.getItemInHand().shrink(1);
                return InteractionResult.CONSUME;
            }
            @Override public void appendHoverText(ItemStack stack, TooltipContext ctx,
                    List<Component> lines, TooltipFlag flag) {
                super.appendHoverText(stack,ctx,lines,flag);
                lines.add(Component.translatable("tooltip.paleness.value", "+2.000"));
                lines.add(Component.translatable("tooltip.paleness.super_crystal"));
            }
        });
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<InfuserEntity>> INFUSER_BE =
        Paleness.BLOCK_ENTITIES.register("paleness_infuser", () ->
            BlockEntityType.Builder.of(InfuserEntity::new, INFUSER.get()).build(null));
    public static final DeferredHolder<MenuType<?>,MenuType<InfuserMenu>> INFUSER_MENU =
        Paleness.MENUS.register("infuser", () -> IMenuTypeExtension.create(
            (id, inv, buf) -> new InfuserMenu(id,inv,buf.readBlockPos())));
    public static void init() {}
}