package ua.hades7432.paleness;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/** 0.5 content is source-controlled; art is pending the five-concept approval. */
public final class PaleMetalContent {
    private PaleMetalContent() {}

    public static final DeferredBlock<Block> PALE_ORE =
        Paleness.BLOCKS.register("pale_ore", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                .strength(4.5f, 6f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> PALE_ORE_ITEM =
        Paleness.ITEMS.registerSimpleBlockItem("pale_ore", PALE_ORE);

    public static final DeferredItem<Item> RAW_PALE_METAL =
        Paleness.ITEMS.register("raw_pale_metal", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> PALE_METAL_INGOT =
        Paleness.ITEMS.register("pale_metal_ingot", () -> new Item(new Item.Properties()));

    public static final DeferredBlock<Block> PALE_METAL_BLOCK =
        Paleness.BLOCKS.register("pale_metal_block", () -> new Block(
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ)
                .strength(5f, 6f).sound(Blocks.IRON_BLOCK.defaultBlockState().getSoundType()).requiresCorrectToolForDrops()));
    public static final DeferredItem<BlockItem> PALE_METAL_BLOCK_ITEM =
        Paleness.ITEMS.registerSimpleBlockItem("pale_metal_block", PALE_METAL_BLOCK);

    public static final DeferredItem<Item> PALENESS_DETECTOR =
        Paleness.ITEMS.register("paleness_detector", () ->
            new PalenessDetector(new Item.Properties().stacksTo(1)));
    public static void init() {}
}
