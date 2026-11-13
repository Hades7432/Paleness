package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MachineMenu extends AbstractContainerMenu {
    public final boolean analyzer;
    private final MachineEntity entity;
    private final BlockPos pos;
    private final ContainerData data;
    private final int machineSlots;

    public MachineMenu(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, pos, null,
                new ItemStackHandler(5), new SimpleContainerData(3));
    }
    public MachineMenu(int id, Inventory playerInventory, MachineEntity entity) {
        this(id, playerInventory, entity.getBlockPos(), entity, entity.inventory, entity.data);
    }
    private MachineMenu(int id, Inventory playerInventory, BlockPos pos, MachineEntity entity,
                        ItemStackHandler handler, ContainerData data) {
        super(Paleness.MACHINE_MENU.get(), id);
        this.pos = pos;
        this.entity = entity;
        this.data = data;
        this.analyzer = entity != null ? entity.analyzer
                : playerInventory.player.level().getBlockState(pos).is(Paleness.ANALYZER.get());
        this.machineSlots = analyzer ? 1 : 5;
        if (analyzer) {
            addSlot(new SlotItemHandler(handler, 0, 79, 51) {
                @Override public boolean mayPlace(ItemStack stack) { return entity == null || entity.progress == 0; }
                @Override public boolean mayPickup(Player player) { return entity == null || entity.progress == 0; }
            });
        } else {
            int[][] positions = {{48,31},{110,31},{48,80},{110,80},{79,57}};
            for (int i=0;i<5;i++) {
                final int index=i;
                addSlot(new SlotItemHandler(handler, i, positions[i][0],positions[i][1]) {
                    @Override public boolean mayPlace(ItemStack stack) {
                        return index<4 && (entity == null || entity.progress == 0);
                    }
                    @Override public boolean mayPickup(Player player) {
                        return entity == null || entity.progress == 0;
                    }
                });
            }
        }
        for (int row=0;row<3;row++)
            for (int col=0;col<9;col++)
                addSlot(new Slot(playerInventory, col+row*9+9, 8+col*18, 130+row*18));
        for (int col=0;col<9;col++)
            addSlot(new Slot(playerInventory,col,8+col*18,188));
        addDataSlots(data);
    }
    public int progress() { return data.get(0); }
    public int value() { return data.get(1); }
    public int state() { return data.get(2); }
    @Override public boolean clickMenuButton(Player player, int id) {
        return id==0 && entity!=null && entity.begin();
    }
    @Override public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof MachineEntity
            && player.distanceToSqr(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5)<=64;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        ItemStack result=ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if (!slot.hasItem()) return result;
        ItemStack source=slot.getItem();
        result=source.copy();
        if (index<machineSlots) {
            if (!moveItemStackTo(source,machineSlots,slots.size(),true)) return ItemStack.EMPTY;
        } else {
            int targetEnd=analyzer?1:4;
            if (!moveItemStackTo(source,0,targetEnd,false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }
}
