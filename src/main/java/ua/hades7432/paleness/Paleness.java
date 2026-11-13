package ua.hades7432.paleness;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;

@Mod(Paleness.ID)
public final class Paleness {
    public static final String ID = "paleness";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PALENESS_VALUE =
        COMPONENTS.registerComponentType("paleness_value", builder ->
            builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredBlock<MachineBlock> ANALYZER =
        BLOCKS.register("paleness_analyzer", () -> new MachineBlock(true, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(3.5f).lightLevel(state -> 6).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredBlock<MachineBlock> CONDENSER =
        BLOCKS.register("paleness_condenser", () -> new MachineBlock(false, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.5f).lightLevel(state -> 10).requiresCorrectToolForDrops().noOcclusion()));
    public static final DeferredItem<BlockItem> ANALYZER_ITEM = ITEMS.registerSimpleBlockItem("paleness_analyzer", ANALYZER);
    public static final DeferredItem<BlockItem> CONDENSER_ITEM = ITEMS.registerSimpleBlockItem("paleness_condenser", CONDENSER);
    public static final DeferredItem<Item> SHARD =
        ITEMS.register("paleness_shard", () -> new ShardItem(new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineEntity>> ANALYZER_BE =
        BLOCK_ENTITIES.register("paleness_analyzer", () -> BlockEntityType.Builder.of(
            (pos, state) -> new MachineEntity(true, pos, state), ANALYZER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MachineEntity>> CONDENSER_BE =
        BLOCK_ENTITIES.register("paleness_condenser", () -> BlockEntityType.Builder.of(
            (pos, state) -> new MachineEntity(false, pos, state), CONDENSER.get()).build(null));
    public static final DeferredHolder<MenuType<?>, MenuType<MachineMenu>> MACHINE_MENU =
        MENUS.register("machine", () -> IMenuTypeExtension.create(
            (windowId, inv, buf) -> new MachineMenu(windowId, inv, buf.readBlockPos())));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
        TABS.register("paleness", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.paleness"))
            .icon(() -> new ItemStack(SHARD.get()))
            .displayItems((parameters, output) -> {
                output.accept(ANALYZER_ITEM.get());
                output.accept(CONDENSER_ITEM.get());
                output.accept(SHARD.get());
            }).build());

    public Paleness(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ANALYZER_BE.get(),
                (entity, side) -> entity.inventory);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CONDENSER_BE.get(),
                (entity, side) -> entity.inventory);
    }

    public static final class ShardItem extends Item {
        public ShardItem(Properties properties) { super(properties); }
        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            int value = stack.getOrDefault(PALENESS_VALUE.get(), 0);
            tooltip.add(Component.translatable("tooltip.paleness.value", PalenessValues.format(value)));
        }
    }
}
