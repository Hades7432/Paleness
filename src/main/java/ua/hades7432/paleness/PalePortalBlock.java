package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class PalePortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS=BlockStateProperties.HORIZONTAL_AXIS;
    public PalePortalBlock(BlockBehaviour.Properties props){
        super(props);
        registerDefaultState(stateDefinition.any().setValue(AXIS,Direction.Axis.X));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
        b.add(AXIS);
    }
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected void entityInside(BlockState state,Level level,BlockPos pos,Entity entity){
        if(level.isClientSide || !(entity instanceof ServerPlayer player))return;
        if(player.isOnPortalCooldown())return;
        PalePortalLogic.travel(player,pos,state.getValue(AXIS));
    }
    @Override protected void neighborChanged(BlockState state,Level level,BlockPos pos,Block neighbor,BlockPos from,boolean moving){
        if(!level.isClientSide && !PalePortalLogic.validPortal(level,pos,state.getValue(AXIS)))
            level.removeBlock(pos,false);
    }
    @Override public void animateTick(BlockState s,Level level,BlockPos p,RandomSource r){
        if(r.nextInt(6)!=0)return;
        double x=p.getX()+r.nextDouble(),y=p.getY()+r.nextDouble(),z=p.getZ()+r.nextDouble();
        level.addParticle(ParticleTypes.END_ROD,x,y,z,0,(r.nextDouble()-.5)*.012,0);
    }
}