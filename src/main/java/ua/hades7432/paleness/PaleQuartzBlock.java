package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Six neighboring connections, 64 states, sixteen face designs (T/R/B/L). */
public final class PaleQuartzBlock extends Block {
    public static final BooleanProperty NORTH=BlockStateProperties.NORTH;
    public static final BooleanProperty EAST=BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH=BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST=BlockStateProperties.WEST;
    public static final BooleanProperty UP=BlockStateProperties.UP;
    public static final BooleanProperty DOWN=BlockStateProperties.DOWN;

    public PaleQuartzBlock(Properties props){
        super(props);
        registerDefaultState(defaultBlockState()
            .setValue(NORTH,false).setValue(EAST,false)
            .setValue(SOUTH,false).setValue(WEST,false)
            .setValue(UP,false).setValue(DOWN,false));
    }
    public static BooleanProperty property(Direction side){
        return switch(side){
            case NORTH->NORTH;case EAST->EAST;case SOUTH->SOUTH;
            case WEST->WEST;case UP->UP;case DOWN->DOWN;
        };
    }
    public static BlockState connectedState(BlockGetter level,BlockPos pos){
        BlockState state=PaleContent.PALE_QUARTZ.get().defaultBlockState();
        for(Direction side:Direction.values())
            state=state.setValue(property(side),
                level.getBlockState(pos.relative(side)).is(PaleContent.PALE_QUARTZ.get()));
        return state;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context){
        return connectedState(context.getLevel(),context.getClickedPos());
    }
    @Override
    protected BlockState updateShape(BlockState state,Direction side,BlockState neighbor,
            LevelAccessor level,BlockPos pos,BlockPos neighborPos){
        return state.setValue(property(side),neighbor.is(this));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
        b.add(NORTH,EAST,SOUTH,WEST,UP,DOWN);
    }
}
