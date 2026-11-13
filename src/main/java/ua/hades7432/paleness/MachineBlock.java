package ua.hades7432.paleness;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

public final class MachineBlock extends BaseEntityBlock {
    private final boolean analyzer;
    public MachineBlock(boolean analyzer, BlockBehaviour.Properties properties) {
        super(properties);
        this.analyzer = analyzer;
    }
    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MachineEntity(analyzer, pos, state);
    }
    @Nullable @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MachineEntity entity ? entity : null;
    }
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof MachineEntity entity) {
            serverPlayer.openMenu(entity, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Nullable @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, analyzer ? Paleness.ANALYZER_BE.get() : Paleness.CONDENSER_BE.get(), MachineEntity::tick);
    }
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineEntity entity) {
            if (!level.isClientSide) {
                for (int i = 0; i < entity.inventory.getSlots(); i++) {
                    ItemStack stack = entity.inventory.getStackInSlot(i);
                    if (!stack.isEmpty()) popResource(level, pos, stack.copy());
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
