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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class InfuserEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler inventory=new ItemStackHandler(2) {
        @Override public int getSlotLimit(int slot) { return 1; }
        @Override public boolean isItemValid(int slot,ItemStack stack) {
            return slot==0 ? stack.is(PaleMetalContent.PALE_METAL_INGOT.get()) : stack.is(Items.QUARTZ_BLOCK);
        }
        @Override protected void onContentsChanged(int slot) { InfuserEntity.this.setChanged(); }
    };
    public int progress=0, state=0, lastValue=0;
    private int fingerprint;
    public final ContainerData data=new ContainerData() {
        @Override public int get(int i) {
            return switch(i){case 0 -> progress;case 1 -> state;case 2 -> lastValue;default -> 0;};
        }
        @Override public void set(int i,int v) {
            switch(i){case 0 -> progress=v;case 1 -> state=v;case 2 -> lastValue=v;}
        }
        @Override public int getCount(){return 3;}
    };
    public InfuserEntity(BlockPos pos,BlockState state) {
        super(PaleContent.INFUSER_BE.get(),pos,state);
    }
    public boolean start() {
        if(progress>0) return false;
        ItemStack crystal=inventory.getStackInSlot(0), target=inventory.getStackInSlot(1);
        if(!crystal.is(PaleMetalContent.PALE_METAL_INGOT.get()) || !target.is(Items.QUARTZ_BLOCK))return false;
        // A pale ingot is refined energy; two treatments upgrade natural quartz.
        fingerprint=hash();
        progress=1;
        state=1;
        lastValue=PalenessValues.calculate(target);
        phase(1);
        setChanged();
        return true;
    }
    private int hash(){
        int h=1;
        for(int i=0;i<2;i++){
            ItemStack s=inventory.getStackInSlot(i);
            h=31*h+s.getItem().hashCode();
            h=31*h+s.getCount();
            h=31*h+s.getComponentsPatch().hashCode();
        }
        return h;
    }
    private void phase(int target){
        if(level==null || level.isClientSide)return;
        BlockState bs=level.getBlockState(worldPosition);
        if(bs.getBlock() instanceof InfuserBlock && bs.getValue(InfuserBlock.PHASE)!=target)
            level.setBlock(worldPosition,bs.setValue(InfuserBlock.PHASE,target),3);
    }
    public static void tick(Level level,BlockPos pos,BlockState bs,InfuserEntity e){
        if(e.progress<=0)return;
        if(e.fingerprint!=e.hash()){
            e.progress=0;e.state=0;e.phase(0);e.setChanged();return;
        }
        if(++e.progress<40) {
            e.phase(Math.min(3,1+e.progress*3/40));
            if(e.progress%4==0)e.setChanged();
            return;
        }
        ItemStack target=e.inventory.getStackInSlot(1);
        int value=PalenessValues.calculate(target)+750;
        if(target.is(Items.QUARTZ_BLOCK)){
            if(value>2000){
                e.inventory.setStackInSlot(1,new ItemStack(PaleContent.SUPER_QUARTZ_ITEM.get()));
                e.lastValue=2001;
            } else {
                ItemStack charged=new ItemStack(Items.QUARTZ_BLOCK);
                int base=PalenessValues.baseQuartz();
                charged.set(PaleContent.INFUSED.get(),Math.max(0,value-base));
                e.inventory.setStackInSlot(1,charged);
                e.lastValue=value;
            }
            e.inventory.extractItem(0,1,false);
        }
        e.progress=0;e.state=2;e.phase(4);e.setChanged();
    }
    @Override public Component getDisplayName(){return Component.translatable("block.paleness.paleness_infuser");}
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){
        return new InfuserMenu(id,inv,this);
    }
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider p) {
        super.saveAdditional(tag,p);
        tag.put("Inventory",inventory.serializeNBT(p));
        tag.putInt("Progress",progress);
        tag.putInt("State",state);
        tag.putInt("LastValue",lastValue);
        tag.putInt("Fingerprint",fingerprint);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider p) {
        super.loadAdditional(tag,p);
        if(tag.contains("Inventory"))inventory.deserializeNBT(p,tag.getCompound("Inventory"));
        progress=tag.getInt("Progress");state=tag.getInt("State");lastValue=tag.getInt("LastValue");
        fingerprint=tag.getInt("Fingerprint");
    }
}