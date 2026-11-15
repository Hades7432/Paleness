package ua.hades7432.paleness;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

/** Point at a block, right click and receive the numeric Paleness on the action bar. */
public final class PalenessDetector extends Item {
    public PalenessDetector(Properties props) { super(props); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getPlayer() != null) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            ItemStack sample = new ItemStack(state.getBlock().asItem());
            int value = PalenessValues.calculate(sample);
            context.getPlayer().displayClientMessage(
                Component.translatable("message.paleness.detector", PalenessValues.format(value)),
                true);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
