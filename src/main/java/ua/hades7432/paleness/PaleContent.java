package ua.hades7432.paleness;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class PaleContent {
    private PaleContent(){}

    public static final DeferredBlock<PaleQuartzBlock> PALE_QUARTZ =
        Paleness.BLOCKS.register("pale_quartz",()->new PaleQuartzBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(3.0f,6.0f)
                .requiresCorrectToolForDrops().lightLevel(s->4)));
    public static final DeferredItem<BlockItem> PALE_QUARTZ_ITEM=
        Paleness.ITEMS.registerSimpleBlockItem("pale_quartz",PALE_QUARTZ);

    public static final DeferredBlock<PalePortalBlock> PORTAL =
        Paleness.BLOCKS.register("paleness_portal",()->new PalePortalBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
                .noCollission().noOcclusion().strength(-1.0f,3600000.0f)
                .lightLevel(s->13).noLootTable()));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<PalePortalEntity>> PORTAL_BE =
        Paleness.BLOCK_ENTITIES.register("paleness_portal",()->
            BlockEntityType.Builder.of(PalePortalEntity::new,PORTAL.get()).build(null));

    public static final DeferredItem<Item> PALE_ALLOY =
        Paleness.ITEMS.register("pale_alloy",()->new PaleAlloyItem(new Item.Properties()));
    public static final DeferredItem<Item> PALE_SOUL =
        Paleness.ITEMS.register("pale_soul",()->new Item(new Item.Properties()));
    public static final DeferredItem<Item> SOUL_TAGGER =
        Paleness.ITEMS.register("soul_tagger",()->new SoulTaggerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> PALE_IGNITER =
        Paleness.ITEMS.register("pale_igniter",()->new PaleIgniterItem(new Item.Properties().stacksTo(1)));
    public static void init(){}
}
