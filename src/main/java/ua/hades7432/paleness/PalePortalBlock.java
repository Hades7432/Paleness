package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/** Horizontal, non-solid portal plane. A renderer will use the approved bright-white art. */
public final class PalePortalBlock extends Block implements EntityBlock {
    public PalePortalBlock(BlockBehaviour.Properties props) { super(props); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {
        return new PalePortalEntity(pos,state);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player && !player.isOnPortalCooldown())
            PalePortalLogic.travel(player, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block block, BlockPos neighbor, boolean moving) {
        if (!level.isClientSide && !PalePortalLogic.validPortal(level, pos))
            level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource r) {
        if (r.nextInt(8) != 0) return;
        double x = pos.getX() + r.nextDouble();
        double y = pos.getY() + 0.79 + r.nextDouble() * 0.12;
        double z = pos.getZ() + r.nextDouble();
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.006, 0);
    }
}
