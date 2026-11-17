package ua.hades7432.paleness;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class PaleIgniterItem extends Item {
    public PaleIgniterItem(Properties props){super(props);}
    @Override
    public InteractionResult useOn(UseOnContext context){
        var level=context.getLevel();
        if(level.isClientSide)
            return PalePortalLogic.canLight(level,context.getClickedPos()) ?
                InteractionResult.SUCCESS : InteractionResult.PASS;
        if(!PalePortalLogic.light(level,context.getClickedPos()))return InteractionResult.PASS;
        level.playSound(null,context.getClickedPos(),SoundEvents.BEACON_ACTIVATE,
            SoundSource.BLOCKS,0.8f,1.35f);
        var player=context.getPlayer();
        if(player==null || !player.getAbilities().instabuild)
            context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }
}
