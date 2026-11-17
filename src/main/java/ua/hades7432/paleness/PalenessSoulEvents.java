package ua.hades7432.paleness;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/** Each correctly marked mob drops exactly one soul, replacing its experience. */
public final class PalenessSoulEvents {
    @SubscribeEvent
    public void onDeath(LivingDeathEvent event){
        var entity=event.getEntity();
        if(entity.level().isClientSide || !(entity instanceof Mob))return;
        if(!SoulTaggerItem.isMarked(entity))return;
        entity.getPersistentData().putBoolean(SoulTaggerItem.PAID,true);
        entity.spawnAtLocation(new ItemStack(PaleContent.PALE_SOUL.get()));
    }

    @SubscribeEvent
    public void onExperience(LivingExperienceDropEvent event){
        var entity=event.getEntity();
        if(entity.level().isClientSide || !(entity instanceof Mob))return;
        var tag=entity.getPersistentData();
        // Death fires before experience handling, so PAID is also evidence
        // that this death legitimately yielded a pale soul.
        if(tag.getBoolean(SoulTaggerItem.PAID) || SoulTaggerItem.isMarked(entity))
            event.setDroppedExperience(0);
    }
}
