package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MachineEntity extends BlockEntity implements MenuProvider {
    public final boolean analyzer;
    public final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override protected void onContentsChanged(int slot) {
            if (level != null && progress == 0 && state == 2 && (analyzer ? slot == 0 : slot == 4)) {
                state = 0;
                outputValue = 0;
                updatePhase(0);
            }
            MachineEntity.this.setChanged();
        }
        @Override public boolean isItemValid(int slot, ItemStack stack) {
            return analyzer ? slot == 0 : slot < 4;
        }
    };
    public int progress = 0;
    public int outputValue = 0;
    public int state = 0; // idle, processing, finished
    private int inputFingerprint = 0;

    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch(index) {
                case 0 -> progress;
                case 1 -> outputValue;
                case 2 -> state;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch(index) {
                case 0 -> progress = value;
                case 1 -> outputValue = value;
                case 2 -> state = value;
            }
        }
        @Override public int getCount() { return 3; }
    };

    public MachineEntity(boolean analyzer, BlockPos pos, BlockState state) {
        super(analyzer ? Paleness.ANALYZER_BE.get() : Paleness.CONDENSER_BE.get(), pos, state);
        this.analyzer = analyzer;
    }

    public boolean begin() {
        if (progress > 0) return false;
        if (analyzer) {
            if (inventory.getStackInSlot(0).isEmpty()) return false;
        } else {
            if (!inventory.getStackInSlot(4).isEmpty()) return false;
            for (int i=0; i<4; i++) {
                ItemStack input=inventory.getStackInSlot(i);
                if(input.isEmpty() || input.is(PaleContent.SUPER_CRYSTAL.get())
                    || input.is(PaleContent.SUPER_QUARTZ_ITEM.get())
                    || input.has(PaleContent.INFUSED.get())) return false;
            }
            int sum = 0;
            for (int i=0; i<4; i++) sum += PalenessValues.calculate(inventory.getStackInSlot(i));
            if (sum <= 0) return false;
        }
        inputFingerprint = fingerprint();
        outputValue = 0;
        state = 1;
        progress = 1;
        updatePhase(1);
        setChanged();
        return true;
    }

    private void updatePhase(int target) {
        if (level == null || level.isClientSide) return;
        BlockState blockState = level.getBlockState(worldPosition);
        if (blockState.getBlock() instanceof MachineBlock && blockState.getValue(MachineBlock.PHASE) != target)
            level.setBlock(worldPosition, blockState.setValue(MachineBlock.PHASE, target), 3);
    }

    private int fingerprint() {
        int h=1;
        for (int i=0; i<(analyzer ? 1 : 4); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            h=31*h+stack.getItem().hashCode();
            h=31*h+stack.getCount();
            h=31*h+stack.getComponentsPatch().hashCode();
        }
        return h;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MachineEntity machine) {
        if (machine.progress == 0) return;
        if (machine.inputFingerprint != machine.fingerprint()) {
            machine.progress=0;
            machine.state=0;
            machine.updatePhase(0);
            machine.setChanged();
            return;
        }
        int time = machine.analyzer ? 80 : 100;
        if (++machine.progress < time) {
            int nextPhase = Math.min(3, 1 + machine.progress * 3 / time);
            machine.updatePhase(nextPhase);
            machine.setChanged();
            return;
        }
        if (machine.analyzer) {
            machine.outputValue = PalenessValues.calculate(machine.inventory.getStackInSlot(0));
        } else {
            int sum = 0;
            for(int i=0;i<4;i++) sum += PalenessValues.calculate(machine.inventory.getStackInSlot(i));
            if (sum > 0 && machine.inventory.getStackInSlot(4).isEmpty()) {
                ItemStack shard = new ItemStack(Paleness.SHARD.get());
                shard.set(Paleness.PALENESS_VALUE.get(), Math.min(100000, sum));
                for(int i=0;i<4;i++) machine.inventory.extractItem(i, 1, false);
                machine.inventory.setStackInSlot(4, shard);
                machine.outputValue = sum;
            }
        }
        machine.progress=0;
        machine.state=2;
        machine.updatePhase(4);
        machine.setChanged();
    }

    @Override public Component getDisplayName() {
        return Component.translatable(analyzer ? "block.paleness.paleness_analyzer" : "block.paleness.paleness_condenser");
    }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MachineMenu(id, inv, this);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("Value", outputValue);
        tag.putInt("State", state);
        tag.putInt("Fingerprint", inputFingerprint);
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        progress=tag.getInt("Progress");
        outputValue=tag.getInt("Value");
        state=tag.getInt("State");
        inputFingerprint=tag.getInt("Fingerprint");
    }
}
