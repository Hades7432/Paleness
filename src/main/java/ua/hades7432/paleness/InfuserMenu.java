package ua.hades7432.paleness;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class InfuserMenu extends AbstractContainerMenu {
    private final InfuserEntity machine;
    private final BlockPos pos;
    private final ContainerData data;
    public InfuserMenu(int id,Inventory inv,BlockPos pos) {
        this(id,inv,pos,null,new ItemStackHandler(2),new SimpleContainerData(3));
    }
    public InfuserMenu(int id,Inventory inv,InfuserEntity machine) {
        this(id,inv,machine.getBlockPos(),machine,machine.inventory,machine.data);
    }
    private InfuserMenu(int id,Inventory inv,BlockPos pos,InfuserEntity machine,ItemStackHandler slots,ContainerData data) {
        super(PaleContent.INFUSER_MENU.get(),id);
        this.pos=pos;this.machine=machine;this.data=data;
        addSlot(new SlotItemHandler(slots,0,45,52) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(PaleMetalContent.PALE_METAL_INGOT.get()) && (machine==null || machine.progress==0);
            }
            @Override public boolean mayPickup(Player player){return machine==null || machine.progress==0;}
        });
        addSlot(new SlotItemHandler(slots,1,113,52) {
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.QUARTZ_BLOCK) && (machine==null || machine.progress==0);
            }
            @Override public boolean mayPickup(Player player){return machine==null || machine.progress==0;}
        });
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,130+r*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,188));
        addDataSlots(data);
    }
    public int progress(){return data.get(0);}
    public int phase(){return data.get(1);}
    public int result(){return data.get(2);}
    @Override public boolean clickMenuButton(Player p,int id){return id==0 && machine!=null && machine.start();}
    @Override public boolean stillValid(Player p){
        return p.level().getBlockEntity(pos) instanceof InfuserEntity
                && p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64;
    }
    @Override public ItemStack quickMoveStack(Player p,int index){
        Slot slot=slots.get(index);
        if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack src=slot.getItem(),result=src.copy();
        if(index<2){
            if(!moveItemStackTo(src,2,slots.size(),true))return ItemStack.EMPTY;
        } else {
            int dest=src.is(PaleMetalContent.PALE_METAL_INGOT.get())?0:src.is(Items.QUARTZ_BLOCK)?1:-1;
            if(dest<0||!moveItemStackTo(src,dest,dest+1,false))return ItemStack.EMPTY;
        }
        if(src.isEmpty())slot.set(ItemStack.EMPTY); else slot.setChanged();
        return result;
    }
}