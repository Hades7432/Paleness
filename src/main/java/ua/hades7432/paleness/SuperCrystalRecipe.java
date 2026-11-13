package ua.hades7432.paleness;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class SuperCrystalRecipe extends CustomRecipe {
    public SuperCrystalRecipe(CraftingBookCategory category) { super(category); }
    @Override public boolean matches(CraftingInput input, Level level) {
        int total=0, shards=0;
        for(int i=0;i<input.size();i++) {
            ItemStack stack=input.getItem(i);
            if(stack.isEmpty()) continue;
            if(!stack.is(Paleness.SHARD.get())) return false;
            int amount=PalenessValues.calculate(stack);
            if(amount<=0) return false;
            total+=amount;
            shards++;
        }
        return shards>=1 && total>=2000;
    }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return matches(input,null) ? new ItemStack(PaleContent.SUPER_CRYSTAL.get()) : ItemStack.EMPTY;
    }
    @Override public boolean canCraftInDimensions(int width,int height) { return width*height>=1; }
    @Override public RecipeSerializer<?> getSerializer() { return PaleContent.SUPER_CRYSTAL_RECIPE.get(); }
}