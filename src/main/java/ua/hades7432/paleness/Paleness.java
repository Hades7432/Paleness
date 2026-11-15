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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
        TABS.register("paleness", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.paleness"))
            .icon(() -> new ItemStack(PaleMetalContent.PALENESS_DETECTOR.get()))
            .displayItems((parameters, output) -> {
                output.accept(PaleMetalContent.PALE_ORE_ITEM.get());
                output.accept(PaleMetalContent.RAW_PALE_METAL.get());
                output.accept(PaleMetalContent.PALE_METAL_INGOT.get());
                output.accept(PaleMetalContent.PALE_METAL_BLOCK_ITEM.get());
                output.accept(PaleMetalContent.PALENESS_DETECTOR.get());
                output.accept(PaleContent.INFUSER_ITEM.get());
                output.accept(PaleContent.SUPER_QUARTZ_ITEM.get());
                output.accept(PaleContent.SUPER_CRYSTAL.get());
                output.accept(PaleMetalContent.PALE_ORE_ITEM.get());
                output.accept(PaleMetalContent.RAW_PALE_METAL.get());
                output.accept(PaleMetalContent.PALE_METAL_INGOT.get());
                output.accept(PaleMetalContent.PALE_METAL_BLOCK_ITEM.get());
                output.accept(PaleMetalContent.PALENESS_DETECTOR.get());
            }).build());

    public Paleness(IEventBus bus) {
        PaleContent.init();
        PaleMetalContent.init();
        PaleMetalContent.init();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
        bus.addListener(this::registerCapabilities);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PaleContent.INFUSER_BE.get(),
                (entity, side) -> entity.inventory);
    }

}
