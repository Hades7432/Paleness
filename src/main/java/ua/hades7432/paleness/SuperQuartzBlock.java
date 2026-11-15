package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SuperQuartzBlock extends Block {
    public SuperQuartzBlock(BlockBehaviour.Properties props) { super(props); }
    @Override public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 0) return;
        double x=pos.getX()+0.12+random.nextDouble()*0.76;
        double y=pos.getY()+0.2+random.nextDouble()*0.7;
        double z=pos.getZ()+0.12+random.nextDouble()*0.76;
        level.addParticle(ParticleTypes.END_ROD,x,y,z,0,0.008,0);
    }
}