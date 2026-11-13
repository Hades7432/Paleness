package ua.hades7432.paleness;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class InfuserBlock extends Block implements EntityBlock {
    public static final IntegerProperty PHASE = IntegerProperty.create("phase",0,4);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public InfuserBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(PHASE,0).setValue(FACING,Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(PHASE,FACING);
    }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    @Override protected BlockState rotate(BlockState s,Rotation rot) {
        return s.setValue(FACING,rot.rotate(s.getValue(FACING)));
    }
    @Override protected BlockState mirror(BlockState s,Mirror mirror) {
        return s.rotate(mirror.getRotation(s.getValue(FACING)));
    }
    @Override public RenderShape getRenderShape(BlockState s) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {
        return new InfuserEntity(pos,state);
    }
    @Nullable @Override public MenuProvider getMenuProvider(BlockState s,Level level,BlockPos pos) {
        return level.getBlockEntity(pos) instanceof InfuserEntity entity ? entity : null;
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(!level.isClientSide && player instanceof ServerPlayer sp &&
            level.getBlockEntity(pos) instanceof InfuserEntity entity)
            sp.openMenu(entity,buf -> buf.writeBlockPos(pos));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState s,BlockEntityType<T> t) {
        if(level.isClientSide || t!=PaleContent.INFUSER_BE.get())return null;
        return (world,pos,state,be) -> InfuserEntity.tick(world,pos,state,(InfuserEntity)be);
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving) {
        if(!state.is(replacement.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof InfuserEntity be) {
            for(int i=0;i<2;i++) {
                ItemStack stack=be.inventory.getStackInSlot(i);
                if(!stack.isEmpty())popResource(level,pos,stack.copy());
            }
        }
        super.onRemove(state,level,pos,replacement,moving);
    }
}