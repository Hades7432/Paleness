package ua.hades7432.paleness;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

public final class PaleAlloyItem extends Item {
    public PaleAlloyItem(Properties props){super(props);}

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level=context.getLevel();
        var pos=context.getClickedPos();
        if(!level.getBlockState(pos).is(Blocks.QUARTZ_BLOCK))return InteractionResult.PASS;
        if(!level.isClientSide){
            level.setBlock(pos,PaleQuartzBlock.connectedState(level,pos),3);
            level.playSound(null,pos,SoundEvents.ANVIL_USE,SoundSource.BLOCKS,0.35f,1.6f);
            var player=context.getPlayer();
            if(player==null || !player.getAbilities().instabuild)
                context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
