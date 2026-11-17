package ua.hades7432.paleness;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SoulTaggerItem extends Item {
    public static final int MARK_TICKS=200;
    static final String EXPIRES="paleness:soul_mark_expires";
    static final String MARK_DIM="paleness:soul_mark_dimension";
    static final String PAID="paleness:soul_awarded";

    public SoulTaggerItem(Properties properties){super(properties);}

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack,Player player,
                               LivingEntity target,InteractionHand hand){
        if(!(target instanceof Mob) || player.getCooldowns().isOnCooldown(this))return InteractionResult.PASS;
        if(!player.level().isClientSide){
            mark(target);
            player.level().playSound(null,target.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,0.65f,1.65f);
            player.getCooldowns().addCooldown(this,10);
            player.displayClientMessage(Component.translatable("message.paleness.soul_marked"),true);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    /** Right-click in the air to mark the mob aimed at, up to 16 blocks away.
     *  Uses an actual hitbox ray test and block collision, not a nearest-mob guess.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player.getCooldowns().isOnCooldown(this))
            return InteractionResultHolder.pass(stack);
        Vec3 from=player.getEyePosition();
        Vec3 to=from.add(player.getLookAngle().scale(16.0));
        var blockHit=level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,player));
        if(blockHit.getType()!=HitResult.Type.MISS)to=blockHit.getLocation();
        AABB candidateBounds=new AABB(from,to).inflate(0.8);
        Mob best=null;
        double bestDistance=Double.POSITIVE_INFINITY;
        for(Mob mob:level.getEntitiesOfClass(Mob.class,candidateBounds,m->m.isAlive())){
            var hit=mob.getBoundingBox().inflate(0.17).clip(from,to);
            if(hit.isEmpty())continue;
            double distance=from.distanceToSqr(hit.get());
            if(distance<bestDistance){bestDistance=distance;best=mob;}
        }
        if(best==null)return InteractionResultHolder.pass(stack);
        if(!level.isClientSide){
            mark(best);
            level.playSound(null,best.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,0.65f,1.65f);
            player.getCooldowns().addCooldown(this,10);
            player.displayClientMessage(Component.translatable("message.paleness.soul_marked"),true);
        }
        return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
    }

    public static void mark(LivingEntity mob){
        var tag=mob.getPersistentData();
        tag.putLong(EXPIRES,mob.level().getGameTime()+MARK_TICKS);
        tag.putString(MARK_DIM,mob.level().dimension().location().toString());
        tag.remove(PAID);
        mob.addEffect(new MobEffectInstance(MobEffects.GLOWING,MARK_TICKS,0,false,false,true));
    }
    public static boolean isMarked(LivingEntity mob){
        var data=mob.getPersistentData();
        return data.contains(EXPIRES)
            && !data.getBoolean(PAID)
            && mob.level().getGameTime() <= data.getLong(EXPIRES)
            && mob.level().dimension().location().toString().equals(data.getString(MARK_DIM));
    }
}
