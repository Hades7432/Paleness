package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One small renderer component per tile of the horizontal Paleness gateway. */
public final class PalePortalEntity extends BlockEntity {
    public PalePortalEntity(BlockPos pos, BlockState state) {
        super(PaleContent.PORTAL_BE.get(), pos, state);
    }
}
