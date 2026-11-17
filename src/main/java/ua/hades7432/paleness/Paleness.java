package ua.hades7432.paleness;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod(Paleness.ID)
public final class Paleness {
    public static final String ID="paleness";
    public static final DeferredRegister.Blocks BLOCKS=DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS=DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,ID);
    public static final DeferredRegister<CreativeModeTab> TABS=
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ID);

    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TAB =
        TABS.register("paleness",()-> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.paleness"))
            .icon(()->new ItemStack(PaleContent.SOUL_TAGGER.get()))
            .displayItems((parameters,output)->{
                output.accept(PaleMetalContent.PALE_ORE_ITEM.get());
                output.accept(PaleMetalContent.RAW_PALE_METAL.get());
                output.accept(PaleMetalContent.PALE_METAL_INGOT.get());
                output.accept(PaleMetalContent.PALE_METAL_BLOCK_ITEM.get());
                output.accept(PaleMetalContent.PALENESS_DETECTOR.get());
                output.accept(PaleContent.PALE_ALLOY.get());
                output.accept(PaleContent.PALE_QUARTZ_ITEM.get());
                output.accept(PaleContent.SOUL_TAGGER.get());
                output.accept(PaleContent.PALE_SOUL.get());
                output.accept(PaleContent.PALE_IGNITER.get());
            }).build());

    public Paleness(IEventBus bus) {
        PaleMetalContent.init();
        PaleContent.init();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        NeoForge.EVENT_BUS.register(new PalenessSoulEvents());
    }
}
